import { useCallback, useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, Grid, Stack, Typography } from '@mui/material';
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import StorefrontOutlinedIcon from '@mui/icons-material/StorefrontOutlined';
import WidgetsOutlinedIcon from '@mui/icons-material/WidgetsOutlined';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { MetricCard } from '../components/common/MetricCard';
import { PageHeader } from '../components/common/PageHeader';
import { RequestState } from '../components/common/RequestState';
import { getDashboardSummary } from '../services/dashboardService';
import type { DashboardSummary } from '../types/api';
import { numberFormat } from '../utils/format';

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reload, setReload] = useState(0);

  const loadSummary = useCallback(async (signal: AbortSignal) => {
    setLoading(true);
    setError(null);
    try {
      setSummary(await getDashboardSummary(signal));
    } catch (requestError) {
      if (!signal.aborted) {
        setError(requestError instanceof Error ? requestError.message : 'No fue posible cargar el resumen.');
      }
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void loadSummary(controller.signal);
    return () => controller.abort();
  }, [loadSummary, reload]);

  return (
    <>
      <PageHeader title="Resumen general" description="Una vista clara de la operación de tus almacenes." />
      <RequestState
        loading={loading}
        error={error}
        empty={!summary}
        emptyTitle="No se pudo obtener el resumen"
        emptyDescription="El dashboard requiere que la API esté disponible."
        onRetry={() => setReload((value) => value + 1)}
      >
        {summary && (
          <Stack spacing={3}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <MetricCard label="Almacenes" value={numberFormat.format(summary.almacenes)} caption="Sedes registradas" icon={<StorefrontOutlinedIcon fontSize="small" />} accent="#3980a8" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <MetricCard label="Productos" value={numberFormat.format(summary.productos)} caption="Referencias en catálogo" icon={<WidgetsOutlinedIcon fontSize="small" />} accent="#7158bd" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <MetricCard label="Unidades" value={numberFormat.format(summary.totalUnidades)} caption="Existencias registradas" icon={<Inventory2OutlinedIcon fontSize="small" />} accent="#21866c" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <MetricCard label="Stock bajo" value={numberFormat.format(summary.productosStockBajo)} caption="En o por debajo del mínimo" icon={<WarningAmberRoundedIcon fontSize="small" />} accent="#c27b15" />
              </Grid>
            </Grid>

            <Grid container spacing={2}>
              <Grid size={{ xs: 12, lg: 8 }}>
                <Card sx={{ height: '100%' }}>
                  <CardContent sx={{ p: 2.5 }}>
                    <Stack direction="row" justifyContent="space-between" alignItems="flex-start" mb={2}>
                      <Box>
                        <Typography variant="h6">Unidades por almacén</Typography>
                        <Typography variant="body2" color="text.secondary">Agregado calculado por la API</Typography>
                      </Box>
                      <Chip label="Datos reales" size="small" sx={{ bgcolor: '#eff8f5', color: '#21866c', fontWeight: 650 }} />
                    </Stack>
                    <RequestState
                      loading={false}
                      empty={summary.inventarioPorAlmacen.length === 0}
                      emptyTitle="Sin existencias registradas"
                      emptyDescription="El gráfico aparecerá cuando haya inventario en los almacenes."
                    >
                      <Box sx={{ width: '100%', height: 300 }}>
                        <ResponsiveContainer>
                          <BarChart data={summary.inventarioPorAlmacen} margin={{ top: 8, right: 12, left: -12, bottom: 4 }}>
                            <CartesianGrid stroke="#edf0f2" vertical={false} />
                            <XAxis dataKey="almacen" tickLine={false} axisLine={false} tick={{ fill: '#687684', fontSize: 12 }} />
                            <YAxis tickLine={false} axisLine={false} tick={{ fill: '#687684', fontSize: 12 }} />
                            <Tooltip formatter={(value) => [numberFormat.format(Number(value)), 'Unidades']} />
                            <Bar dataKey="totalUnidades" fill="#22a884" radius={[7, 7, 0, 0]} maxBarSize={54} />
                          </BarChart>
                        </ResponsiveContainer>
                      </Box>
                    </RequestState>
                  </CardContent>
                </Card>
              </Grid>
              <Grid size={{ xs: 12, lg: 4 }}>
                <Card sx={{ height: '100%' }}>
                  <CardContent sx={{ p: 2.5 }}>
                    <Typography variant="h6">Estado por sede</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>Productos en nivel de reposición</Typography>
                    <RequestState
                      loading={false}
                      empty={summary.inventarioPorAlmacen.length === 0}
                      emptyTitle="Sin datos"
                    >
                      <Stack divider={<Box sx={{ borderBottom: '1px solid #edf0f2' }} />} spacing={0}>
                        {summary.inventarioPorAlmacen.map((warehouse) => (
                          <Stack key={warehouse.almacenId} direction="row" justifyContent="space-between" alignItems="center" py={1.7}>
                            <Box>
                              <Typography fontWeight={650}>{warehouse.almacen}</Typography>
                              <Typography variant="caption" color="text.secondary">
                                {numberFormat.format(warehouse.productos)} referencias · {numberFormat.format(warehouse.totalUnidades)} unidades
                              </Typography>
                            </Box>
                            <Chip
                              size="small"
                              label={`${warehouse.stockBajo} bajo`}
                              color={warehouse.stockBajo > 0 ? 'warning' : 'success'}
                              variant={warehouse.stockBajo > 0 ? 'filled' : 'outlined'}
                            />
                          </Stack>
                        ))}
                      </Stack>
                    </RequestState>
                    {summary.productosStockBajo > 0 && (
                      <Alert severity="warning" sx={{ mt: 2 }}>
                        Revisa los niveles de reposición antes de realizar nuevos pedidos.
                      </Alert>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Stack>
        )}
      </RequestState>
    </>
  );
}

import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Button, Card, CardContent, Chip, IconButton, InputAdornment, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TablePagination, TableRow, TextField, Tooltip,
} from '@mui/material';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import { PageHeader } from '../components/common/PageHeader';
import { RequestState } from '../components/common/RequestState';
import { getWarehouses } from '../services/warehouseService';
import type { PageResult, Warehouse } from '../types/api';

export function WarehousesPage() {
  const [result, setResult] = useState<PageResult<Warehouse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [query, setQuery] = useState('');
  const [reload, setReload] = useState(0);

  const load = useCallback(async (signal: AbortSignal) => {
    setLoading(true);
    setError(null);
    try {
      setResult(await getWarehouses(page, size, query, signal));
    } catch (requestError) {
      if (!signal.aborted) setError(requestError instanceof Error ? requestError.message : 'No fue posible cargar los almacenes.');
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [page, query, size]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load, reload]);

  return (
    <>
      <PageHeader title="Almacenes" description="Consulta las sedes y accede al inventario disponible." />
      <Card>
        <CardContent sx={{ p: { xs: 1.5, sm: 2.5 } }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" gap={2} sx={{ mb: 2.5 }}>
            <TextField
              size="small"
              placeholder="Buscar por nombre, código o ciudad"
              value={query}
              onChange={(event) => { setQuery(event.target.value); setPage(0); }}
              sx={{ width: { xs: '100%', sm: 360 } }}
              InputProps={{ startAdornment: <InputAdornment position="start"><SearchRoundedIcon fontSize="small" /></InputAdornment> }}
            />
            <Button component={RouterLink} to="/inventory" variant="contained" startIcon={<Inventory2OutlinedIcon />}>
              Consultar inventario
            </Button>
          </Stack>
          <RequestState
            loading={loading}
            error={error}
            empty={result?.items.length === 0}
            emptyTitle={query ? 'Sin coincidencias' : 'No hay almacenes'}
            emptyDescription={query ? 'Prueba con otro término de búsqueda.' : 'La API no devolvió almacenes.'}
            onRetry={() => setReload((value) => value + 1)}
          >
            {result && (
              <>
                <TableContainer>
                  <Table aria-label="Almacenes">
                    <TableHead>
                      <TableRow>
                        <TableCell>Almacén</TableCell>
                        <TableCell>Código</TableCell>
                        <TableCell>Ubicación</TableCell>
                        <TableCell>Estado</TableCell>
                        <TableCell align="right">Acción</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {result.items.map((warehouse) => (
                        <TableRow key={warehouse.id} hover>
                          <TableCell>
                            <Stack>
                              <strong>{warehouse.nombre}</strong>
                              <span style={{ color: '#687684', fontSize: 13 }}>{warehouse.direccion}</span>
                            </Stack>
                          </TableCell>
                          <TableCell><Chip size="small" label={warehouse.codigoSede} variant="outlined" /></TableCell>
                          <TableCell>{warehouse.ciudad}{warehouse.telefono ? ` · ${warehouse.telefono}` : ''}</TableCell>
                          <TableCell><Chip size="small" label={warehouse.activo ? 'Activo' : 'Inactivo'} color={warehouse.activo ? 'success' : 'default'} /></TableCell>
                          <TableCell align="right">
                            <Tooltip title="Ver inventario">
                              <IconButton
                                component={RouterLink}
                                to={`/warehouses/${warehouse.id}/inventory`}
                                aria-label={`Ver inventario de ${warehouse.nombre}`}
                                color="primary"
                              >
                                <ArrowForwardRoundedIcon />
                              </IconButton>
                            </Tooltip>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
                <TablePagination
                  component="div"
                  count={result.page.totalElements}
                  page={page}
                  rowsPerPage={result.page.size}
                  rowsPerPageOptions={[10, 25, 50]}
                  onPageChange={(_, nextPage) => setPage(nextPage)}
                  onRowsPerPageChange={(event) => { setSize(Number(event.target.value)); setPage(0); }}
                  labelRowsPerPage="Filas por página"
                  labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
                />
              </>
            )}
          </RequestState>
        </CardContent>
      </Card>
    </>
  );
}

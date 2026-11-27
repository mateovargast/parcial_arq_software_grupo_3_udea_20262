import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Button, Card, CardContent, FormControl, InputAdornment, InputLabel, MenuItem,
  Select, Stack, TablePagination, TextField,
} from '@mui/material';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import { InventoryTable } from '../components/tables/InventoryTable';
import { PageHeader } from '../components/common/PageHeader';
import { RequestState } from '../components/common/RequestState';
import { getWarehouseInventory } from '../services/inventoryService';
import { getWarehouse, getWarehouses } from '../services/warehouseService';
import type { PageResult, Warehouse, InventoryItem } from '../types/api';

export function InventoryPage() {
  const { warehouseId } = useParams();
  const navigate = useNavigate();
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [warehouseName, setWarehouseName] = useState('');
  const [result, setResult] = useState<PageResult<InventoryItem> | null>(null);
  const [loading, setLoading] = useState(Boolean(warehouseId));
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [query, setQuery] = useState('');
  const [selectedId, setSelectedId] = useState('');
  const [reload, setReload] = useState(0);

  useEffect(() => {
    if (warehouseId) return;
    const controller = new AbortController();
    void getWarehouses(0, 100, '', controller.signal)
      .then((response) => setWarehouses(response.items))
      .catch((requestError: unknown) => {
        if (!controller.signal.aborted) setError(requestError instanceof Error ? requestError.message : 'No fue posible cargar los almacenes.');
      });
    return () => controller.abort();
  }, [warehouseId]);

  const load = useCallback(async (signal: AbortSignal) => {
    if (!warehouseId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const id = Number(warehouseId);
      const [warehouse, inventory] = await Promise.all([
        getWarehouse(id, signal),
        getWarehouseInventory(id, page, size, query, signal),
      ]);
      setWarehouseName(warehouse.nombre);
      setResult(inventory);
    } catch (requestError) {
      if (!signal.aborted) setError(requestError instanceof Error ? requestError.message : 'No fue posible consultar el inventario.');
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [page, query, size, warehouseId]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load, reload]);

  return (
    <>
      <PageHeader
        title={warehouseName || 'Inventario por almacén'}
        description={warehouseName ? 'Existencias y niveles mínimos de la sede seleccionada.' : 'Selecciona una sede para consultar sus existencias.'}
      />
      {!warehouseId ? (
        <Card sx={{ maxWidth: 720 }}>
          <CardContent sx={{ p: 3 }}>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
              <FormControl fullWidth>
                <InputLabel id="warehouse-select-label">Almacén</InputLabel>
                <Select
                  labelId="warehouse-select-label"
                  label="Almacén"
                  value={selectedId}
                  onChange={(event) => setSelectedId(event.target.value)}
                >
                  {warehouses.map((warehouse) => (
                    <MenuItem key={warehouse.id} value={String(warehouse.id)}>{warehouse.nombre} · {warehouse.codigoSede}</MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Button
                variant="contained"
                disabled={!selectedId}
                onClick={() => navigate(`/warehouses/${selectedId}/inventory`)}
              >
                Ver inventario
              </Button>
            </Stack>
            {error && <RequestState loading={false} error={error} onRetry={() => setReload((value) => value + 1)}><span /></RequestState>}
            {!error && warehouses.length === 0 && !loading && (
              <RequestState loading={false} empty emptyTitle="No hay almacenes disponibles"><></></RequestState>
            )}
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent sx={{ p: { xs: 1.5, sm: 2.5 } }}>
            <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" gap={2} sx={{ mb: 2.5 }}>
              <TextField
                size="small"
                placeholder="Buscar SKU, producto o categoría"
                value={query}
                onChange={(event) => { setQuery(event.target.value); setPage(0); }}
                sx={{ width: { xs: '100%', sm: 350 } }}
                InputProps={{ startAdornment: <InputAdornment position="start"><SearchRoundedIcon fontSize="small" /></InputAdornment> }}
              />
              <Button variant="outlined" onClick={() => navigate('/inventory')}>Cambiar almacén</Button>
            </Stack>
            <RequestState
              loading={loading}
              error={error}
              empty={result?.items.length === 0}
              emptyTitle="Almacén sin productos"
              emptyDescription="No hay existencias que coincidan con esta consulta."
              onRetry={() => setReload((value) => value + 1)}
            >
              {result && (
                <>
                  <InventoryTable items={result.items} />
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
      )}
    </>
  );
}

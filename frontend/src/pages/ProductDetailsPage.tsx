import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink, useParams } from 'react-router-dom';
import {
  Box, Button, Card, CardContent, Chip, Grid, Stack, TablePagination, Typography,
} from '@mui/material';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { InventoryTable } from '../components/tables/InventoryTable';
import { PageHeader } from '../components/common/PageHeader';
import { RequestState } from '../components/common/RequestState';
import { getProductInventory } from '../services/inventoryService';
import { getProduct } from '../services/productService';
import type { InventoryItem, PageResult, Product } from '../types/api';
import { numberFormat } from '../utils/format';

export function ProductDetailsPage() {
  const { productId } = useParams();
  const id = Number(productId);
  const [product, setProduct] = useState<Product | null>(null);
  const [inventory, setInventory] = useState<PageResult<InventoryItem> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [reload, setReload] = useState(0);

  const load = useCallback(async (signal: AbortSignal) => {
    setLoading(true);
    setError(null);
    try {
      const [productResponse, inventoryResponse] = await Promise.all([
        getProduct(id, signal),
        getProductInventory(id, page, size, signal),
      ]);
      setProduct(productResponse);
      setInventory(inventoryResponse);
    } catch (requestError) {
      if (!signal.aborted) setError(requestError instanceof Error ? requestError.message : 'No fue posible cargar el producto.');
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [id, page, size]);

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load, reload]);

  return (
    <>
      <PageHeader
        title={product?.nombre || 'Detalle del producto'}
        description={product ? `Referencia ${product.sku} · ${product.categoria}` : 'Información disponible en el catálogo.'}
        action={<Button component={RouterLink} to="/products" startIcon={<ArrowBackRoundedIcon />}>Volver</Button>}
      />
      <RequestState loading={loading} error={error} empty={!product} emptyTitle="Producto no encontrado" onRetry={() => setReload((value) => value + 1)}>
        {product && inventory && (
          <Stack spacing={2.5}>
            <Card>
              <CardContent sx={{ p: 2.5 }}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 7 }}>
                    <Typography variant="overline" color="text.secondary">DESCRIPCIÓN</Typography>
                    <Typography sx={{ mt: 0.5 }}>{product.descripcion || 'Sin descripción registrada.'}</Typography>
                  </Grid>
                  <Grid size={{ xs: 6, md: 2.5 }}>
                    <Typography variant="overline" color="text.secondary">UNIDAD</Typography>
                    <Typography sx={{ mt: 0.5 }}>{product.unidadMedida}</Typography>
                  </Grid>
                  <Grid size={{ xs: 6, md: 2.5 }}>
                    <Typography variant="overline" color="text.secondary">PRECIO UNITARIO</Typography>
                    <Typography sx={{ mt: 0.5 }}>{numberFormat.format(product.precioUnitario)}</Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
            <Card>
              <CardContent sx={{ p: { xs: 1.5, sm: 2.5 } }}>
                <Stack direction="row" alignItems="center" justifyContent="space-between" mb={2}>
                  <Box>
                    <Typography variant="h6">Existencias por almacén</Typography>
                    <Typography variant="body2" color="text.secondary">Datos actuales, sin historial de movimientos.</Typography>
                  </Box>
                  <Chip label={`${inventory.page.totalElements} sedes`} size="small" variant="outlined" />
                </Stack>
                <RequestState
                  loading={false}
                  empty={inventory.items.length === 0}
                  emptyTitle="Sin existencias"
                  emptyDescription="El producto aún no tiene inventario registrado."
                >
                  <InventoryTable items={inventory.items} />
                  <TablePagination
                    component="div"
                    count={inventory.page.totalElements}
                    page={page}
                    rowsPerPage={inventory.page.size}
                    rowsPerPageOptions={[10, 25, 50]}
                    onPageChange={(_, nextPage) => setPage(nextPage)}
                    onRowsPerPageChange={(event) => { setSize(Number(event.target.value)); setPage(0); }}
                    labelRowsPerPage="Filas por página"
                    labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
                  />
                </RequestState>
              </CardContent>
            </Card>
          </Stack>
        )}
      </RequestState>
    </>
  );
}

import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Button, Card, CardContent, Chip, InputAdornment, Stack, Table, TableBody, TableCell,
  TableContainer, TableHead, TablePagination, TableRow, TextField,
} from '@mui/material';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import { PageHeader } from '../components/common/PageHeader';
import { RequestState } from '../components/common/RequestState';
import { getProducts } from '../services/productService';
import type { PageResult, Product } from '../types/api';
import { numberFormat } from '../utils/format';

export function ProductsPage() {
  const [result, setResult] = useState<PageResult<Product> | null>(null);
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
      setResult(await getProducts(page, size, query, signal));
    } catch (requestError) {
      if (!signal.aborted) setError(requestError instanceof Error ? requestError.message : 'No fue posible cargar los productos.');
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
      <PageHeader
        title="Catálogo de productos"
        description="Referencias, categorías y precios unitarios registrados."
        action={<Button component={RouterLink} to="/products/new" variant="contained" startIcon={<AddRoundedIcon />}>Registrar productos</Button>}
      />
      <Card>
        <CardContent sx={{ p: { xs: 1.5, sm: 2.5 } }}>
          <TextField
            size="small"
            placeholder="Buscar por SKU, nombre o categoría"
            value={query}
            onChange={(event) => { setQuery(event.target.value); setPage(0); }}
            sx={{ width: { xs: '100%', sm: 360 }, mb: 2.5 }}
            InputProps={{ startAdornment: <InputAdornment position="start"><SearchRoundedIcon fontSize="small" /></InputAdornment> }}
          />
          <RequestState
            loading={loading}
            error={error}
            empty={result?.items.length === 0}
            emptyTitle={query ? 'Sin coincidencias' : 'No hay productos'}
            emptyDescription={query ? 'Prueba con otro SKU, nombre o categoría.' : 'Registra productos para verlos en el catálogo.'}
            onRetry={() => setReload((value) => value + 1)}
          >
            {result && (
              <>
                <TableContainer>
                  <Table aria-label="Catálogo de productos">
                    <TableHead>
                      <TableRow>
                        <TableCell>Producto</TableCell>
                        <TableCell>SKU</TableCell>
                        <TableCell>Categoría</TableCell>
                        <TableCell>Unidad</TableCell>
                        <TableCell align="right">Precio unitario</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {result.items.map((product) => (
                        <TableRow key={product.id} hover component={RouterLink} to={`/products/${product.id}`} sx={{ textDecoration: 'none', color: 'inherit' }}>
                          <TableCell>
                            <Stack>
                              <strong>{product.nombre}</strong>
                              <span style={{ color: '#687684', fontSize: 13 }}>{product.descripcion || 'Sin descripción'}</span>
                            </Stack>
                          </TableCell>
                          <TableCell><Chip size="small" label={product.sku} variant="outlined" /></TableCell>
                          <TableCell>{product.categoria}</TableCell>
                          <TableCell>{product.unidadMedida}</TableCell>
                          <TableCell align="right">{numberFormat.format(product.precioUnitario)}</TableCell>
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

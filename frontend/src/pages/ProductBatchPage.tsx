import { useEffect, useState } from 'react';
import { Controller, useFieldArray, useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert, Box, Button, Card, CardContent, Divider, FormControl, FormHelperText, Grid,
  IconButton, InputLabel, MenuItem, Select, Stack, TextField, Typography,
} from '@mui/material';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import { Link as RouterLink } from 'react-router-dom';
import { PageHeader } from '../components/common/PageHeader';
import { getWarehouses } from '../services/warehouseService';
import { registerProductBatch } from '../services/productService';
import type { Warehouse } from '../types/api';

const itemSchema = z.object({
  sku: z.string().trim().min(1, 'El SKU es obligatorio.').max(40, 'Máximo 40 caracteres.'),
  nombre: z.string().trim().min(1, 'El nombre es obligatorio.').max(150, 'Máximo 150 caracteres.'),
  descripcion: z.string().max(500, 'Máximo 500 caracteres.'),
  categoria: z.string().trim().min(1, 'La categoría es obligatoria.').max(80, 'Máximo 80 caracteres.'),
  unidadMedida: z.string().trim().min(1, 'La unidad es obligatoria.').max(20, 'Máximo 20 caracteres.'),
  precioUnitario: z.number().finite().min(0, 'El precio no puede ser negativo.').multipleOf(0.01, 'Usa máximo dos decimales.'),
  cantidadInicial: z.number().int().min(0, 'La cantidad debe ser cero o mayor.'),
  stockMinimo: z.number().int().min(0, 'El mínimo debe ser cero o mayor.'),
});

const formSchema = z.object({
  almacenId: z.number().int().positive('Selecciona un almacén.'),
  productos: z.array(itemSchema).min(1, 'Agrega al menos un producto.').max(100, 'El lote admite máximo 100 productos.'),
});

type BatchForm = z.infer<typeof formSchema>;

const emptyProduct = {
  sku: '',
  nombre: '',
  descripcion: '',
  categoria: '',
  unidadMedida: 'UNIDAD',
  precioUnitario: 0,
  cantidadInicial: 0,
  stockMinimo: 0,
};

export function ProductBatchPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [loadingWarehouses, setLoadingWarehouses] = useState(true);
  const [requestError, setRequestError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const {
    control,
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BatchForm>({
    resolver: zodResolver(formSchema),
    defaultValues: { almacenId: 0, productos: [{ ...emptyProduct }] },
  });
  const { fields, append, remove } = useFieldArray({ control, name: 'productos' });

  useEffect(() => {
    const controller = new AbortController();
    void getWarehouses(0, 100, '', controller.signal)
      .then((response) => setWarehouses(response.items))
      .catch((error: unknown) => {
        if (!controller.signal.aborted) setRequestError(error instanceof Error ? error.message : 'No fue posible cargar los almacenes.');
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoadingWarehouses(false);
      });
    return () => controller.abort();
  }, []);

  const onSubmit = handleSubmit(async (form) => {
    setSubmitting(true);
    setRequestError(null);
    setSuccess(null);
    try {
      const created = await registerProductBatch({
        almacenId: form.almacenId,
        productos: form.productos.map((item) => ({
          ...item,
          descripcion: item.descripcion.trim() || undefined,
        })),
      });
      setSuccess(`Registro completado: ${created.length} producto(s) creados con inventario inicial.`);
    } catch (error) {
      setRequestError(error instanceof Error ? error.message : 'No fue posible registrar el lote.');
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <>
      <PageHeader
        title="Registrar productos"
        description="Crea nuevas referencias y registra su existencia inicial en una sede."
        action={<Button component={RouterLink} to="/products" variant="text">Volver al catálogo</Button>}
      />
      {requestError && <Alert severity="error" sx={{ mb: 2 }}>{requestError}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Card>
        <CardContent sx={{ p: { xs: 2, md: 3 } }}>
          <Stack component="form" onSubmit={onSubmit} spacing={3}>
            <Box>
              <Typography variant="h6">Almacén de destino</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Cada producto se creará con su stock inicial en este almacén.
              </Typography>
              <Controller
                control={control}
                name="almacenId"
                render={({ field }) => (
                  <FormControl sx={{ width: { xs: '100%', sm: 420 } }} error={Boolean(errors.almacenId)} disabled={loadingWarehouses || submitting}>
                    <InputLabel id="batch-warehouse-label">Almacén</InputLabel>
                    <Select
                      {...field}
                      labelId="batch-warehouse-label"
                      label="Almacén"
                      value={field.value || ''}
                      onChange={(event) => field.onChange(Number(event.target.value))}
                    >
                      {warehouses.map((warehouse) => (
                        <MenuItem key={warehouse.id} value={warehouse.id}>{warehouse.nombre} · {warehouse.codigoSede}</MenuItem>
                      ))}
                    </Select>
                    <FormHelperText>{errors.almacenId?.message}</FormHelperText>
                  </FormControl>
                )}
              />
            </Box>

            <Divider />
            <Stack direction={{ xs: 'column', sm: 'row' }} alignItems={{ sm: 'center' }} justifyContent="space-between" gap={1}>
              <Box>
                <Typography variant="h6">Productos del lote</Typography>
                <Typography variant="body2" color="text.secondary">Máximo 100 productos nuevos por envío.</Typography>
              </Box>
              <Button
                variant="outlined"
                startIcon={<AddRoundedIcon />}
                disabled={fields.length >= 100 || submitting}
                onClick={() => append({ ...emptyProduct })}
              >
                Agregar fila
              </Button>
            </Stack>

            {fields.map((field, index) => {
              const productErrors = errors.productos?.[index];
              return (
                <Card key={field.id} variant="outlined" sx={{ boxShadow: 'none', bgcolor: '#fbfcfd' }}>
                  <CardContent sx={{ p: { xs: 1.5, md: 2.5 } }}>
                    <Stack direction="row" alignItems="center" justifyContent="space-between" mb={2}>
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Inventory2OutlinedIcon color="primary" fontSize="small" />
                        <Typography fontWeight={700}>Producto {index + 1}</Typography>
                      </Stack>
                      <IconButton
                        aria-label={`Eliminar producto ${index + 1}`}
                        color="error"
                        disabled={fields.length === 1 || submitting}
                        onClick={() => remove(index)}
                      >
                        <DeleteOutlineRoundedIcon />
                      </IconButton>
                    </Stack>
                    <Grid container spacing={2}>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField fullWidth label="SKU *" disabled={submitting} {...register(`productos.${index}.sku`)} error={Boolean(productErrors?.sku)} helperText={productErrors?.sku?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 8 }}>
                        <TextField fullWidth label="Nombre *" disabled={submitting} {...register(`productos.${index}.nombre`)} error={Boolean(productErrors?.nombre)} helperText={productErrors?.nombre?.message} />
                      </Grid>
                      <Grid size={{ xs: 12 }}>
                        <TextField fullWidth label="Descripción" disabled={submitting} {...register(`productos.${index}.descripcion`)} error={Boolean(productErrors?.descripcion)} helperText={productErrors?.descripcion?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField fullWidth label="Categoría *" disabled={submitting} {...register(`productos.${index}.categoria`)} error={Boolean(productErrors?.categoria)} helperText={productErrors?.categoria?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField fullWidth label="Unidad de medida *" disabled={submitting} {...register(`productos.${index}.unidadMedida`)} error={Boolean(productErrors?.unidadMedida)} helperText={productErrors?.unidadMedida?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <TextField fullWidth type="number" label="Precio unitario *" disabled={submitting} inputProps={{ min: 0, step: 0.01 }} {...register(`productos.${index}.precioUnitario`, { valueAsNumber: true })} error={Boolean(productErrors?.precioUnitario)} helperText={productErrors?.precioUnitario?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <TextField fullWidth type="number" label="Cantidad inicial *" disabled={submitting} inputProps={{ min: 0, step: 1 }} {...register(`productos.${index}.cantidadInicial`, { valueAsNumber: true })} error={Boolean(productErrors?.cantidadInicial)} helperText={productErrors?.cantidadInicial?.message} />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <TextField fullWidth type="number" label="Stock mínimo *" disabled={submitting} inputProps={{ min: 0, step: 1 }} {...register(`productos.${index}.stockMinimo`, { valueAsNumber: true })} error={Boolean(productErrors?.stockMinimo)} helperText={productErrors?.stockMinimo?.message} />
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              );
            })}

            <Alert severity="info">
              El envío es transaccional. Los SKU existentes o repetidos producen un conflicto y no alteran existencias.
            </Alert>
            <Stack direction={{ xs: 'column-reverse', sm: 'row' }} justifyContent="flex-end" spacing={1.5}>
              <Button component={RouterLink} to="/products" disabled={submitting}>Cancelar</Button>
              <Button type="submit" variant="contained" disabled={submitting || loadingWarehouses || warehouses.length === 0}>
                {submitting ? 'Registrando…' : 'Registrar lote'}
              </Button>
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    </>
  );
}

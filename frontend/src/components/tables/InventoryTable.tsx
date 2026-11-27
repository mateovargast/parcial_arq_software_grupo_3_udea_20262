import {
  Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
} from '@mui/material';
import type { InventoryItem } from '../../types/api';
import { numberFormat } from '../../utils/format';

interface InventoryTableProps {
  items: InventoryItem[];
}

export function InventoryTable({ items }: InventoryTableProps) {
  return (
    <TableContainer>
      <Table aria-label="Existencias">
        <TableHead>
          <TableRow>
            <TableCell>Producto</TableCell>
            <TableCell>SKU</TableCell>
            <TableCell>Categoría</TableCell>
            <TableCell align="right">Disponible</TableCell>
            <TableCell align="right">Mínimo</TableCell>
            <TableCell>Estado</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => {
            const lowStock = item.cantidad <= item.stockMinimo;
            return (
              <TableRow key={item.id} hover>
                <TableCell>
                  <strong>{item.producto.nombre}</strong>
                  <div style={{ color: '#687684', fontSize: 13 }}>{item.producto.unidadMedida}</div>
                </TableCell>
                <TableCell>{item.producto.sku}</TableCell>
                <TableCell>{item.producto.categoria}</TableCell>
                <TableCell align="right"><strong>{numberFormat.format(item.cantidad)}</strong></TableCell>
                <TableCell align="right">{numberFormat.format(item.stockMinimo)}</TableCell>
                <TableCell>
                  <Chip size="small" label={lowStock ? 'Reponer' : 'Disponible'} color={lowStock ? 'warning' : 'success'} variant={lowStock ? 'filled' : 'outlined'} />
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

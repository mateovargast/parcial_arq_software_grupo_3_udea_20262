import { api, readHalPage } from './api';
import type { HalResource, InventoryItem, PageResult } from '../types/api';

export async function getWarehouseInventory(
  warehouseId: number,
  page: number,
  size: number,
  query: string,
  signal?: AbortSignal,
): Promise<PageResult<InventoryItem>> {
  const response = await api.get<HalResource>(`/warehouses/${warehouseId}/inventory`, {
    params: { page, size, q: query || undefined },
    signal,
  });
  return readHalPage<InventoryItem>(response.data);
}

export async function getProductInventory(
  productId: number,
  page: number,
  size: number,
  signal?: AbortSignal,
): Promise<PageResult<InventoryItem>> {
  const response = await api.get<HalResource>(`/products/${productId}/inventory`, {
    params: { page, size },
    signal,
  });
  return readHalPage<InventoryItem>(response.data);
}

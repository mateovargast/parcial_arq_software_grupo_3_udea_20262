import { api, readHalPage } from './api';
import type { HalResource, PageResult, Warehouse } from '../types/api';

export async function getWarehouses(
  page: number,
  size: number,
  query = '',
  signal?: AbortSignal,
): Promise<PageResult<Warehouse>> {
  const response = await api.get<HalResource>('/warehouses', {
    params: { page, size, q: query || undefined },
    signal,
  });
  return readHalPage<Warehouse>(response.data);
}

export async function getWarehouse(id: number, signal?: AbortSignal): Promise<Warehouse> {
  const response = await api.get<Warehouse>(`/warehouses/${id}`, { signal });
  return response.data;
}

import { api, readHalCollection, readHalPage } from './api';
import type { HalResource, PageResult, Product, ProductBatch } from '../types/api';

export async function getProducts(
  page: number,
  size: number,
  query = '',
  signal?: AbortSignal,
): Promise<PageResult<Product>> {
  const response = await api.get<HalResource>('/products', {
    params: { page, size, q: query || undefined },
    signal,
  });
  return readHalPage<Product>(response.data);
}

export async function getProduct(id: number, signal?: AbortSignal): Promise<Product> {
  const response = await api.get<Product>(`/products/${id}`, { signal });
  return response.data;
}

export async function registerProductBatch(batch: ProductBatch): Promise<Product[]> {
  const response = await api.post<HalResource>('/products/batch', batch);
  return readHalCollection<Product>(response.data);
}

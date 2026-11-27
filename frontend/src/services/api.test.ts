import { describe, expect, it } from 'vitest';
import { readHalItems, readHalPage } from './api';
import type { HalResource } from '../types/api';

describe('HAL response parsing', () => {
  it('reads the embedded collection without assuming its relation name', () => {
    const response: HalResource = {
      _embedded: { warehouses: [{ id: 7, nombre: 'Central' }] },
      page: { number: 0, size: 10, totalElements: 1, totalPages: 1 },
    };

    expect(readHalItems<{ id: number }>(response)).toEqual([{ id: 7, nombre: 'Central' }]);
  });

  it('preserves server pagination metadata', () => {
    const response: HalResource = {
      _embedded: { products: [{ id: 4 }] },
      page: { number: 2, size: 10, totalElements: 25, totalPages: 3 },
    };

    expect(readHalPage<{ id: number }>(response).page).toEqual({
      number: 2,
      size: 10,
      totalElements: 25,
      totalPages: 3,
    });
  });

  it('rejects a response without HAL page metadata', () => {
    expect(() => readHalPage({ _embedded: { products: [] } })).toThrow(/paginación/);
  });
});

import axios, { AxiosError } from 'axios';
import type { ApiProblem, HalResource, PageResult } from '../types/api';

export class ApiError extends Error {
  readonly status?: number;
  readonly fieldErrors: Record<string, string>;

  constructor(message: string, status?: number, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    Accept: 'application/hal+json, application/json',
    'Content-Type': 'application/json',
    'X-API-Version': '1',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiProblem>) => {
    if (!error.response) {
      return Promise.reject(new ApiError('No fue posible conectar con el servidor.'));
    }

    const body = error.response.data;
    return Promise.reject(new ApiError(
      body?.detail || body?.title || 'La solicitud no pudo completarse.',
      error.response.status,
      body?.errors ?? {},
    ));
  },
);

export function readHalItems<T>(resource: HalResource): T[] {
  const embedded = resource._embedded;
  if (!embedded) return [];

  for (const value of Object.values(embedded)) {
    if (Array.isArray(value)) return value as T[];
  }
  return [];
}

export function readHalCollection<T>(resource: HalResource): T[] {
  return readHalItems<T>(resource);
}

export function readHalPage<T>(resource: HalResource): PageResult<T> {
  if (!resource.page) {
    throw new ApiError('La respuesta del servidor no contiene metadatos de paginación.');
  }
  return {
    items: readHalItems<T>(resource),
    page: resource.page,
    links: resource._links,
  };
}

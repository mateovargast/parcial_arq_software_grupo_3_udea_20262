import { api } from './api';
import type { DashboardSummary } from '../types/api';

export async function getDashboardSummary(signal?: AbortSignal): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard/summary', { signal });
  return response.data;
}

export interface HalLink {
  href: string;
  templated?: boolean;
}

export interface HalResource {
  _links?: Record<string, HalLink | HalLink[]>;
  _embedded?: Record<string, unknown>;
  page?: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
}

export interface PageResult<T> {
  items: T[];
  page: NonNullable<HalResource['page']>;
  links: HalResource['_links'];
}

export interface Warehouse {
  id: number;
  codigoSede: string;
  nombre: string;
  ciudad: string;
  direccion: string;
  telefono: string | null;
  activo: boolean;
}

export interface Product {
  id: number;
  sku: string;
  nombre: string;
  descripcion: string | null;
  categoria: string;
  unidadMedida: string;
  precioUnitario: number;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface InventoryItem {
  id: number;
  almacenId: number;
  productoId: number;
  producto: Product;
  cantidad: number;
  stockMinimo: number;
  fechaIngreso: string | null;
  updatedAt: string | null;
}

export interface WarehouseSummary {
  almacenId: number;
  almacen: string;
  totalUnidades: number;
  productos: number;
  stockBajo: number;
}

export interface DashboardSummary {
  almacenes: number;
  productos: number;
  totalUnidades: number;
  productosStockBajo: number;
  inventarioPorAlmacen: WarehouseSummary[];
}

export interface BatchItem {
  sku: string;
  nombre: string;
  descripcion?: string;
  categoria: string;
  unidadMedida: string;
  precioUnitario: number;
  cantidadInicial: number;
  stockMinimo: number;
}

export interface ProductBatch {
  almacenId: number;
  productos: BatchItem[];
}

export interface ApiProblem {
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string>;
}

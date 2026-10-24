export type ProductCategory = "HEALTH" | "LIFE" | "VEHICLE" | "TRAVEL";
export type ProductStatus = "ACTIVE" | "INACTIVE";

export interface ProductResponse {
  productId: string;
  productCode: string;
  name: string;
  description: string | null;
  category: ProductCategory;
  coverageDescription: string;
  coverageAmount: number;
  premiumAmount: number;
  tenureMonths: number;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ProductPageResponse {
  products: ProductResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ProductComparisonResponse {
  products: ProductResponse[];
}
export interface ProductFilters {
  name?: string;
  productCode?: string;
  category?: ProductCategory;
  status?: ProductStatus;
  page: number;
  size: number;
  sort: string;
}
export interface CreateProductRequest {
  productCode: string;
  name: string;
  description: string;
  category: ProductCategory;
  coverageDescription: string;
  coverageAmount: number;
  premiumAmount: number;
  tenureMonths: number;
}
export interface UpdateProductRequest {
  name: string;
  description: string;
  category: ProductCategory;
  coverageDescription: string;
  coverageAmount: number;
  premiumAmount: number;
  tenureMonths: number;
  status: ProductStatus;
}

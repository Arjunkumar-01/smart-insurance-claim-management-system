import { z } from "zod";

const money = z.coerce.number().min(0.01, "Amount must be greater than 0.");
const common = {
  name: z.string().trim().min(1, "Name is required.").max(150),
  description: z.string().max(2000),
  category: z.enum(["HEALTH", "LIFE", "VEHICLE", "TRAVEL"]),
  coverageDescription: z
    .string()
    .trim()
    .min(1, "Coverage description is required.")
    .max(2000),
  coverageAmount: money,
  premiumAmount: money,
  tenureMonths: z.coerce
    .number()
    .int("Tenure must use whole months.")
    .positive("Tenure must be greater than 0."),
};
export const createProductSchema = z.object({
  productCode: z.string().trim().min(1, "Product code is required.").max(20),
  ...common,
});
export const updateProductSchema = z.object({
  ...common,
  status: z.enum(["ACTIVE", "INACTIVE"]),
});
export const productFormSchema = z.object({
  ...common,
  productCode: z.string().max(20),
  status: z.enum(["ACTIVE", "INACTIVE"]).optional(),
});
export type ProductFormValues = z.infer<typeof productFormSchema>;
export type CreateProductFormValues = z.infer<typeof createProductSchema>;
export type UpdateProductFormValues = z.infer<typeof updateProductSchema>;

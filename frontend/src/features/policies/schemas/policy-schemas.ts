import { z } from "zod";
const nominee = {
  name: z.string().trim().min(1, "Name is required.").max(150),
  relationship: z.string().trim().min(1, "Relationship is required.").max(80),
  contactNumber: z
    .string()
    .trim()
    .min(7, "Enter a valid contact number.")
    .max(30)
    .regex(/^[+0-9() .-]+$/, "Enter a valid contact number."),
};
export const purchaseSchema = z.object({
  productId: z.string().uuid(),
  startDate: z.string().min(1, "Start date is required."),
  nominee: z.object(nominee).optional(),
});
export const nomineeSchema = z.object(nominee);
export const renewalSchema = z.object({});
export const cancellationSchema = z.object({ reason: z.string().max(1000) });
export const decisionSchema = z.object({
  status: z.enum(["APPROVED", "REJECTED"]),
  decisionComment: z.string(),
});

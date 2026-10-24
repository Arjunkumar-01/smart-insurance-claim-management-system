import { z } from "zod";

export const customerKycSchema = z.object({
  identityType: z
    .string()
    .trim()
    .min(1, "Identity type is required.")
    .max(50, "Identity type must be 50 characters or fewer."),
  identityNumber: z
    .string()
    .trim()
    .min(1, "Identity number is required.")
    .max(100, "Identity number must be 100 characters or fewer."),
});

export type CustomerKycFormValues = z.infer<typeof customerKycSchema>;

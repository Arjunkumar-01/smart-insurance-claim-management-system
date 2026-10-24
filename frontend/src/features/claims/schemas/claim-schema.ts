import { z } from "zod";
export const claimSchema = z.object({
  policyId: z.string().uuid(),
  claimAmount: z.coerce
    .number()
    .min(0.01, "Claim amount must be greater than 0."),
  claimType: z.enum(["HEALTH", "LIFE", "VEHICLE", "TRAVEL"]),
  incidentDate: z.string().min(1, "Incident date is required."),
  description: z.string().trim().min(1, "Description is required.").max(2000),
});

import { z } from "zod";
export const paymentSchema = z.object({
  paymentMethod: z.enum(["CARD", "BANK_TRANSFER", "UPI"]),
});

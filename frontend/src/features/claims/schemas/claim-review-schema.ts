import { z } from "zod";
export const reviewNotesSchema = z.object({ notes: z.string().max(2000) });
export const settlementNotesSchema = z.object({ notes: z.string().max(1000) });

import { z } from "zod";

export const customerProfileSchema = z.object({
  phoneNumber: z
    .string()
    .trim()
    .min(7, "Enter a valid phone number.")
    .max(30, "Phone number must be 30 characters or fewer.")
    .regex(/^[+0-9() .-]+$/, "Enter a valid phone number."),
  dateOfBirth: z
    .string()
    .refine(
      (value) => !value || value < new Date().toISOString().slice(0, 10),
      "Date of birth must be in the past.",
    ),
  addressLine1: z
    .string()
    .trim()
    .min(1, "Address line 1 is required.")
    .max(200, "Address line 1 must be 200 characters or fewer."),
  addressLine2: z
    .string()
    .max(200, "Address line 2 must be 200 characters or fewer."),
  city: z
    .string()
    .trim()
    .min(1, "City is required.")
    .max(100, "City must be 100 characters or fewer."),
  state: z
    .string()
    .trim()
    .min(1, "State is required.")
    .max(100, "State must be 100 characters or fewer."),
  postalCode: z
    .string()
    .trim()
    .min(1, "Postal code is required.")
    .max(20, "Postal code must be 20 characters or fewer."),
  country: z
    .string()
    .trim()
    .min(1, "Country is required.")
    .max(100, "Country must be 100 characters or fewer."),
});

export type CustomerProfileFormValues = z.infer<typeof customerProfileSchema>;

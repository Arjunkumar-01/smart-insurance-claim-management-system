import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().trim().email("Enter a valid email address."),
  password: z.string().min(1, "Password is required."),
});

export const registerSchema = z
  .object({
    firstName: z
      .string()
      .trim()
      .min(1, "First name is required.")
      .max(100, "First name must be 100 characters or fewer."),
    lastName: z
      .string()
      .trim()
      .min(1, "Last name is required.")
      .max(100, "Last name must be 100 characters or fewer."),
    email: z
      .string()
      .trim()
      .email("Enter a valid email address.")
      .max(254, "Email must be 254 characters or fewer."),
    password: z
      .string()
      .min(8, "Password must be at least 8 characters.")
      .max(128, "Password must be 128 characters or fewer.")
      .regex(/[a-z]/, "Password must contain a lowercase letter.")
      .regex(/[A-Z]/, "Password must contain an uppercase letter.")
      .regex(/\d/, "Password must contain a number.")
      .regex(/[^A-Za-z\d]/, "Password must contain a special character."),
    confirmPassword: z.string().min(1, "Confirm your password."),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "Passwords must match.",
  });

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;

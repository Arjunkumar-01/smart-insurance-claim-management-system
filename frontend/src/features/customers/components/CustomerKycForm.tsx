import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, TextField } from "@mui/material";
import { useForm } from "react-hook-form";
import {
  customerKycSchema,
  type CustomerKycFormValues,
} from "../schemas/customer-kyc-schema";

export function CustomerKycForm({
  pending,
  onSubmit,
}: {
  pending?: boolean;
  onSubmit: (values: CustomerKycFormValues) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CustomerKycFormValues>({
    resolver: zodResolver(customerKycSchema),
    defaultValues: { identityType: "", identityNumber: "" },
  });
  return (
    <Box
      component="form"
      noValidate
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2, maxWidth: 620 }}
    >
      <TextField
        label="Identity type"
        {...register("identityType")}
        error={Boolean(errors.identityType)}
        helperText={errors.identityType?.message}
      />
      <TextField
        label="Identity number"
        type="password"
        autoComplete="off"
        {...register("identityNumber")}
        error={Boolean(errors.identityNumber)}
        helperText={
          errors.identityNumber?.message ??
          "This value is used only for submission and is never stored in the browser."
        }
      />
      <Button type="submit" variant="contained" disabled={pending}>
        {pending ? "Submitting..." : "Submit KYC information"}
      </Button>
    </Box>
  );
}

import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, TextField } from "@mui/material";
import { useForm } from "react-hook-form";
import type {
  CustomerProfileResponse,
  CustomerProfileRequest,
} from "../types/customer-types";
import {
  customerProfileSchema,
  type CustomerProfileFormValues,
} from "../schemas/customer-profile-schema";

export function CustomerProfileForm({
  profile,
  pending,
  onCancel,
  onSubmit,
}: {
  profile?: CustomerProfileResponse;
  pending?: boolean;
  onCancel?: () => void;
  onSubmit: (values: CustomerProfileRequest) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<CustomerProfileFormValues>({
    resolver: zodResolver(customerProfileSchema),
    defaultValues: {
      phoneNumber: profile?.phoneNumber ?? "",
      dateOfBirth: profile?.dateOfBirth ?? "",
      addressLine1: profile?.addressLine1 ?? "",
      addressLine2: profile?.addressLine2 ?? "",
      city: profile?.city ?? "",
      state: profile?.state ?? "",
      postalCode: profile?.postalCode ?? "",
      country: profile?.country ?? "",
    },
  });
  return (
    <Box
      component="form"
      noValidate
      onSubmit={handleSubmit((values) => onSubmit(values))}
      sx={{ display: "grid", gap: 3 }}
    >
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
          gap: 2,
        }}
      >
        <TextField
          label="Phone number"
          {...register("phoneNumber")}
          error={Boolean(errors.phoneNumber)}
          helperText={errors.phoneNumber?.message}
        />
        <TextField
          label="Date of birth"
          type="date"
          InputLabelProps={{ shrink: true }}
          {...register("dateOfBirth")}
          error={Boolean(errors.dateOfBirth)}
          helperText={errors.dateOfBirth?.message}
        />
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
          gap: 2,
        }}
      >
        <TextField
          label="Address line 1"
          {...register("addressLine1")}
          error={Boolean(errors.addressLine1)}
          helperText={errors.addressLine1?.message}
        />
        <TextField
          label="Address line 2"
          {...register("addressLine2")}
          error={Boolean(errors.addressLine2)}
          helperText={errors.addressLine2?.message}
        />
        <TextField
          label="City"
          {...register("city")}
          error={Boolean(errors.city)}
          helperText={errors.city?.message}
        />
        <TextField
          label="State"
          {...register("state")}
          error={Boolean(errors.state)}
          helperText={errors.state?.message}
        />
        <TextField
          label="Postal code"
          {...register("postalCode")}
          error={Boolean(errors.postalCode)}
          helperText={errors.postalCode?.message}
        />
        <TextField
          label="Country"
          {...register("country")}
          error={Boolean(errors.country)}
          helperText={errors.country?.message}
        />
      </Box>
      <Box
        sx={{
          display: "flex",
          gap: 1,
          justifyContent: "flex-end",
          flexWrap: "wrap",
        }}
      >
        <Button type="button" onClick={onCancel} disabled={pending}>
          Cancel
        </Button>
        <Button
          type="submit"
          variant="contained"
          disabled={pending || Boolean(profile && !isDirty)}
        >
          {pending ? "Saving..." : profile ? "Save changes" : "Create profile"}
        </Button>
      </Box>
    </Box>
  );
}

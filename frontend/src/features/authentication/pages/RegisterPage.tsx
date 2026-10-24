import {
  Alert,
  Box,
  Button,
  Divider,
  IconButton,
  InputAdornment,
  Link as MuiLink,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { ApiError } from "../../../api/api-error";
import { register as registerAccount } from "../api/auth-api";
import {
  registerSchema,
  type RegisterFormValues,
} from "../schemas/auth-schemas";

export function RegisterPage() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async (values: RegisterFormValues) => {
    setSubmitError(null);
    setIsSubmitting(true);
    try {
      await registerAccount(values);
      navigate("/login", {
        replace: true,
        state: { message: "Registration complete. Sign in to continue." },
      });
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fieldErrors).forEach(([field, message]) => {
          if (
            [
              "firstName",
              "lastName",
              "email",
              "password",
              "confirmPassword",
            ].includes(field)
          )
            setError(field as keyof RegisterFormValues, { message });
        });
        setSubmitError(error.message);
      } else {
        setSubmitError("Unable to create your account. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          md: "minmax(360px, 0.9fr) minmax(460px, 1.1fr)",
        },
        bgcolor: "background.default",
      }}
    >
      <Box
        sx={{
          display: { xs: "none", md: "flex" },
          bgcolor: "#071A33",
          color: "#FFFFFF",
          p: 6,
          alignItems: "flex-end",
        }}
      >
        <Box sx={{ maxWidth: 420 }}>
          <Typography
            variant="overline"
            sx={{ color: "#06B6D4", letterSpacing: 0 }}
          >
            Customer registration
          </Typography>
          <Typography
            variant="h1"
            sx={{
              mt: 1,
              color: "#FFFFFF",
              fontSize: { xs: "2.2rem", lg: "4rem" },
            }}
          >
            Start with a clear view of your coverage.
          </Typography>
          <Typography sx={{ mt: 2, color: "#94A3B8" }}>
            Create your customer account to begin.
          </Typography>
        </Box>
      </Box>
      <Box sx={{ display: "grid", placeItems: "center", p: { xs: 3, sm: 6 } }}>
        <Paper
          component="main"
          elevation={0}
          sx={{
            width: "100%",
            maxWidth: 520,
            p: { xs: 3, sm: 5 },
            border: 1,
            borderColor: "divider",
          }}
        >
          <Typography variant="overline" color="primary.main">
            SICMS customer account
          </Typography>
          <Typography variant="h2" sx={{ mt: 1, fontSize: "2rem" }}>
            Create your account
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Registration creates a customer account only.
          </Typography>
          {submitError ? (
            <Alert severity="error" role="alert" sx={{ mt: 3 }}>
              {submitError}
            </Alert>
          ) : null}
          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
            sx={{ mt: 3, display: "grid", gap: 2 }}
          >
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
                gap: 2,
              }}
            >
              <TextField
                label="First name"
                autoComplete="given-name"
                {...register("firstName")}
                error={Boolean(errors.firstName)}
                helperText={errors.firstName?.message}
              />
              <TextField
                label="Last name"
                autoComplete="family-name"
                {...register("lastName")}
                error={Boolean(errors.lastName)}
                helperText={errors.lastName?.message}
              />
            </Box>
            <TextField
              label="Email"
              type="email"
              autoComplete="email"
              {...register("email")}
              error={Boolean(errors.email)}
              helperText={errors.email?.message}
            />
            <TextField
              label="Password"
              type={showPassword ? "text" : "password"}
              autoComplete="new-password"
              {...register("password")}
              error={Boolean(errors.password)}
              helperText={
                errors.password?.message ??
                "Use 8-128 characters with upper, lower, number, and special character."
              }
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      type="button"
                      onClick={() => setShowPassword((visible) => !visible)}
                      aria-label={
                        showPassword ? "Hide password" : "Show password"
                      }
                      edge="end"
                    >
                      {showPassword ? "Hide" : "Show"}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              label="Confirm password"
              type={showConfirmation ? "text" : "password"}
              autoComplete="new-password"
              {...register("confirmPassword")}
              error={Boolean(errors.confirmPassword)}
              helperText={errors.confirmPassword?.message}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      type="button"
                      onClick={() => setShowConfirmation((visible) => !visible)}
                      aria-label={
                        showConfirmation
                          ? "Hide confirmation"
                          : "Show confirmation"
                      }
                      edge="end"
                    >
                      {showConfirmation ? "Hide" : "Show"}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating account..." : "Create customer account"}
            </Button>
          </Box>
          <Divider sx={{ my: 3 }} />
          <Typography variant="body2" color="text.secondary">
            Already registered?{" "}
            <MuiLink component={Link} to="/login">
              Sign in
            </MuiLink>
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
}

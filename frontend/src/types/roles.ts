export const APPLICATION_ROLES = [
  "CUSTOMER",
  "AGENT",
  "CLAIMS_OFFICER",
  "ADMINISTRATOR",
] as const;

export type ApplicationRole = (typeof APPLICATION_ROLES)[number];

export function getLandingPath(roles: ApplicationRole[]): string {
  const priority: ApplicationRole[] = [
    "ADMINISTRATOR",
    "CLAIMS_OFFICER",
    "AGENT",
    "CUSTOMER",
  ];
  const role =
    priority.find((candidate) => roles.includes(candidate)) ?? "CUSTOMER";
  return `/app/${role.toLowerCase().replaceAll("_", "-")}`;
}

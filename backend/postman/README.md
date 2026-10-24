# SICMS Postman Workspace

Import:

- `SICMS.postman_collection.json`
- `SICMS-local.postman_environment.json`

The collection covers Authentication, Customer Profile/KYC, Products, Policies, Claims and documents, Claims Officer review/settlement, Payments and Receipt download, Reports, Administration, and Audit.

## Local Use

1. Start the backend at `http://localhost:8080`.
2. Register a synthetic Customer with a locally chosen password that satisfies the API policy.
3. Run Login and copy the returned token to the role-appropriate environment variable when testing Customer or privileged-role folders.
4. Privileged local accounts require environment-enabled backend bootstrapping as documented in the backend README.
5. Select local upload files interactively for multipart document requests.

All committed token and password variables are blank. Examples use synthetic `example.test` identities. Never commit or export a populated Postman environment, active JWT, real password, Customer identity, or local sensitive file path.

## API Endpoints

### Hello

- Method: GET
- Path: /
- Auth: none
- Description: Health greeting endpoint. Returns a simple message.

### Database

- Method: GET
- Path: /api/database/test
- Auth: none
- Description: Tests DB connectivity.

- Method: GET
- Path: /api/database/info
- Auth: none
- Description: Returns DB metadata and connection info.

- Method: GET
- Path: /api/database/tables
- Auth: none
- Description: Lists available database tables.

- Method: GET
- Path: /api/database/health
- Auth: none
- Description: Returns database health status.

- Method: GET
- Path: /api/database/pool-stats
- Auth: none
- Description: Returns connection pool statistics.

### User

- Method: GET
- Path: /user/profile
- Auth: Bearer token (Authorization header)
- Description: Returns the authenticated user's profile, including fields: `userId`, `firstName`, `lastName`, `isActive`, `createdAt`, `updatedAt`, `kindeUserId`, `maxStorage`, `autoSave`, `autoSaveDuration`.

- Method: POST
- Path: /user/profile
- Auth: Bearer token
- Body (JSON): { "firstName": string, "lastName": string }
- Description: Creates a profile for the authenticated user. Both `firstName` and `lastName` are required.

- Method: PUT
- Path: /user/profile
- Auth: Bearer token
- Body (JSON): any subset of { "firstName": string, "lastName": string, "autoSave": boolean, "autoSaveDuration": integer }
- Description: Partially updates the user's profile. At least one field must be provided. `maxStorage` cannot be changed via API.

### Notes

- Method: POST
- Path: /notes
- Auth: Bearer token
- Body (multipart/form-data): file (binary), filename (string)
- Description: Uploads a new note file (HTML). Enforces storage quota against `maxStorage`. Returns 413 if limit exceeded.

- Method: PUT
- Path: /notes
- Auth: Bearer token
- Body (multipart/form-data): file (binary), filename (string)
- Description: Replaces an existing note's file content (by `filename`). Enforces storage quota considering previous file size. Returns 413 if limit exceeded.

- Method: GET
- Path: /notes
- Auth: Bearer token
- Description: Lists all notes for the authenticated user.

- Method: GET
- Path: /notes/info
- Auth: Bearer token
- Query: filename (string; URL-encoded full path)
- Description: Returns metadata for a given note owned by the user.

- Method: GET
- Path: /notes/content
- Auth: Bearer token
- Query: filename (string; URL-encoded full path)
- Response Content-Type: text/html; charset=UTF-8
- Description: Returns the HTML content of the note from storage.

- Method: DELETE
- Path: /notes
- Auth: Bearer token
- Query: filename (string; URL-encoded full path)
- Description: Deletes the note and the backing object from storage.

- Method: POST
- Path: /notes/rename
- Auth: Bearer token
- Query: oldFilename (string; URL-encoded full path), newFilename (string; URL-encoded full path)
- Description: Renames the note's `fileName` only. Does not change the storage object key.

- Method: GET
- Path: /notes/storage/size
- Auth: Bearer token
- Description: Returns total storage used (bytes) and note count for the user, plus human-readable size breakdown.

- Method: GET
- Path: /notes/structure
- Auth: Bearer token
- Description: Returns a hierarchical structure of the user's files. Only folder and file names are included.

### Notes

- Filenames may include `/`. Always URL-encode `filename`, `oldFilename`, and `newFilename` in query parameters.
- Storage quota is enforced using `maxStorage` from the user's profile.

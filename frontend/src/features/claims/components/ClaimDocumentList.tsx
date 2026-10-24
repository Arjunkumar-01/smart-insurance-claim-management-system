import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import type { ClaimDocumentResponse } from "../types/claim-types";
export function ClaimDocumentList({
  documents,
  onDownload,
}: {
  documents: ClaimDocumentResponse[];
  onDownload: (document: ClaimDocumentResponse) => void;
}) {
  return (
    <DataTableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>File name</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Size</TableCell>
            <TableCell>Uploaded</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {documents.map((document) => (
            <TableRow key={document.documentId}>
              <TableCell>{document.originalFileName}</TableCell>
              <TableCell>{document.contentType}</TableCell>
              <TableCell>{formatBytes(document.fileSize)}</TableCell>
              <TableCell>{document.uploadedAt}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => onDownload(document)}>
                  Download
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </DataTableContainer>
  );
}
function formatBytes(value: number) {
  return value < 1024 * 1024
    ? `${(value / 1024).toFixed(1)} KB`
    : `${(value / (1024 * 1024)).toFixed(1)} MB`;
}

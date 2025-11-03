import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  TextField,
  InputAdornment,
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Download as DownloadIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  InsertDriveFile as FileIcon,
} from '@mui/icons-material';

interface Document {
  id: string;
  fileName: string;
  entityType: string;
  entityName: string;
  size: number;
  uploadedBy: string;
  uploadedAt: string;
}

const Documents: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  const documents: Document[] = [
    {
      id: '1',
      fileName: 'proje_plani.pdf',
      entityType: 'PROJECT',
      entityName: 'Bahçeşehir Konut Projesi',
      size: 2456789,
      uploadedBy: 'Ahmet Yılmaz',
      uploadedAt: '2024-11-20',
    },
    {
      id: '2',
      fileName: 'malzeme_listesi.xlsx',
      entityType: 'TASK',
      entityName: 'Malzeme temini',
      size: 45678,
      uploadedBy: 'Mehmet Demir',
      uploadedAt: '2024-11-22',
    },
  ];

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const getEntityColor = (entityType: string) => {
    const colors: any = {
      PROJECT: 'primary',
      TASK: 'success',
      ISSUE: 'warning',
      UNIT: 'info',
      COMPANY: 'secondary',
    };
    return colors[entityType] || 'default';
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Dökümanlar
        </Typography>
        <Button variant="contained" startIcon={<UploadIcon />}>
          Dosya Yükle
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Döküman ara..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Dosya Adı</strong></TableCell>
              <TableCell><strong>Varlık</strong></TableCell>
              <TableCell><strong>Boyut</strong></TableCell>
              <TableCell><strong>Yükleyen</strong></TableCell>
              <TableCell><strong>Tarih</strong></TableCell>
              <TableCell align="right"><strong>İşlemler</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {documents.map((doc) => (
              <TableRow key={doc.id} hover>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FileIcon color="action" />
                    {doc.fileName}
                  </Box>
                </TableCell>
                <TableCell>
                  <Chip
                    label={doc.entityName}
                    color={getEntityColor(doc.entityType)}
                    size="small"
                  />
                </TableCell>
                <TableCell>{formatFileSize(doc.size)}</TableCell>
                <TableCell>{doc.uploadedBy}</TableCell>
                <TableCell>{new Date(doc.uploadedAt).toLocaleDateString('tr-TR')}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" color="primary">
                    <DownloadIcon />
                  </IconButton>
                  <IconButton size="small" color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default Documents;

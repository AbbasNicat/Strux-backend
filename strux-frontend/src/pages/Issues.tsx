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
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
} from '@mui/icons-material';

interface Issue {
  id: string;
  title: string;
  project: string;
  reporter: string;
  assignee: string;
  status: string;
  priority: string;
  severity: string;
  createdAt: string;
}

const Issues: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  const issues: Issue[] = [
    {
      id: '1',
      title: 'Beton kalitesi yetersiz',
      project: 'Bahçeşehir Konut Projesi',
      reporter: 'Ali Veli',
      assignee: 'Mehmet Demir',
      status: 'OPEN',
      priority: 'CRITICAL',
      severity: 'MAJOR',
      createdAt: '2024-11-25',
    },
    {
      id: '2',
      title: 'Elektrik tesisatı hatası',
      project: 'Maltepe Residence',
      reporter: 'Ayşe Yılmaz',
      assignee: 'Can Öztürk',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      severity: 'CRITICAL',
      createdAt: '2024-11-28',
    },
  ];

  const getStatusColor = (status: string) => {
    const colors: any = {
      OPEN: 'error',
      IN_PROGRESS: 'warning',
      RESOLVED: 'info',
      CLOSED: 'success',
      REOPENED: 'error',
    };
    return colors[status] || 'default';
  };

  const getStatusText = (status: string) => {
    const texts: any = {
      OPEN: 'Açık',
      IN_PROGRESS: 'Devam Ediyor',
      RESOLVED: 'Çözüldü',
      CLOSED: 'Kapatıldı',
      REOPENED: 'Yeniden Açıldı',
    };
    return texts[status] || status;
  };

  const getPriorityColor = (priority: string) => {
    const colors: any = {
      LOW: 'success',
      MEDIUM: 'info',
      HIGH: 'warning',
      CRITICAL: 'error',
    };
    return colors[priority] || 'default';
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Sorunlar
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Yeni Sorun
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Sorun ara..."
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
              <TableCell><strong>Başlık</strong></TableCell>
              <TableCell><strong>Proje</strong></TableCell>
              <TableCell><strong>Raporlayan</strong></TableCell>
              <TableCell><strong>Atanan</strong></TableCell>
              <TableCell><strong>Durum</strong></TableCell>
              <TableCell><strong>Öncelik</strong></TableCell>
              <TableCell><strong>Tarih</strong></TableCell>
              <TableCell align="right"><strong>İşlemler</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {issues.map((issue) => (
              <TableRow key={issue.id} hover>
                <TableCell>{issue.title}</TableCell>
                <TableCell>{issue.project}</TableCell>
                <TableCell>{issue.reporter}</TableCell>
                <TableCell>{issue.assignee}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusText(issue.status)}
                    color={getStatusColor(issue.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={issue.priority}
                    color={getPriorityColor(issue.priority)}
                    size="small"
                  />
                </TableCell>
                <TableCell>{new Date(issue.createdAt).toLocaleDateString('tr-TR')}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" color="primary">
                    <EditIcon />
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

export default Issues;

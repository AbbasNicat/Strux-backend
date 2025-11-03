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
  Map as MapIcon,
  Search as SearchIcon,
} from '@mui/icons-material';

interface Project {
  id: string;
  name: string;
  status: string;
  progress: number;
  startDate: string;
  company: string;
}

const Projects: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data
  const projects: Project[] = [
    {
      id: '1',
      name: 'Bahçeşehir Konut Projesi',
      status: 'IN_PROGRESS',
      progress: 65,
      startDate: '2024-01-15',
      company: 'ABC İnşaat',
    },
    {
      id: '2',
      name: 'Maltepe Residence',
      status: 'PLANNING',
      progress: 15,
      startDate: '2024-03-01',
      company: 'XYZ Yapı',
    },
    {
      id: '3',
      name: 'Kadıköy Plaza',
      status: 'COMPLETED',
      progress: 100,
      startDate: '2023-06-10',
      company: 'ABC İnşaat',
    },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PLANNING':
        return 'info';
      case 'IN_PROGRESS':
        return 'warning';
      case 'COMPLETED':
        return 'success';
      case 'ON_HOLD':
        return 'default';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PLANNING':
        return 'Planlama';
      case 'IN_PROGRESS':
        return 'Devam Ediyor';
      case 'COMPLETED':
        return 'Tamamlandı';
      case 'ON_HOLD':
        return 'Beklemede';
      case 'CANCELLED':
        return 'İptal';
      default:
        return status;
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Projeler
        </Typography>
        <Box>
          <Button
            variant="outlined"
            startIcon={<MapIcon />}
            sx={{ mr: 1 }}
          >
            Harita Görünümü
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
          >
            Yeni Proje
          </Button>
        </Box>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Proje ara..."
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
              <TableCell><strong>Proje Adı</strong></TableCell>
              <TableCell><strong>Şirket</strong></TableCell>
              <TableCell><strong>Durum</strong></TableCell>
              <TableCell><strong>İlerleme</strong></TableCell>
              <TableCell><strong>Başlangıç Tarihi</strong></TableCell>
              <TableCell align="right"><strong>İşlemler</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {projects.map((project) => (
              <TableRow key={project.id} hover>
                <TableCell>{project.name}</TableCell>
                <TableCell>{project.company}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusText(project.status)}
                    color={getStatusColor(project.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>{project.progress}%</TableCell>
                <TableCell>{new Date(project.startDate).toLocaleDateString('tr-TR')}</TableCell>
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

export default Projects;

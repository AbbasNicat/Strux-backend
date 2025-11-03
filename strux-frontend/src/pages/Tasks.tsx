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
  LinearProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
} from '@mui/icons-material';

interface Task {
  id: string;
  title: string;
  project: string;
  assignee: string;
  status: string;
  priority: string;
  progress: number;
  dueDate: string;
}

const Tasks: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data
  const tasks: Task[] = [
    {
      id: '1',
      title: 'Temel kazısı kontrolü',
      project: 'Bahçeşehir Konut Projesi',
      assignee: 'Ahmet Yılmaz',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      progress: 60,
      dueDate: '2024-12-15',
    },
    {
      id: '2',
      title: 'Malzeme temini',
      project: 'Maltepe Residence',
      assignee: 'Mehmet Demir',
      status: 'TODO',
      priority: 'MEDIUM',
      progress: 0,
      dueDate: '2024-12-20',
    },
    {
      id: '3',
      title: 'Kalite kontrolü',
      project: 'Kadıköy Plaza',
      assignee: 'Ayşe Kaya',
      status: 'COMPLETED',
      priority: 'HIGH',
      progress: 100,
      dueDate: '2024-11-30',
    },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'TODO':
        return 'default';
      case 'IN_PROGRESS':
        return 'info';
      case 'REVIEW':
        return 'warning';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'TODO':
        return 'Yapılacak';
      case 'IN_PROGRESS':
        return 'Devam Ediyor';
      case 'REVIEW':
        return 'İnceleme';
      case 'COMPLETED':
        return 'Tamamlandı';
      case 'CANCELLED':
        return 'İptal';
      default:
        return status;
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'LOW':
        return 'success';
      case 'MEDIUM':
        return 'info';
      case 'HIGH':
        return 'warning';
      case 'URGENT':
        return 'error';
      default:
        return 'default';
    }
  };

  const getPriorityText = (priority: string) => {
    switch (priority) {
      case 'LOW':
        return 'Düşük';
      case 'MEDIUM':
        return 'Orta';
      case 'HIGH':
        return 'Yüksek';
      case 'URGENT':
        return 'Acil';
      default:
        return priority;
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Görevler
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Yeni Görev
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Görev ara..."
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
              <TableCell><strong>Görev</strong></TableCell>
              <TableCell><strong>Proje</strong></TableCell>
              <TableCell><strong>Atanan</strong></TableCell>
              <TableCell><strong>Durum</strong></TableCell>
              <TableCell><strong>Öncelik</strong></TableCell>
              <TableCell><strong>İlerleme</strong></TableCell>
              <TableCell><strong>Bitiş Tarihi</strong></TableCell>
              <TableCell align="right"><strong>İşlemler</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tasks.map((task) => (
              <TableRow key={task.id} hover>
                <TableCell>{task.title}</TableCell>
                <TableCell>{task.project}</TableCell>
                <TableCell>{task.assignee}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusText(task.status)}
                    color={getStatusColor(task.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={getPriorityText(task.priority)}
                    color={getPriorityColor(task.priority)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LinearProgress
                      variant="determinate"
                      value={task.progress}
                      sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
                    />
                    <Typography variant="body2">{task.progress}%</Typography>
                  </Box>
                </TableCell>
                <TableCell>{new Date(task.dueDate).toLocaleDateString('tr-TR')}</TableCell>
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

export default Tasks;

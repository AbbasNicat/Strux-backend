import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Chip,
  Avatar,
  TextField,
  InputAdornment,
} from '@mui/material';
import {
  Add as AddIcon,
  Business as BusinessIcon,
  Search as SearchIcon,
} from '@mui/icons-material';

interface Company {
  id: string;
  name: string;
  type: string;
  status: string;
  email: string;
  phone: string;
  projectCount: number;
}

const Companies: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data
  const companies: Company[] = [
    {
      id: '1',
      name: 'ABC İnşaat A.Ş.',
      type: 'GENERAL_CONTRACTOR',
      status: 'ACTIVE',
      email: 'info@abcinsaat.com',
      phone: '+90 212 555 1234',
      projectCount: 12,
    },
    {
      id: '2',
      name: 'XYZ Yapı Ltd.',
      type: 'DEVELOPER',
      status: 'ACTIVE',
      email: 'contact@xyzyapi.com',
      phone: '+90 216 555 5678',
      projectCount: 8,
    },
    {
      id: '3',
      name: 'Mimar Danışmanlık',
      type: 'CONSULTANT',
      status: 'ACTIVE',
      email: 'info@mimardanismanlik.com',
      phone: '+90 312 555 9012',
      projectCount: 24,
    },
  ];

  const getTypeText = (type: string) => {
    switch (type) {
      case 'GENERAL_CONTRACTOR':
        return 'Ana Yüklenici';
      case 'SUB_CONTRACTOR':
        return 'Alt Yüklenici';
      case 'DEVELOPER':
        return 'Geliştirici';
      case 'CONSULTANT':
        return 'Danışman';
      default:
        return type;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'SUSPENDED':
        return 'error';
      case 'INACTIVE':
        return 'default';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'Aktif';
      case 'PENDING':
        return 'Beklemede';
      case 'SUSPENDED':
        return 'Askıya Alındı';
      case 'INACTIVE':
        return 'Pasif';
      default:
        return status;
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Şirketler
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Yeni Şirket
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Şirket ara..."
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

      <Grid container spacing={3}>
        {companies.map((company) => (
          <Grid item xs={12} md={6} lg={4} key={company.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar sx={{ width: 56, height: 56, mr: 2, bgcolor: 'primary.main' }}>
                    <BusinessIcon />
                  </Avatar>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component="div">
                      {company.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {getTypeText(company.type)}
                    </Typography>
                  </Box>
                  <Chip
                    label={getStatusText(company.status)}
                    color={getStatusColor(company.status)}
                    size="small"
                  />
                </Box>

                <Typography variant="body2" color="text.secondary" gutterBottom>
                  📧 {company.email}
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  📱 {company.phone}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  📊 {company.projectCount} Proje
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small">Detaylar</Button>
                <Button size="small">Düzenle</Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default Companies;

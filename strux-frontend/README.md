# Strux Frontend - İnşaat Yönetim Sistemi

Modern React frontend for Strux construction management system.

## 🚀 Teknolojiler

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Material-UI (MUI)** - Component library
- **React Router** - Navigation
- **Axios** - HTTP client
- **TailwindCSS** - Utility-first CSS
- **Zustand** - State management (lightweight alternative to Redux)
- **React Hook Form** - Form management
- **React Hot Toast** - Notifications

## 📋 Özellikler

### Kimlik Doğrulama
- Kullanıcı girişi ve kaydı
- Google OAuth2 entegrasyonu
- JWT token yönetimi
- Otomatik token yenileme
- Korumalı rotalar

### Ana Modüller
- **Dashboard** - Genel bakış ve istatistikler
- **Şirketler** - Şirket yönetimi
- **Projeler** - Proje takibi ve harita görünümü
- **Görevler** - Görev oluşturma ve atama
- **Sorunlar** - Sorun takibi
- **Birimler** - Emlak birimi yönetimi
- **Dökümanlar** - Dosya yükleme ve indirme

## 🛠️ Kurulum

### Gereksinimler
- Node.js 18+
- npm veya yarn

### Adımlar

1. Bağımlılıkları yükleyin:
```bash
npm install
```

2. Environment dosyasını oluşturun:
```bash
cp .env.example .env
```

3. `.env` dosyasını düzenleyin:
```env
VITE_API_BASE_URL=http://localhost:8081/api
VITE_GOOGLE_MAPS_API_KEY=your_api_key_here
```

4. Geliştirme sunucusunu başlatın:
```bash
npm run dev
```

Uygulama http://localhost:3000 adresinde çalışacaktır.

## 🏗️ Build

Production build oluşturmak için:
```bash
npm run build
```

Build dosyaları `dist/` klasöründe oluşturulacaktır.

## 📁 Proje Yapısı

```
src/
├── components/      # Reusable components
├── contexts/        # React contexts (Auth, etc.)
├── hooks/           # Custom hooks
├── layouts/         # Layout components
├── pages/           # Page components
├── services/        # API services
├── types/           # TypeScript types
├── utils/           # Utility functions
├── App.tsx          # Main app component
└── main.tsx         # Entry point
```

## 🔗 API Entegrasyonu

Frontend, backend API'sine http://localhost:8081 üzerinden bağlanır. Vite proxy yapılandırması sayesinde `/api` istekleri otomatik olarak backend'e yönlendirilir.

### Mevcut API Endpoints:
- `/api/auth/*` - Kimlik doğrulama
- `/api/users/*` - Kullanıcı yönetimi
- `/api/companies/*` - Şirket yönetimi
- `/api/projects/*` - Proje yönetimi
- `/api/tasks/*` - Görev yönetimi
- `/api/issues/*` - Sorun takibi
- `/api/units/*` - Birim yönetimi
- `/api/documents/*` - Döküman yönetimi
- `/api/notifications/*` - Bildirimler

## 🎨 Tema ve Stil

Uygulama Material-UI tema sistemini kullanır. Tema ayarları `src/App.tsx` içinde yapılandırılmıştır.

TailwindCSS utility classları, MUI ile birlikte kullanılabilir ancak preflight özelliği devre dışı bırakılmıştır (MUI ile çakışmayı önlemek için).

## 🔐 Kimlik Doğrulama Akışı

1. Kullanıcı login sayfasından giriş yapar
2. Backend JWT token döner
3. Token localStorage'a kaydedilir
4. Her API isteğinde token otomatik olarak header'a eklenir
5. Token süresi dolduğunda otomatik refresh yapılır
6. Refresh başarısız olursa kullanıcı login sayfasına yönlendirilir

## 📱 Responsive Tasarım

Uygulama tamamen responsive'dir ve mobil, tablet ve masaüstü cihazlarda çalışır.

## 🧪 Test

(Test yapılandırması eklenecek)

## 📝 Lisans

Bu proje Strux projesi için geliştirilmiştir.

package com.strux.task_service.enums;

public enum TaskType {
    CONSTRUCTION,        // Fiziksel inşaat işleri (beton dökme, duvar örme, vs.)
    INSTALLATION,        // Elektrik, su, doğalgaz, ısıtma, tesisat montajları
    INSPECTION,          // Kontrol, denetim, kalite testi
    DESIGN,              // Mimari, mühendislik veya teknik çizim işleri
    PROCUREMENT,         // Malzeme, ekipman veya kaynak tedarik işleri
    LOGISTICS,           // Malzeme taşıma, teslimat, saha içi lojistik
    SAFETY,              // Güvenlik denetimleri, iş güvenliği görevleri
    ADMINISTRATIVE,      // Evrak, sözleşme, rapor, ofis içi işler
    COMMUNICATION,       // Şirket içi veya proje dışı iletişim görevleri
    MAINTENANCE,         // Tamir, bakım, yeniden yapılandırma işleri
    REPORTING,           // Haftalık/aylık ilerleme raporları
    OTHER                // Yukarıdakilerin dışında kalan özel işler
}

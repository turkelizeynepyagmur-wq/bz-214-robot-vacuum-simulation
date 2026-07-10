# BZ 214 Visual Programming Robot Vacuum Cleaning Simulation

Bu proje, **Model-View-Controller (MVC)** tasarım deseni kullanılarak Java ve JavaFX ile geliştirilmiş interaktif bir **2 Boyutlu Robot Süpürge Simülasyonu** uygulamasıdır. Simülasyon; temizlik algoritmaları (Rastgele, Spiral, Duvar Takip), batarya yönetimi, dinamik engel/kir yerleştirme ve BFS tabanlı yol bulma algoritmalarını görselleştirerek çalışır.

---

## 🚀 Önemli Özellikler

* **Dinamik Hücresel Izgara (Grid):** Simülasyon 20x14 boyutlarında hücrelerden oluşur. Başlangıçta sınır duvarları otomatik yerleştirilir. Şarj istasyonu (0, 12) konumundadır.
* **Dinamik Mobilya ve Engel Ekleme:**
    * Koltuk (1x3), TV Ünitesi (1x3), Sehpalı Halı (2x2), Berjerler (1x1) ve Duvarlar (1x1) fare ile tıklanarak ızgaraya yerleştirilebilir.
    * Yerleştirilmiş olan bir mobilyayı döndürmek için, sol panelden aynı mobilya türü seçiliyken ızgaradaki mobilyaya doğrudan tıklayabilirsiniz (0° ➔ 90° ➔ 180° ➔ 270° döngüsü).
    * Mobilya silmek için **"Silgi"** aracını seçip mobilyaya tıklamanız yeterlidir. Mobilyanın kapladığı tüm hücreler tek tıklamayla temizlenir.
* **Kademeli Kir ve Leke Türleri:**
    * **Toz (Dust):** Hızlı temizlenir, düşük batarya tüketir (+1 sn süre, +1.0 ek batarya tüketimi).
    * **Sıvı (Liquid):** Orta hızda temizlenir, orta batarya tüketir (+3 sn süre, +2.5 ek batarya tüketimi).
    * **Leke (Stain):** En zor temizlenen kir türüdür (+5 sn süre, +4.0 ek batarya tüketimi).
* **Gelişmiş Batarya ve Şarj Mekanizması:**
    * Robotun maksimum bataryası %100'dür. Her temel adım hareketi 0.5 birim batarya tüketir.
    * Batarya %20'nin altına indiğinde veya kullanıcı **"İstasyona Dön"** emri verdiğinde, robot temizliği durdurur ve şarj istasyonuna dönmek üzere BFS ile en kısa rotayı hesaplar.
    * Şarj istasyonunda her adımda (tick) +5.0 batarya kazanır ve %100 şarj olduğunda temizliğe kaldığı yerden devam etmek üzere yola çıkar.
    * Batarya tamamen biterse robot durur ve durumu *"Batarya Bitti (Sıkıştı)"* olarak güncellenir.
* **Gelişmiş İstatistikler Paneli:**
    * Toplam Temizlenebilir Alan ($m^2$ cinsinden)
    * Temizlenen Alan Yüzdesi ve Miktarı
    * Kalan Alan Yüzdesi ve Miktarı
    * Geçen Süre (dk:sn formatında)
    * Toplanan Toplam Kir Miktarı
* **Hız Ayarı:** Slider aracılığıyla robotun hareket hızı 0.5x ile 5.0x arasında gerçek zamanlı olarak ayarlanabilir.

---

## 🧠 Kullanılan Algoritmalar

1. **BFS (Breadth-First Search) ile En Kısa Yol Bulma:**
    * Robotun bataryası azaldığında veya kullanıcı "İstasyona Dön" emri verdiğinde şarj istasyonuna giden en kısa engelsiz yolu hesaplamak için kullanılır.
    * Rota ekranda yeşil kesikli çizgilerle dinamik olarak çizilir.
2. **Flood Fill (BFS) ile Erişilemeyen Alan Tespiti:**
    * Odadaki engeller sebebiyle şarj istasyonundan fiziksel olarak ulaşılamayan kapalı alanlar tespit edilir, grid üzerinde koyu gölgeyle kaplanır ve temizlik istatistiklerine dahil edilmez.
3. **Temizlik Algoritmaları:**
    * **Rastgele (Random):** Robot düz gitmeye çalışır. Önü kapalıysa ya da zaten temizlenmişse etrafındaki temizlenmemiş yönleri arar. Yakınında temizlenmemiş hiçbir yer yoksa, BFS kullanarak odadaki en yakın temizlenmemiş hücreye otomatik rota çizer.
    * **Spiral:** Robot merkezden başlayarak büyüyen bir kare spiral çizer. Bir engelle veya temizlenmiş alanla karşılaştığında spirali bozar, BFS ile en yakın temizlenmemiş alana gidip orada yeni bir spiral başlatır.
    * **Duvar Takip (Wall Follow):** Robot duvarı veya engelleri sağ tarafında tutarak duvar kenarlarını sistematik şekilde takip eder ve süpürür.

---

## 📂 Proje Yapısı

```text
src/
├── algorithm/
│   └── PathFinder.java            # BFS en kısa yol bulma algoritması sınıfı
├── application/
│   ├── MainApp.java               # Uygulama Giriş Noktası (JavaFX Stage ayarları)
│   └── Launcher.java              # Modül hatalarını aşmak için ikincil başlatıcı sınıfı
├── controller/
│   └── SimulationController.java  # Simülasyon Döngüsü, Robot ve Izgara Durum Yönetimi
├── model/
│   ├── Cell.java                  # Grid hücresi (konum, engel, kir, temizlenme durumu)
│   ├── DirtType.java              # Kir tipleri enum sınıfı (Toz, Sıvı, Leke)
│   ├── ObstacleType.java          # Engel tipleri enum sınıfı (Koltuk, TV, Sehpa, Sandalye, Duvar)
│   ├── RobotModel.java            # Robotun konumu, yönü, bataryası ve rota durumları
│   └── RoomGrid.java              # Odanın 2D hücre matrisi ve Flood Fill alan hesaplamaları
├── view/
│   └── SimulationView.java        # JavaFX arayüz tasarımı, butonlar, animasyonlar ve çizimler
└── resources/
    └── images/                    # Robot, istasyon, mobilyalar ve kir ikonları (.png)
---

## 🎮 Simülasyon Kontrolleri ve Kullanımı

Araç Seçimi: Sol paneldeki mobilyalardan (Koltuk, TV, Sehpa, Berjer), duvar çizme veya kirlerden (Toz, Sıvı, Leke) birini seçin. Grid üzerinde istediğiniz hücreye tıklayarak yerleştirin.

Döndürme: Sol panelden ilgili mobilya türü seçiliyken ızgara üzerindeki mobilyaya doğrudan tıklayarak yönünü döndürebilirsiniz (0° ➔ 90° ➔ 180° ➔ 270°).

Temizleme (Silgi): "Silgi" aracını seçip eklediğiniz engel veya kire tıklayarak kaldırın. Mobilyalar çoklu hücre kaplasa dahi tek tıklamayla tüm kapladığı hücreler temizlenir.

Simülasyon Hızı: Slider aracılığıyla robotun hareket hızını anlık olarak 0.5x ile 5.0x arasında değiştirebilirsiniz.

Algoritma Seçimi: Robot hareket ederken dahi sol taraftan temizlik algoritmasını (Rastgele, Spiral, Duvar Takip) değiştirebilirsiniz.

Kontrol Butonları:

Başlat: Simülasyonu başlatır.

Duraklat: Simülasyonu duraklatır veya devam ettirir.

Sıfırla: Izgaradaki tüm kirleri ve engelleri temizleyerek robotu başlangıç konumuna (şarj istasyonu) döndürür ve bataryayı %100 yapar.

İstasyona Dön: Robota şarj istasyonuna gitme emri verir. Robot en kısa yolu yeşil kesikli çizgiyle göstererek istasyona hareket eder.

## 🎓 Public Sharing and Acknowledgment
This project was developed as part of the BZ 214 Visual Programming course. Special thanks to the course instructor and contributors.

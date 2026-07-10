package model;

import java.util.LinkedList;
import java.util.Queue;
/**
 * Bu sınıf robot süpürgenin içinde gezeceği sanal odayı oluşturuyor.
 * Burada robot süpürge simülasyonu için odanın iki boyutlu ızgarasını oluşturuyoruz.
 */
public class RoomGrid {
    private final Cell[][] cells;
    private final int width;
    private final int height;

    //Yapıcı Metot (Constructor)
    public RoomGrid(int width, int height) {
        if (width <= 0 || height <= 0) {
            //Biz boyutları standart ayarladık ama daha sonra değiştirmek istersek eksi bir boyut oluşturulamasın diye ekledik.
            throw new IllegalArgumentException("Izgara genişliği ve yüksekliği pozitif tam sayılar olmalıdır");
        }
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];

        createCells();
    }
    private void createCells() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell(x, y);
            }
        }
    } //burada hücre nesnesini oluşturuyoruz.

    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return cells[y][x];
        }
        return null;
    }
    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }//Robot süpürgenin duvarın ötesine geçemesini engelliyoruz.
    // bu işlemin sonucunda true demek bu koordinat haritanın içinde
    // ve false demek bu koordinat odanın dışında oluyor.

    public int getWidth() {
        return width;
    }// en boyutunu getir

    public int getHeight() {
        return height;
    }// yüksekliği getir

    //Odanın temizlenme yüzdesini hesaplayan metot
    public double getCleanedPercentage() {
        int totalCleanableCells = 0; // Temizlenmesi gereken toplam alan
        int cleanedCells = 0;    // Şu ana kadar temizlenmiş alan
        // Odanın içindeki tüm kareleri tek tek gez
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = cells[y][x];

                // Eğer hücrede engel (duvar/mobilya) YOKSA hesaplamaya dahil et
                if (!cell.isObstacle()) {
                    totalCleanableCells++;
                    // Eğer bu temizlenebilir hücre temizlenmişse sayacı artır
                    if (cell.isCleaned()) {
                        cleanedCells++;
                    }
                }
            }
        }

        // Eğer odada temizlenecek hiçbir yer yoksa (her yer engelse), temizlik %100 bitmiş sayılır.
        if (totalCleanableCells == 0) {
            return 100.0;
        }
        // (Temizlenen / Toplam) * 100
        return ((double) cleanedCells / totalCleanableCells) * 100.0;
    }

    // BFS (Breadth-First Search) algoritması ile odadaki kapalı/ulaşılamaz bölgeleri tespit ediyoruz.
    public void findUnreachableZones(int startX, int startY) {
        // 1. ADIM: Tüm haritayı sıfırlıyoruz. Başlangıçta tüm boşlukları "Ulaşılamaz/Unreachable" varsayıyoruz.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!cells[y][x].isObstacle()) {
                    cells[y][x].setUnreachable(true); // Boşluklar şimdilik ulaşılamaz
                } else {
                    cells[y][x].setUnreachable(false); // Engeller ulaşılamaz
                }
            }
        }
        // 2. ADIM: Başlangıç noktası (şarj istasyonu)
        Cell startNode = getCell(startX, startY);
        if (startNode == null || startNode.isObstacle()) {
            return; // Başlangıç noktası geçersiz veya engelin içindeyse işlemi iptal et
        }
        //algoritmamız için bir kuyruk (Queue) oluşturuyoruz ve başlangıç noktasını ekliyoruz
        Queue<Cell> queue = new LinkedList<>();
        queue.add(startNode);
        startNode.setUnreachable(false);// Başlangıç noktası kesinlikle ulaşılabilir olsun.
        // Yön Vektörleri: Sol, Sağ, Yukarı, Aşağı
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        // 3. ADIM: Kuyruk bitene kadar kontrol et
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            // Bulunan hücrenin 4 tarafına da bak
            for (int[] dir : dirs) {
                int nx = current.getX() + dir[0];
                int ny = current.getY() + dir[1];
                Cell neighbor = getCell(nx, ny);

                // Eğer komşu hücre harita içindeyse, engel değilse ve hala ulaşılamaz görünüyorsa:
                if (neighbor != null && !neighbor.isObstacle() && neighbor.isUnreachable()) {
                    neighbor.setUnreachable(false); // Artık buraya ulaşılabilir oluyor.
                    queue.add(neighbor); // Komşunun da etrafına bakmak için kuyruğa ekle
                }
            }
        }
    }
}

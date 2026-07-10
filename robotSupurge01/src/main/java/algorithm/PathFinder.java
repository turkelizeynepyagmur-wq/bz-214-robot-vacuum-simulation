package algorithm;

import model.Cell;
import model.RoomGrid;

import java.util.*;

public class PathFinder {

    // Hareket yönleri: Yukarı, Aşağı, Sol, Sağ
    private static final int[][] DIRECTIONS = {
            {0, -1},
            {0, 1},
            {-1, 0},
            {1, 0}
    };

    public static Queue<Cell> findPath(
            RoomGrid roomGrid,
            int startX,
            int startY,
            int targetX,
            int targetY) {

        Queue<Cell> path = new LinkedList<>();

        Cell startCell = roomGrid.getCell(startX, startY);
        Cell targetCell = roomGrid.getCell(targetX, targetY);

        // Başlangıç veya hedef hücre geçersizse boş yol döndür
        if (startCell == null || targetCell == null) {
            return path;
        }

        // Robot zaten istasyondaysa gidilecek yol yok
        if (startCell.equals(targetCell)) {
            return path;
        }

        // BFS için gerekli veri yapıları
        Queue<Cell> queue = new LinkedList<>();
        Set<Cell> visited = new HashSet<>();
        Map<Cell, Cell> parentMap = new HashMap<>();

        // Başlangıç düğümünü kuyruğa ekle
        queue.add(startCell);
        visited.add(startCell);

        boolean found = false;

        // BFS arama döngüsü
        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // Hedefe ulaşıldı mı kontrol et
            if (current.equals(targetCell)) {
                found = true;
                break;
            }

            // Komşu hücreleri kontrol et
            for (int[] dir : DIRECTIONS) {
                int nx = current.getX() + dir[0];
                int ny = current.getY() + dir[1];

                Cell neighbor = roomGrid.getCell(nx, ny);

                // Hücre engel değilse ve daha önce ziyaret edilmemişse
                if (neighbor != null
                        && !neighbor.isObstacle()
                        && !visited.contains(neighbor)) {

                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Yol bulunduysa geri izleme yap
        if (found) {
            List<Cell> route = new ArrayList<>();
            Cell curr = targetCell;

            // Hedeften başlangıca doğru ilerle
            while (curr != null && !curr.equals(startCell)) {
                route.add(curr);
                curr = parentMap.get(curr);
            }

            // Liste ters oluştuğu için çevir
            Collections.reverse(route);

            // Kuyruğa ekle
            path.addAll(route);
        }

        return path;
    }
}
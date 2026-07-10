package model;

import java.util.Queue;
import algorithm.PathFinder;

/**
 * Robot süpürgenin model sınıfıdır.
 * Robotun konumunu, pil durumunu ve çalışma modunu yönetir.
 */
public class RobotModel {

    // Pil sabitleri
    private static final double MAX_BATTERY = 100.0;
    private static final double MOVE_COST = 0.5;
    private static final double CHARGE_AMOUNT = 5.0;

    // Robotun mevcut x ve y koordinatları
    private int x;
    private int y;

    // Robotun baktığı yön (Kuzey, Güney, Doğu, Batı)
    private String direction;

    // Mevcut pil seviyesi (max 100)
    private double battery;

    // Şarj istasyonunun sabit koordinatları
    private final int stationX;
    private final int stationY;

    // Robotun çalışma modları. Örneğin;CLEANING, RETURNING_TO_STATION, CHARGING
    private String currentMode;

    /**  Robot nesnesi oluşturur.
     *   @param startX Robotun başlangıç x koordinatı
     *   @param startY Robotun başlangıç y koordinatı
     *   @param stationX Şarj istasyonunun x koordinatı
     *   @param stationY Şarj istasyonunun y koordinatı
     */
    public RobotModel(int startX, int startY, int stationX, int stationY) {
        this.x = startX;
        this.y = startY;
        this.stationX = stationX;
        this.stationY = stationY;

        this.battery = MAX_BATTERY;
        this.direction = "North";
        this.currentMode = "CLEANING";
    }

    /**
     * Robotu belirtilen koordinata taşır.
     * Her hareket için 0.5 pil tüketilir.
     * @param newX The new x-coordinate.
     * @param newY The new y-coordinate.
     */
    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;

        // Bir hücre hareket etmenin temel pil maliyeti
        this.battery = Math.max(0.0, this.battery - MOVE_COST);
    }

    /**
     * Temizlenen kir türüne göre ek pil tüketimi yapar.
     *
     * @param dirtType Bulunulan hücredeki kir türü
     */
    public void consumeExtraBattery(DirtType dirtType) {
        if (dirtType != null) {
            this.battery = Math.max(0.0, this.battery - dirtType.getBatteryCost());
        }
    }

    /**
     * Robot şarj istasyonundaysa pilini doldurur.
     * Her çağrıda pil 5 birim artar ve maksimum 100'e kadar çıkar.
     */
    public void charge() {
        if (this.x == stationX && this.y == stationY) {
            this.battery = Math.min(MAX_BATTERY,
                    this.battery + CHARGE_AMOUNT);
        }
    }

    /**
     * Robotun mevcut konumundan şarj istasyonuna en kısa yolu hesaplar.
     *
     * @param roomGrid Odanın mevcut haritası
     * @return Şarj istasyonuna ulaşmak için izlenecek hücreler kuyruğu
     */
    public Queue<Cell> calcShortestPathStation(RoomGrid roomGrid) {

        return PathFinder.findPath(
                roomGrid,
                x,
                y,
                stationX,
                stationY
        );
    }

    // Getter ve Setter Metotları

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public int getStationX() {
        return stationX;
    }

    public int getStationY() {
        return stationY;
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }
}
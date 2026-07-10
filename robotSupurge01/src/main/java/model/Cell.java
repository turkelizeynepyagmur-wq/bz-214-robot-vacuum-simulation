package model;

public class Cell {
    private final int x;
    private final int y;

    private ObstacleType obstacleType = ObstacleType.NONE;
    private DirtType dirtType = DirtType.NONE;

    private boolean isUnreachable = false;
    private boolean isCleaned = false;

    private int obstacleRootX = -1;
    private int obstacleRootY = -1;

    private int obstacleRotation = 0;

    /**
     * Hücre nesnesini oluşturur.
     *
     * @param x Hücrenin sütun (x) koordinatı
     * @param y Hücrenin satır (y) koordinatı
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Hücrenin x koordinatını döndürür.
     *
     * @return x koordinatı
     */
    public int getX() {
        return x;
    }

    // Hücrenin y koordinatını döndürür.
    public int getY() {
        return y;
    }

    /**
     * Hücrenin bir engel içerip içermediğini kontrol eder.
     *
     * @return Hücre engel içeriyorsa true, aksi halde false
     */
    public boolean isObstacle() {

        return obstacleType != ObstacleType.NONE;
    }

    //Hücrede bulunan engel türünü döndürür.
    public ObstacleType getObstacleType() {
        return obstacleType;
    }

    //Hücrenin engel türünü ayarlar.
    public void setObstacleType(ObstacleType type) {
        this.obstacleType = type;
        if (type == ObstacleType.NONE) {
            this.obstacleRootX = -1;
            this.obstacleRootY = -1;
            this.obstacleRotation = 0;
        }
    }

    public int getObstacleRootX() {

        return obstacleRootX;
    }

    public void setObstacleRootX(int obstacleRootX) {

        this.obstacleRootX = obstacleRootX;
    }

    public int getObstacleRootY() {

        return obstacleRootY;
    }

    public void setObstacleRootY(int obstacleRootY) {

        this.obstacleRootY = obstacleRootY;
    }

    public int getObstacleRotation() {

        return obstacleRotation;
    }

    public void setObstacleRotation(int rotation) {

        this.obstacleRotation = rotation;
    }

    //Hücredeki kir türünü döndürür.
    public DirtType getDirtType() {

        return dirtType;
    }

    /**
     * Hücreye yeni bir kir türü atar.
     *
     * @param dirtType Atanacak kir türü
     */
    public void setDirtType(DirtType dirtType) {

        this.dirtType = dirtType;
    }

    /**
     * Hücrenin kirli olup olmadığını kontrol eder.
     *
     * @return Hücre kirliyse true, aksi halde false
     */
    public boolean isDirty() {
        return dirtType != DirtType.NONE;
    }

    //Hücrenin robot tarafından ulaşılamaz durumda olup olmadığını kontrol eder.
    public boolean isUnreachable() {

        return isUnreachable;
    }

    //Hücrenin ulaşılabilirlik durumunu ayarlar.
    public void setUnreachable(boolean unreachable) {

        this.isUnreachable = unreachable;
    }

    //Hücrenin temizlenip temizlenmediğini kontrol eder.
    public boolean isCleaned() {

        return isCleaned;
    }
    /**
     * Hücrenin temizlenme durumunu ayarlar.
     *
     * @param cleaned Temizlenmiş ise true
     */
    public void setCleaned(boolean cleaned) {

        this.isCleaned = cleaned;
    }
}
package model;
public enum DirtType {
    //Kir cesitleri ve o kiri temizlemek icin gereken sure
    NONE(0.0, 0.0),
    DUST(1.0, 1.0),     // hizli temizlenir az batarya harcar
    LIQUID(2.5, 3.0),   // biraz daha yavas temizlenir orta batarya harcar
    STAIN(4.0, 5.0);    // Zor temizlenir fazla batarya harcar
    private final double batteryCost;
    private final double cleaningDuration;

    DirtType(double batteryCost, double cleaningDuration) {
        this.batteryCost = batteryCost;
        this.cleaningDuration = cleaningDuration;
    }

    public double getBatteryCost() {
        return batteryCost;
    }

    public double getCleaningMoment() {
        return cleaningDuration;
    }
}
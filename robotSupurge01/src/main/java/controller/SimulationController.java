package controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Toggle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import javafx.util.Duration;
import model.*;
import java.util.List;
import java.util.ArrayList;
import view.SimulationView;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Arayüzdeki araç seçimi (Silgi, Kir, Engel vs.) için gereken

import java.util.Random;

import static java.awt.Toolkit.getDefaultToolkit;

/**
 * Controller class for the Robot Vacuum simulation.
 * Manages simulation loops, model state changes, movement algorithms,
 * and interacts with the view to update the UI.
 */
public class SimulationController {

    private SimulationView view;
    // Modeller
    private final RoomGrid roomGrid;//Odanın grid yapısını tutar
    private final RobotModel robotModel;//Robotla ilgil özellikleri tutar
    //Simülasyon döngüsü ve zamanlayıcısı
    private final Random random = new Random();
    private Timeline timeline;
    private int elapsedTime= 0;
    private int dustCount= 0;
    //Yol bulmak için
    private Queue<Cell> returnPath = new LinkedList<>();
    private Queue<Cell> cleanPath = new LinkedList<>();
    //Robotun güncel durumu ve temizleme şekli
    private int currentrotation= 0;
    private int ticksLeft = 0;
    private DirtType currentDirt = null;
    //Robotun o anki gidiş yönü vektörleri
    private int robotDx= 1;
    private int robotDy = 0;
    //sprial temizlik algoritması için değişkenler
    private int spiralLength = 0;
    private int spiralCount = 0;
    private int spiralIdx = 0;

    public SimulationController(int width, int height) {
        this.roomGrid = new RoomGrid(width, height);
        // 0,12 lokasyona yerleşti
        this.robotModel = new RobotModel(0, 12, 0, 12);
        // Buradaki döngülerin amacı dıs duvarları çizmek bunu da o odanın gridleri üzerinde gezinerek yapıyoruz
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //Sadece en dıs cercevedeki pikselleri yakala yani x=0 ya da y=0 gibi
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                    //Bu kısım ise aslnda bir dogrulama icin yani robot sarj istasyonuna geri dönebilsin diye yolu yanlıslıkla
                    //kapatmamak için korumaya alıyor
                    if (!(x == robotModel.getStationX() && y == robotModel.getStationY())) {
                        Cell c = roomGrid.getCell(x, y);
                        if (c != null) {
                            c.setObstacleType(ObstacleType.WALL);
                            c.setObstacleRootX(x);
                            c.setObstacleRootY(y);
                        }
                    }
                }
            }
        }

        this.roomGrid.findUnreachableZones(robotModel.getStationX() , robotModel.getStationY());
    }

    public void setView(SimulationView view) {
        this.view = view;
        setupGameLoop();
        updateGridUI();
        updateStatsUI();
    }

    public void initKey(Scene scene) {
        //Eger klavyeden bir tusa basılırsa bu event object fırlatılır
        scene.setOnKeyPressed(event -> {
        });
    }


    //Simülasyon kontrolu
    public void startSimulation() {
        if (timeline != null) {
            //Tam bu kisimda JavaFX timeline baslatark döngütü tetikler
            timeline.play();
            if (view != null) {
                view.setPause(false);
            }
        }
    }

    public void pauseSimulation() {
        if (timeline != null) {
            //Bu bir kontrol satiridir timeline gercekten arkaplanda calisiyor mu
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.pause();
                if (view != null) {
                    view.setPause(true);
                }
            } else {
                timeline.play();
                if (view != null) {
                    view.setPause(false);
                }
            }
        }
    }

    //Sarj bitince istasyona donmeyi saglar
    public void returnToStation() {
        robotModel.setCurrentMode("RETURNING_TO_STATION");
        //En kisa yolu hesaplamak icin kullanilir
        returnPath = robotModel.calcShortestPathStation(roomGrid);
    }

    //Hizi dinamik olarak gunceller
    public void adjustSpeed(double speedValue) {
        if (timeline != null) {
            timeline.setRate(speedValue);
        }
    }

    //batarya yi manuel olarak ayarlamayi saglar
    public void handleUpdateBattery(double newBatteryLevel) {
        double validLevel = Math.max(0.0, Math.min(100.0, newBatteryLevel));
        robotModel.setBattery(validLevel);
        if (validLevel <= 0.0) {
            handleOutOfBattery();
            return;
        }
        if (validLevel > 20.0 && ("OUT_OF_BATTERY".equals(robotModel.getCurrentMode()) || "RETURNING_TO_STATION".equals(robotModel.getCurrentMode()))) {
            robotModel.setCurrentMode("CLEANING");
            robotModel.setDirection("Doğu (+)");
            returnPath.clear();
            cleanPath.clear();
            startSimulation();
        } else if (validLevel > 0.0 && "OUT_OF_BATTERY".equals(robotModel.getCurrentMode())) {
            robotModel.setCurrentMode("CLEANING");
            robotModel.setDirection("Doğu (+)");
            returnPath.clear();
            cleanPath.clear();
            startSimulation();
        }
        updateStatsUI();
        updateGridUI();
    }

    private void handleOutOfBattery() {
        robotModel.setBattery(0.0);
        robotModel.setCurrentMode("OUT_OF_BATTERY");
        robotModel.setDirection("Batarya Bitti (Sıkıştı)");
        updateGridUI();
        updateStatsUI();
        showBatteryOrStuck();
    }

    private void showBatteryOrStuck() {
        javafx.application.Platform.runLater(() -> {
            if (timeline != null) {
                timeline.pause();
            }
            if (view != null) {
                view.setPause(true);
            }
            getDefaultToolkit().beep();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            if (view != null && view.getRootView() != null && view.getRootView().getScene() != null) {
                alert.initOwner(view.getRootView().getScene().getWindow());
            }
            alert.setTitle("Durum Uyarısı");
            alert.setHeaderText(null);
            alert.setContentText("Robot süpürgenin şarjı bitti / sıkıştı.");

            ButtonType btnContinue = new ButtonType("Devam Et");
            ButtonType btnTerminate = new ButtonType("Sonlandır");
            alert.getButtonTypes().setAll(btnContinue, btnTerminate);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnContinue) {
                robotModel.setCurrentMode("CLEANING");
                robotModel.setDirection("Doğu (+)");
                returnPath.clear();
                cleanPath.clear();
                updateStatsUI();
                updateGridUI();
            } else {
                showFinishAlert();
            }
        });
    }

    //Simulasyon bittigi zaman bir uyari ve pop-up ekrani verir
    private void showFinishAlert() {
        Platform.runLater(() -> {
            getDefaultToolkit().beep();
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            if (view != null && view.getRootView() != null && view.getRootView().getScene() != null) {
                infoAlert.initOwner(view.getRootView().getScene().getWindow());
            }
            infoAlert.setTitle("Simülasyon Bitti");
            infoAlert.setHeaderText(null);
            infoAlert.setContentText("Simülasyon sonlandırıldı.");
            infoAlert.showAndWait();

            resetSimulation();
            //En basa doner
        });
    }

    //Temizlik bitince / gorev tamamlaninca simulasyon tamamlansin
    private void simulationComplete() {
        if (timeline != null) {
            timeline.pause();
        }
        if (view != null) {
            view.setPause(true);
        }
        Platform.runLater(() -> {
            getDefaultToolkit().beep();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            if (view != null && view.getRootView() != null && view.getRootView().getScene() != null) {
                alert.initOwner(view.getRootView().getScene().getWindow());
            }
            alert.setTitle("Temizlik Tamamlandı");
            alert.setHeaderText(null);
            alert.setContentText("Temizlenecek yer kalmadı! Simülasyon başarıyla tamamlandı.");
            alert.showAndWait();

            resetSimulation();
        });
    }

    //GameLoop
    private void setupGameLoop() {
        timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        if (view != null && view.getSpeedSlider() != null) {
            timeline.setRate(view.getSpeedSlider().getValue());
        }
    }

    //Robotun yasam dongusu
    private void tick() {
        String mode = robotModel.getCurrentMode();
        //Pil bitmisse sadece UI yi gunceller
        if ("OUT_OF_BATTERY".equals(mode)) {
            updateGridUI();
            updateStatsUI();
            return;
        }
        //Batarya bistmisse sistemi guvenli moda alir
        if (robotModel.getBattery() <= 0.0 && !"CHARGING".equals(mode)) {
            handleOutOfBattery();
            return;
        }
        //Hemen sureyi arttirip eski konuma gider
        elapsedTime++;
        int prevX = robotModel.getX();
        int prevY = robotModel.getY();
        if ("CHARGING".equals(mode)) {
            robotModel.charge();
            if (robotModel.getBattery() >= 100.0) {
                if (hasMoreDirt()) {
                    robotModel.setCurrentMode("CLEANING");
                } else {
                    simulationComplete();
                }
            }
        } else if ("RETURNING_TO_STATION".equals(mode)) {
            if (!returnPath.isEmpty()) {
                Cell nextCell = returnPath.poll();
                robotModel.move(nextCell.getX(), nextCell.getY());
            } else {
                if (robotModel.getX() == robotModel.getStationX() && robotModel.getY() == robotModel.getStationY()) {
                    robotModel.setCurrentMode("CHARGING");
                    if (!hasMoreDirt()) {
                        simulationComplete();
                    }
                } else {
                    returnToStation();
                }
            }
        } else if ("CLEANING".equals(mode)) {
            if (robotModel.getBattery() <= 20.0) {
                //Sarj 20 nin altina duserse geri doner
                returnToStation();
            } else {
                CleaningMove();
            }
        }
        String updatedMode = robotModel.getCurrentMode();
        //Robot sıkısmıs mı anlamayi saglar bunun icin x ve y sürekli sabit kalmali ki hareket etmedigi belli olsun
        if (robotModel.getX() == prevX && robotModel.getY() == prevY) {
            if ("CLEANING".equals(updatedMode) || "RETURNING_TO_STATION".equals(updatedMode)) {
                robotModel.setBattery(Math.max(0.0, robotModel.getBattery() - 0.5));
            }
        }
        if (robotModel.getBattery() <= 0.0 && !"CHARGING".equals(updatedMode)) {
            handleOutOfBattery();
            return;
        }
        //Uzerinden gecilen yeşil oluyor ama geri dönüşte olmuyor (güncelledik)
        Cell currentCell = roomGrid.getCell(robotModel.getX(), robotModel.getY());
        // Sadece robot "CLEANING" (Temizleme) modundayken geçtiği yerleri yeşil (temizlenmiş) yap.
        // İstasyona dönerken ("RETURNING_TO_STATION") boyama işlemini atla.
        if (currentCell != null && "CLEANING".equals(robotModel.getCurrentMode())) {
            currentCell.setCleaned(true);
        }
        updateGridUI();
        updateStatsUI();
    }

    //Temizlik Sureci
    private void CleaningMove() {
        int curx = robotModel.getX();
        int cury = robotModel.getY();
        Cell currentCell = roomGrid.getCell(curx, cury);
        if (ticksLeft > 0 || (currentCell != null && currentCell.isDirty())) {
            if (ticksLeft == 0) {
                currentDirt = currentCell.getDirtType();
                ticksLeft = (int) currentDirt.getCleaningMoment();
                //Kir tipine göre sure ayarlar
            }
            ticksLeft--;//Temizlk islemine tick e göre ilerletir
            if (ticksLeft == 0) {
                //Temizlik  tamamsa
                if (currentDirt != null) {
                    robotModel.consumeExtraBattery(currentDirt); //Kir temizlendi bu yüzden batarya azalir
                    dustCount++;
                    if (view != null) {
                        view.playCleaningAnimation(curx, cury, currentDirt);
                    }
                    currentDirt = null;
                }
                currentCell.setDirtType(DirtType.NONE);
                //Hucreyi temiz olarak isaretle
            }
            return;
            //Temizlik bitene kadar dur ve hareket etme
        }
        //Eğer aktif bir navigasyon rotamız varsa önce oraya gider aradaki temizlenmiş hücrelerden geçer
        if (cleanPath != null && !cleanPath.isEmpty()) {
            Cell nextCell = cleanPath.poll();
            robotDx = nextCell.getX() - curx;
            robotDy = nextCell.getY() - cury;
            updateDirection(robotDx, robotDy);
            robotModel.move(nextCell.getX(), nextCell.getY());
            return;
        }
        //Temizlik Modu
        Toggle selectedToggle = view.getAlgoGroup().getSelectedToggle();
        String algo = selectedToggle != null ? (String) selectedToggle.getUserData() : "SPIRAL";
        // Rastgele/ Düz git temizlenmemiş yeri tercih et sıkışırsa veya önü kapalıysa yön değiştir
        if ("RANDOM".equals(algo)) {
            int tx = curx + robotDx;
            int ty = cury + robotDy;
            Cell target = roomGrid.getCell(tx, ty);
            // Düz gitme koşulu sudur Hedef hücresi engelsiz erişilebilir olmalı ve DAHA ÖNCE TEMİZLENMEMİŞ olmalı
            if (target != null && !target.isObstacle() && !target.isCleaned()) {
                robotModel.move(tx, ty);
            } else {
                // Önümüz kapalı veya zaten temizlenmiş
                // 4 yön içinden henüz temizlenmemiş ve engelsiz olan yönleri bulmaliyiz
                int[][] dirs = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } }; // Kuzey,güney,batı,doğu
                List<int[]> uncleanedDirs = new ArrayList<>();
                for (int[] d : dirs) {
                    Cell c = roomGrid.getCell(curx + d[0], cury + d[1]);
                    if (c != null && !c.isObstacle() && !c.isCleaned()) {
                        uncleanedDirs.add(d);
                    }
                }
                // Temizlenmemiş bir yön bulduysak o tarafa git  bulamadıysak BFS ile en yakın hücreye gidisat hesapla
                if (!uncleanedDirs.isEmpty()) {
                    // Temizlenmemiş yönlerden birini seç
                    int[] chosenDir = uncleanedDirs.get(random.nextInt(uncleanedDirs.size()));
                    robotDx = chosenDir[0];
                    robotDy = chosenDir[1];
                    updateDirection(robotDx, robotDy);
                    robotModel.move(curx + robotDx, cury + robotDy);
                } else {
                    // Etrafta temizlenmemiş yakın hücre kalmadı!
                    // Tüm odadaki en yakın temizlenmemiş hücreye BFS ile rota çizelim
                    cleanPath = findClosestDirt();
                    if (cleanPath != null && !cleanPath.isEmpty()) {
                        Cell nextCell = cleanPath.poll();
                        robotDx = nextCell.getX() - curx;
                        robotDy = nextCell.getY() - cury;
                        updateDirection(robotDx, robotDy);
                        robotModel.move(nextCell.getX(), nextCell.getY());
                    } else {
                        // Odada temizlenmemiş hiç hücre kalmadıysa istasyona dönsün
                        returnToStation();
                    }
                }
            }
        }
        // sistematik genişleyen kare yani spiral
        else if ("SPIRAL".equals(algo)) {
            int[][] spiralDirs = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } }; // Doğu,güney,batı,kuzey
            if (spiralLength <= 0) {
                spiralLength = 1;
                spiralCount = 0;
                spiralIdx = 0;
            }
            int[] dir = spiralDirs[spiralIdx];
            int tx = curx + dir[0];
            int ty = cury + dir[1];
            Cell target = roomGrid.getCell(tx, ty);
            // Düz gitme ve spiral çizme icin gereken Hedef hücresi engelsiz erişilebilir olmalı ve temizlenmemiş olmalı
            if (target != null && !target.isObstacle() && !target.isCleaned()) {
                robotModel.move(tx, ty);
                robotDx = dir[0];
                robotDy = dir[1];
                updateDirection(robotDx, robotDy);
                spiralCount++;
                if (spiralCount >= spiralLength) {
                    spiralCount = 0;
                    spiralIdx = (spiralIdx + 1) % 4;
                    if (spiralIdx == 0 || spiralIdx == 2) {
                        spiralLength++;
                    }
                }
            } else {
                // Çarpışma sınır veya temizlenmiş alan olunca spirali bozar
                spiralLength = 1;
                spiralCount = 0;
                // En yakın temizlenmemiş hücreye gitmek için rota ayarla
                cleanPath = findClosestDirt();
                if (cleanPath != null && !cleanPath.isEmpty()) {
                    Cell nextCell = cleanPath.poll();
                    robotDx = nextCell.getX() - curx;
                    robotDy = nextCell.getY() - cury;
                    updateDirection(robotDx, robotDy);
                    robotModel.move(nextCell.getX(), nextCell.getY());
                } else {
                    // Temizlenecek yer kalmadıysa istasyona dön
                    returnToStation();
                }
            }
        }
        // WALL_FOLLOW Duvar Takip/ Duvarı sağ tarafta tutarak takip edecek
        else if ("WALL_FOLLOW".equals(algo)) {
            int[][] wallDirs = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } }; // Doğu, Güney, Batı, Kuzey
            // Mevcut yönün indeksini bul
            int dirIndex = -1;
            for (int i = 0; i < 4; i++) {
                if (wallDirs[i][0] == robotDx && wallDirs[i][1] == robotDy) {
                    dirIndex = i;
                    break;
                }
            }
            if (dirIndex == -1) {
                dirIndex = 0;
                robotDx = wallDirs[0][0];
                robotDy = wallDirs[0][1];
            }
            int rightDirIndex = (dirIndex + 1) % 4;
            int leftDirIndex = (dirIndex + 3) % 4;
            Cell frontCell = roomGrid.getCell(curx + robotDx, cury + robotDy);
            Cell rightCell = roomGrid.getCell(curx + wallDirs[rightDirIndex][0], cury + wallDirs[rightDirIndex][1]);
            Cell behindRightCell = roomGrid.getCell(curx - robotDx + wallDirs[rightDirIndex][0], cury - robotDy + wallDirs[rightDirIndex][1]);
            boolean frontIsWall = isWall(frontCell);
            boolean rightIsWall = isWall(rightCell);
            boolean behindRightIsWall = isWall(behindRightCell);

            // Çevremizde herhangi bir duvar var mı kontrol eder
            boolean touchingWall = isWall(frontCell) || isWall(rightCell) ||
                    isWall(roomGrid.getCell(curx + wallDirs[leftDirIndex][0], cury + wallDirs[leftDirIndex][1])) ||
                    isWall(roomGrid.getCell(curx - robotDx, cury - robotDy));
            if (touchingWall) {
                if (rightIsWall) {
                    if (!frontIsWall) {
                        // Sağımızda duvar var ve önümüz açık o zaman düz git
                        robotModel.move(curx + robotDx, cury + robotDy);
                    } else {
                        // Sağımızda duvar var ama önümüz kapalı oyleyse sola dön
                        dirIndex = (dirIndex + 3) % 4;
                        robotDx = wallDirs[dirIndex][0];
                        robotDy = wallDirs[dirIndex][1];
                        updateDirection(robotDx, robotDy);
                    }
                } else {
                    // Sağımız boş
                    if (behindRightIsWall) {
                        // Sağ arkamızda duvar varmış ve şimdi sağımız boş oyleyse Köşeyi döndük -->Sağa dön ve ilerle
                        dirIndex = (dirIndex + 1) % 4;
                        robotDx = wallDirs[dirIndex][0];
                        robotDy = wallDirs[dirIndex][1];
                        updateDirection(robotDx, robotDy);
                        robotModel.move(curx + robotDx, cury + robotDy);
                    } else {
                        // Sağımız ve sağ arkamız boş ama başka bir duvara dokunuyoruz -->sol veya ön
                        if (!frontIsWall) {
                            // Önümüz açık o zaman Düz git
                            robotModel.move(curx + robotDx, cury + robotDy);
                        } else {
                            // Önümüz duvar o zman Sola dön
                            dirIndex = (dirIndex + 3) % 4;
                            robotDx = wallDirs[dirIndex][0];
                            robotDy = wallDirs[dirIndex][1];
                            updateDirection(robotDx, robotDy);
                        }
                    }
                }
            } else {
                // Etrafta hiç duvar yoksa düz giderek duvar arar
                if (!frontIsWall) {
                    robotModel.move(curx + robotDx, cury + robotDy);
                } else {
                    // Önümüze duvar çıktıysa sola dön -->duvar sağımızda kalacak
                    dirIndex = (dirIndex + 3) % 4;
                    robotDx = wallDirs[dirIndex][0];
                    robotDy = wallDirs[dirIndex][1];
                    updateDirection(robotDx, robotDy);
                }
            }
        }
    }

    //Yon Guncelleme
    private void updateDirection(int dx, int dy) {
        // JavaFX koordinat sisteminde orijin (0,0) sol üst köşededir
        //  Bu yüzden Y ekseni aşağı doğru Güney artar
        if (dx == 1)
            robotModel.setDirection("Doğu (+)");
        else if (dx == -1)
            robotModel.setDirection("Batı (-)");
        else if (dy == 1)
            robotModel.setDirection("Güney (-)");
        else if (dy == -1)
            robotModel.setDirection("Kuzey (+)");
    }

    //Simulasyon sıfırlama
    public void resetSimulation() {
        if (timeline != null) {
            timeline.stop();
        }
        if (view != null) {
            view.setPause(false);
        }
        //Sayac rota hepsini sifirlar
        dustCount = 0;
        cleanPath.clear();
        ticksLeft = 0;
        currentDirt = null;
        elapsedTime = 0;
        returnPath.clear();
        // Algoritma yönlerini sıfırla
        robotDx = 1;
        robotDy = 0;
        spiralLength = 0;
        spiralCount = 0;
        spiralIdx = 0;
        robotModel.setY(robotModel.getStationY());
        robotModel.setX(robotModel.getStationX());
        robotModel.setBattery(100.0);
        robotModel.setCurrentMode("CLEANING");
        robotModel.setDirection("Doğu (+)");
        //Tüm grid i temizle
        for (int y = 0; y < roomGrid.getHeight(); y++) {
            for (int x = 0; x < roomGrid.getWidth(); x++) {
                Cell cell = roomGrid.getCell(x, y);
                cell.setDirtType(DirtType.NONE);
                cell.setUnreachable(false);
                cell.setCleaned(false);
                cell.setObstacleType(ObstacleType.NONE);
            }
        }
        // Sınır duvarlarını yeniden yerleştir
        for (int y = 0; y < roomGrid.getHeight(); y++) {
            for (int x = 0; x < roomGrid.getWidth(); x++) {
                if (y == 0 || y == roomGrid.getHeight() - 1 || x == 0 || x == roomGrid.getWidth() - 1) {
                    if (!(x == robotModel.getStationX() && y == robotModel.getStationY())) {
                        Cell c = roomGrid.getCell(x, y);
                        if (c != null) {
                            c.setObstacleType(ObstacleType.WALL);
                            c.setObstacleRootX(x);
                            c.setObstacleRootY(y);
                        }
                    }
                }
            }
        }
        roomGrid.findUnreachableZones(robotModel.getStationX(), robotModel.getStationY());
        //Arayuzu de ayni sekilde yenile ve temizle
        updateGridUI();
        updateStatsUI();
    }
    public void handleCellClick(int x, int y) {
        Cell cell = roomGrid.getCell(x, y);
        if (cell == null || (x == robotModel.getStationX() && y == robotModel.getStationY())) {
            return;
        }

        Toggle selectedToggle = view.getToolGroup().getSelectedToggle();
        if (selectedToggle == null)
            return; // Hiçbir araç seçili değilse tıklamayı yoksay
        Object selectedTool = selectedToggle.getUserData();
        // Eğer seçilen araç bir kir ise (silgi hariç) ve tıklanan hücre bir engelse (duvar/mobilya), kir eklemeyi engelle
        if (selectedTool instanceof DirtType && selectedTool != DirtType.NONE && cell.isObstacle()) {
            return;
        }
        // 1. Aynı engel tipine tekrar tıklandıysa: Tıklama ile Döndürme Döngüsü (0 -> 90 -> 180 -> 270 -> 0)
        if (selectedTool instanceof ObstacleType && cell.getObstacleType() == selectedTool) {
            int rx = cell.getObstacleRootX();
            int ry = cell.getObstacleRootY();
            int currentRot = cell.getObstacleRotation();
            int nextRot = (currentRot + 90) % 360;
            ObstacleType obs = (ObstacleType) selectedTool;
            // Geçici olarak engeli kaldırıp yeni rotasyonun sığıp sığmadığını test edelim
            for (int gy = 0; gy < roomGrid.getHeight(); gy++) {
                for (int gx = 0; gx < roomGrid.getWidth(); gx++) {
                    Cell c = roomGrid.getCell(gx, gy);
                    if (c != null && c.getObstacleRootX() == rx && c.getObstacleRootY() == ry) {
                        c.setObstacleType(ObstacleType.NONE);
                    }
                }
            }
            int baseW = view.getObstacleWidth(obs);
            int baseH = view.getObstacleHeight(obs);
            int w = (nextRot == 90 || nextRot == 270) ? baseH : baseW;
            int h = (nextRot == 90 || nextRot == 270) ? baseW : baseH;
            boolean fits = (rx + w <= roomGrid.getWidth() && ry + h <= roomGrid.getHeight());
            // İstasyon veya robot ile çakışıyor mu
            if (fits) {
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int tx = rx + dx;
                        int ty = ry + dy;
                        if (tx == robotModel.getX() && ty == robotModel.getY())
                            fits = false;
                        if (tx == robotModel.getStationX() && ty == robotModel.getStationY())
                            fits = false;
                    }
                }
            }
            if (fits) {
                // Sığıyorsa yeni yönüyle yerleştir
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int tx = rx + dx;
                        int ty = ry + dy;
                        Cell targetCell = roomGrid.getCell(tx, ty);
                        if (targetCell != null) {
                            targetCell.setObstacleType(obs);
                            targetCell.setObstacleRootX(rx);
                            targetCell.setObstacleRootY(ry);
                            targetCell.setObstacleRotation(nextRot);
                            targetCell.setDirtType(DirtType.NONE);
                            targetCell.setCleaned(false);
                        }
                    }
                }
            } else {
                // Sığmıyorsa eski yönüyle geri yerleştir (iptal et)
                int prevW = (currentRot == 90 || currentRot == 270) ? baseH : baseW;
                int prevH = (currentRot == 90 || currentRot == 270) ? baseW : baseH;
                for (int dy = 0; dy < prevH; dy++) {
                    for (int dx = 0; dx < prevW; dx++) {
                        int tx = rx + dx;
                        int ty = ry + dy;
                        Cell targetCell = roomGrid.getCell(tx, ty);
                        if (targetCell != null) {
                            targetCell.setObstacleType(obs);
                            targetCell.setObstacleRootX(rx);
                            targetCell.setObstacleRootY(ry);
                            targetCell.setObstacleRotation(currentRot);
                        }
                    }
                }
            }

            roomGrid.findUnreachableZones(robotModel.getStationX(), robotModel.getStationY());
            updateGridUI();
            updateStatsUI();
            return;
        }
        // 2. Silgi seçildiyse engeli veya kiri temizle
        if (selectedTool == DirtType.NONE) {
            int rx = cell.getObstacleRootX();
            int ry = cell.getObstacleRootY();
            if (rx != -1 && ry != -1) {
                // Bu engelle aynı köke (root) sahip tüm hücreleri temizle
                for (int gy = 0; gy < roomGrid.getHeight(); gy++) {
                    for (int gx = 0; gx < roomGrid.getWidth(); gx++) {
                        Cell c = roomGrid.getCell(gx, gy);
                        if (c != null && c.getObstacleRootX() == rx && c.getObstacleRootY() == ry) {
                            c.setObstacleType(ObstacleType.NONE);
                            c.setCleaned(false);
                        }
                    }
                }
            } else {
                cell.setObstacleType(ObstacleType.NONE);
                cell.setDirtType(DirtType.NONE);
                cell.setCleaned(false);
            }
            roomGrid.findUnreachableZones(robotModel.getStationX(), robotModel.getStationY());
            updateGridUI();
            updateStatsUI();
            return;
        }
        //Yeni Engel Ekleme
        if (selectedTool instanceof ObstacleType) {
            ObstacleType obs = (ObstacleType) selectedTool;
            int baseW = view.getObstacleWidth(obs);
            int baseH = view.getObstacleHeight(obs);
            // Rotasyona göre genişlik ve yüksekliği belirle
            int w = (currentrotation == 90 || currentrotation == 270) ? baseH : baseW;
            int h = (currentrotation == 90 || currentrotation == 270) ? baseW : baseH;
            // Sınırlara sığıyor mu kontrol et
            if (x + w > roomGrid.getWidth() || y + h > roomGrid.getHeight()) {
                return;
            }
            // İstasyon, Robot veya başka bir engel ile çakışıyor mu kontrol et
            for (int dy = 0; dy < h; dy++) {
                for (int dx = 0; dx < w; dx++) {
                    int tx = x + dx;
                    int ty = y + dy;
                    Cell targetCell = roomGrid.getCell(tx, ty);
                    if (targetCell == null) return;
                    if (tx == robotModel.getX() && ty == robotModel.getY())
                        return;
                    if (tx == robotModel.getStationX() && ty == robotModel.getStationY())
                        return;
                    if (targetCell.isObstacle()) {
                        return; // Hedefte zaten başka bir engel (duvar/mobilya) varsa ekleme
                    }
                }
            }
            // Engeli yerleştir
            for (int dy = 0; dy < h; dy++) {
                for (int dx = 0; dx < w; dx++) {
                    int tx = x + dx;
                    int ty = y + dy;
                    Cell targetCell = roomGrid.getCell(tx, ty);
                    if (targetCell != null) {
                        targetCell.setObstacleType(obs);
                        targetCell.setObstacleRootX(x);
                        targetCell.setObstacleRootY(y);
                        targetCell.setObstacleRotation(currentrotation);
                        targetCell.setDirtType(DirtType.NONE);
                        targetCell.setCleaned(false);
                    }
                }
            }
        }
        // Kir Ekleme
        else if (selectedTool instanceof DirtType) {
            DirtType dirt = (DirtType) selectedTool;
            cell.setDirtType(dirt);
            cell.setCleaned(false);
        }
        roomGrid.findUnreachableZones(robotModel.getStationX(), robotModel.getStationY());
        updateGridUI();
        updateStatsUI();
    }

    private void updateGridUI() {
        if (view != null) {
            view.updateGridUI(roomGrid, robotModel, robotDx, robotDy, returnPath);
        }
    }

    private void updateStatsUI() {
        if (view != null) {
            view.updateStatsUI(robotModel, roomGrid, elapsedTime, dustCount);
        }
    }

    private boolean isWall(Cell cell) {
        return cell == null || cell.isObstacle();
    }

    //En yakin kir e ulasamak icin kullanilacak yol
    private Queue<Cell> findClosestDirt() {
        int startX = robotModel.getX();
        int startY = robotModel.getY();
        Cell startCell = roomGrid.getCell(startX, startY);
        if (startCell == null) {
            return new LinkedList<>();
        }
        Queue<Cell> queue = new LinkedList<>();
        Set<Cell> visited = new HashSet<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        queue.add(startCell);
        visited.add(startCell);
        int[][] directions = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } }; // Kuzey, Güney, Batı, Doğu
        Cell targetCell = null;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            // En yakın temizlenmemiş hücreyi bulduk
            if (!current.isObstacle() && !current.isCleaned()) {
                targetCell = current;
                break;
            }
            for (int[] dir : directions) {
                int nx = current.getX() + dir[0];
                int ny = current.getY() + dir[1];
                Cell neighbor = roomGrid.getCell(nx, ny);
                // Komşuları gezerken
                if (neighbor != null && !neighbor.isObstacle() && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        Queue<Cell> path = new LinkedList<>();
        if (targetCell != null) {
            java.util.List<Cell> route = new java.util.ArrayList<>();
            Cell curr = targetCell;
            while (curr != null && !curr.equals(startCell)) {
                route.add(curr);
                curr = parentMap.get(curr);
            }
            Collections.reverse(route);
            path.addAll(route);
        }
        return path;
    }

    private boolean hasMoreDirt() {
        for (int y = 0; y < roomGrid.getHeight(); y++) {
            for (int x = 0; x < roomGrid.getWidth(); x++) {
                Cell cell = roomGrid.getCell(x, y);
                if (cell != null && !cell.isObstacle() && !cell.isUnreachable() && !cell.isCleaned()) {
                    return true;
                }
            }
        }
        return false;
    }
}
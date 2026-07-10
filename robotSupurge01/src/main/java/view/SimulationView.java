package view;

import controller.SimulationController;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.*;
import model.Cell;

import java.net.URL;


public class SimulationView {

    // View Bilesenleri
    private BorderPane rootView;
    private GridPane gridPane;
    private StackPane[][] cellPanes;
    private Rectangle[][] cellBackgrounds;
    private ImageView[][] cellImageViews;
    // Ihtiyacimiz olan gorseller
    private Image imgSofa;
    private Image imgTv;
    private Image imgTable;
    private Image imgChair;
    private Image imgStation;
    private Image imgDust;
    private Image imgLiquid;
    private Image imgStain;
    private Image imgRobot;
    private Image imgFloor;
    // Kullanici arayuzu icin gerekneler
    private ToggleGroup toolGroup;
    private ToggleGroup algoGroup;
    private Label lblLocation;
    private Label lblBatText;
    private Rectangle batteryFill;
    private Slider batterySlider;
    private boolean isUpdatingSlider = false;
    private Label lblTotalArea;
    private Label lblCleanedAreaPercentage;
    private Label lblCollectedDust;
    private ToggleButton btnToggleFloor;
    private Label lblTime;
    private Slider speedSlider;
    private Button btnPause;
    private Label lblLeftArea;
    private Label lblDirection;
    //Boyutlari ayarlamak icin gerekenler
    private final int CELL_SIZE = 40; // Boyutları 40pixel ekran sınırlarına tam sığması için uyumlu
    private final double sizeUpFurniture = 1.0; // Mobilyaları %45 daha büyük göstererek 3D derinlik kazandırır
    private final int gridWidth;
    private final int gridHeight;

    public SimulationView(int width, int height, SimulationController controller) {
        this.gridWidth = width;
        this.gridHeight = height;
        this.cellPanes = new StackPane[height][width];
        this.cellBackgrounds = new Rectangle[height][width];
        this.cellImageViews = new ImageView[height][width];
        loadImages();
        buildUI(controller);
    }
    private void loadImages() {
        imgSofa = loadImage("sofa.png");
        imgTv = loadImage("tv.png");
        imgTable = loadImage("table.png");
        imgChair = loadImage("chair.png");
        imgStation = loadImage("station.png");
        imgDust = loadImage("dust.png");
        imgLiquid = loadImage("liquid.png");
        imgStain = loadImage("stain.png");
        imgRobot = loadImage("robot.png");
        imgFloor = loadImage("floor.png");
    }
    //Kaynak Yonetimi
    // Görsellerin belleğe yüklenmesi sırasında oluşabilecek dosya yolu
    // ve NullPointerException hatalarına karşı önelm almak icin bir yapı kurulmuştur.
    private Image loadImage(String filename) {

        try {
            // Dosya yolunun başındaki "/"  projenin kök dizininden aramaya başlar http gibi
            URL imageUrl = getClass().getResource("/images/" + filename);
            if (imageUrl != null) {
                Image img = new Image(imageUrl.toExternalForm());
                if (!img.isError()) {
                    return img;
                }
            } else {
                System.out.println("UYARI: Görsel bulunamadı -> " + filename);
            }
        } catch (Exception e) {
            System.out.println("UYARI: Görsel yüklenirken hata -> " + filename);
        }
        return null;
    }
    private void buildUI(SimulationController controller) {
        rootView = new BorderPane();
        rootView.setPadding(new Insets(10));
        // Pencerenin tum ic kenarlarindan ust, sag, alt, sol 10'ar piksel ice dogru bosluk birakir
        rootView.setStyle("-fx-background-color: #0f172a; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        //rootView Ekranin ana iskeletidir BorderPane yapisi bilesenleri ust alt sol sag ve merkez olarak 5 farkli bolgeye yerlestirmemizi saglar

        // Simulasyonun en ustune yazilacak baslik
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        // HBox=Horizontal Box Icine eklenen bilesenleri yatay eksende yan yana dizer
        // Parametre olarak verilen 10 icindeki elemanlarin arasindaki piksel boslugunu belirtir
        headerBox.setPadding(new Insets(0, 0, 8, 0));
        //Label-> Ekranda gorsel veya okunabilir statik metinler olusturmak icin kullanilan siniftir
        Label mainTitle = new Label("🤖 Robot Süpürge Simülasyonu ✨");
        mainTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        headerBox.getChildren().add(mainTitle);
        // Hazirlanan yatay kutuyu, ana iskeletin (BorderPane) en ust bolgesine kilitler.
        rootView.setTop(headerBox);
        HBox body = new HBox(20);
        body.setAlignment(Pos.CENTER);

        // Sol tarafa yazilacak metinler ve gorunumu
        VBox leftPanel = new VBox(6);
        leftPanel.setPrefWidth(280);
        leftPanel.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-padding: 10 12 10 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        //Araclar
        //Yani sol tarafta bulunmasi gereken UI islemleri ve secenekleri
        //Butun araclari benzer mantiklarla kuruyoruz
        Label lblTools = new Label("🔧 Araçlar");
        lblTools.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-font-weight: bold;");
        toolGroup = new ToggleGroup();
        Label lblDirtType = new Label("Kir Türü");
        lblDirtType.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: bold;");
        // VBox iskeleti icinde yer alacak olan bu etiketin etrafina ust:4, sag:0, alt:2, sol:0  (dis bosluga) atanir
        VBox.setMargin(lblDirtType, new Insets(4, 0, 2, 0));
        HBox dirtRow = new HBox(5);
        ToggleButton tbDust = new ToggleButton("💨 Toz");
        // veri tutarsizligina yol acabilecegi icin dogrudan enum referansi setUserData ile buton nesnesinin icine gomuldu Bu mimari View-Controller iletisimini guvenli kilar.
        tbDust.setUserData(DirtType.DUST);
        tbDust.setToggleGroup(toolGroup);
        styleToggleButton(tbDust, "secondary");
        //Bu yorum satiri Kir türü icin ortak bir bilgilendirme icerir
        // Aşağıdaki konfigürasyon yapısı diger tüm araç butonları (Toz, Leke, Koltuk vb.) icin de geçerlidir.
        ToggleButton tbLiquid = new ToggleButton("💧 Sıvı");
        tbLiquid.setUserData(DirtType.LIQUID);
        // setUserData--> Arayüz View butonunun icine Controller'ın isleyebilecegi veri tipini (enum) gizili veri olarak ayarlar
        tbLiquid.setToggleGroup(toolGroup);
        //setToggleGroup butonu araca ozel gruba baglar ki aynı anda iki arac secilemesin
        styleToggleButton(tbLiquid, "secondary");
        //styleToggleButton --> Görsel bütünlük için CSS kodlarını tekrar etmeyip yardımcı metoda yollar
        ToggleButton tbStain = new ToggleButton("💥 Leke");
        tbStain.setUserData(DirtType.STAIN);
        tbStain.setToggleGroup(toolGroup);
        styleToggleButton(tbStain, "secondary");
        dirtRow.getChildren().addAll(tbDust, tbLiquid, tbStain);
        //Nesneleri ekleyelim
        Label lblObjects = new Label("Nesneler");
        lblObjects.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox.setMargin(lblObjects, new Insets(8, 0, 2, 0));
        //setMaxWidth(Double.MAX_VALUE) kullanim-->, butonlarin icinde bulunduklari grid hucrelerinin  veya VBox ların genisligine gore otomatik
        // esnemesini saglar. Boylece UI hizalamasi bozulmaz
        ToggleButton tbSofa = new ToggleButton("🛋 Koltuk");
        tbSofa.setUserData(ObstacleType.SOFA);
        tbSofa.setToggleGroup(toolGroup);
        tbSofa.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbSofa, "pink");
        // Uygulama basladiginda kullaniciyi yonlendirmek amaciyla default arac olarak atanir.
        tbSofa.setSelected(true);
        ToggleButton tbTv = new ToggleButton("📺 TV Ünitesi");
        tbTv.setUserData(ObstacleType.TV);
        tbTv.setToggleGroup(toolGroup);
        tbTv.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbTv, "pink");
        ToggleButton tbTable = new ToggleButton("┳ Sehpalı Halı");
        tbTable.setUserData(ObstacleType.TABLE);
        tbTable.setToggleGroup(toolGroup);
        tbTable.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbTable, "pink");
        ToggleButton tbChair = new ToggleButton("💺 Berjer");
        tbChair.setUserData(ObstacleType.CHAIR);
        tbChair.setToggleGroup(toolGroup);
        tbChair.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbChair, "pink");
        //Zemin islemleri
        //2 zemin var biri fayans(image ile eklendi) digeri ise sade mavi zemin
        btnToggleFloor = new ToggleButton("▦ Zemin Ekle");
        btnToggleFloor.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(btnToggleFloor, "secondary");
        btnToggleFloor.setOnAction(e -> {
            if (btnToggleFloor.isSelected()) {
                btnToggleFloor.setText("▦ Zemini Kaldır");
                enableFloorBackground();
            } else {
                btnToggleFloor.setText("▦ Zemin Ekle");
                disableFloorBackground();
            }
        });
        // Duvar islemleri
        ToggleButton tbWall = new ToggleButton("🚧 Duvar Çiz");
        tbWall.setUserData(ObstacleType.WALL);
        tbWall.setToggleGroup(toolGroup);
        tbWall.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbWall, "pink");
        //Silgi islemi icin Controller tarafinda ayri bir durum kontrolu yazmak yerine hucreleri temizlemek adina DirtType.NONE referansi atanmistir.
        // Bu sayede mevcut temizlik mantigi bozulmadan silme islemi entegre edilmis olur.
        ToggleButton tbEraser = new ToggleButton("❌ Silgi");
        tbEraser.setUserData(DirtType.NONE);
        tbEraser.setToggleGroup(toolGroup);
        tbEraser.setMaxWidth(Double.MAX_VALUE);
        styleToggleButton(tbEraser, "danger");

        // Mobilya ve araç butonlarını 2 sütunlu grid'e yerleştirelim
        GridPane toolsGrid = new GridPane();
        toolsGrid.setHgap(8);
        toolsGrid.setVgap(8);
        //Sutun genislikleri piksel  yerine yuzdelik 50-50olarak ayarlanarak arayuzun farkli pencere boyutlarinda bozulmasi engellendi
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        toolsGrid.getColumnConstraints().addAll(col1, col2);
        // Bilesenler (0,0) koordinatindan baslayarak (Sutun, Satir) mantigiyla matrise dizilir.
        toolsGrid.add(tbSofa, 0, 0);
        toolsGrid.add(tbTv, 1, 0);
        toolsGrid.add(tbTable, 0, 1);
        toolsGrid.add(tbChair, 1, 1);
        toolsGrid.add(btnToggleFloor, 0, 2);
        toolsGrid.add(tbWall, 1, 2);
        // Silgi butonunun kullanici acisindan daha erisilebilir olmasi ve alt boslugu doldurmasi icin GridPane.add() metodunun overload edilmis
        // versiyonu tercih edilmistir.
        // add(Node, colIndex, rowIndex, colSpan, rowSpan) parametreleri ile silgi butonunun 2 sutuna (colSpan = 2) yayilmasi saglanmistir.
        toolsGrid.add(tbEraser, 0, 3, 2, 1);

        // Kullanicinin simulasyon animasyon hizini dinamik olarak degistirebilmesi icin yapilandirilmis aralikli Slider bileseni.
        Label lblSpeed = new Label("⏱ Robot Hızı: x1,5");
        lblSpeed.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-font-weight: bold;");
        //Min: 0.5 Max: 5.0 Default: 1.5 degerleriyle baslatilir.
        speedSlider = new Slider(0.5, 5.0, 1.5);
        speedSlider.setBlockIncrement(0.5);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setMinorTickCount(1);
        //setSnapToTicks(true) ile kaydirici serbest birakilmaz,
        // kullanici gorsel olarak belirli araliklara 0.5 gibi zorlanir.
        speedSlider.setSnapToTicks(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setStyle("-fx-cursor: hand; -fx-tick-label-fill: #cbd5e1;");
        //Slider degeri degistikce anlik Controller tetiklenir Math.round algaritmasi kullanilarak, ara degerlerin
        //kusursuz bir sekilde 0.5'in katlarina yuvarlanmasi matematiksel olarak garanti altina alinmistir.
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double roundedVal = Math.round(newVal.doubleValue() * 2.0) / 2.0;
            controller.adjustSpeed(roundedVal);
            //String format ile tek ondalik alinip, Turkce formatlama standartlarina uymasi adina nokta virgul ile degistirilmistir.
            lblSpeed.setText(String.format("⏱ Robot Hızı: x%.1f", roundedVal).replace('.', ','));
        });

        //Temizlik algoritmasi
        //Bu kisim temizligin yapilirken nasıl bir yon ve gidis ayarlayacagini belirler
        Label lblAlgo = new Label("⚙ Temizlik Algoritması");
        lblAlgo.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-font-weight: bold;");
        algoGroup = new ToggleGroup();
        // Arka planda calisan 'RANDOM' algoritmasinin heuristik ciktisi sahada agirlikli olarak spiral bir yorunge cizdigi icin
        // son kullaniciya  bu davranis Spiral seklinde bilerek lamse edilmistir.
        RadioButton rbRandom = new RadioButton("Spiral");
        rbRandom.setUserData("RANDOM");
        rbRandom.setToggleGroup(algoGroup);
        rbRandom.setStyle("-fx-text-fill: #cbd5e1; -fx-cursor: hand;");
        RadioButton rbSpiral = new RadioButton("Rastgele");
        rbSpiral.setUserData("SPIRAL");
        rbSpiral.setToggleGroup(algoGroup);
        rbSpiral.setStyle("-fx-text-fill: #cbd5e1; -fx-cursor: hand;");
        RadioButton rbWallFollow = new RadioButton("Duvar Takip");
        rbWallFollow.setUserData("WALL_FOLLOW");
        rbWallFollow.setToggleGroup(algoGroup);
        rbWallFollow.setStyle("-fx-text-fill: #cbd5e1; -fx-cursor: hand;");
        // Uygulama basladiginda default algoritma olarak atanir
        rbSpiral.setSelected(true);
        VBox algoBox = new VBox(4, rbRandom, rbSpiral, rbWallFollow);
        //Gerekli islemleri controller sinnifinda yapildi

        //Robotun durumu
        VBox statusBox = new VBox(4);
        statusBox.setStyle(
                "-fx-background-color: #0f172a; -fx-background-radius: 12; -fx-padding: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label lblStatus = new Label("🤖 Robot Durumu");
        // Verilerin duzenli bir formatda (sutun ,satir) gosterilmesi icin GridPane kullanilmistir
        lblStatus.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-font-weight: bold;");
        GridPane statusGrid = new GridPane();
        statusGrid.setHgap(30);
        statusGrid.setVgap(4);
        // Label bilesenleri ilk olusturulurken sahte verilerle baslatilmistir test amacli boylee yapildi
        lblLocation = new Label("(11, 7)");
        lblLocation.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        lblDirection = new Label("Doğu (+)");
        lblDirection.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 12px;");
        lblBatText = new Label("22%");
        lblBatText.setStyle("-fx-text-fill: gold; -fx-font-size: 12px;");

        // Statik metin etiketleri
        Label l1 = new Label("Konum (x, y)");
        l1.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label l2 = new Label("Yön");
        l2.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label l3 = new Label("Batarya");
        l3.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        statusGrid.add(l1, 0, 0);
        statusGrid.add(lblLocation, 1, 0);
        statusGrid.add(l2, 0, 1);
        statusGrid.add(lblDirection, 1, 1);
        statusGrid.add(l3, 0, 2);
        statusGrid.add(lblBatText, 1, 2);

        //JavaFX'in standart ProgressBar bileseni gorsel olarak kisitli olduguna StackPane icerisinde iki farkli Rectangle
        // ust uste bindirilerek tamamen ozellestirilebilir bir batarya gostergesi tasarlanmisdir
        StackPane batBar = new StackPane();
        //Batarya icin olusturulan dikdortgeninin yuzdeye gore genisligi azaldiginda daima sola yaslanarak kuculmesi icin hizalama yapilmistir.
        batBar.setAlignment(Pos.CENTER_LEFT);
        // Batarya iskeleti Koyu gri zemin ve kavisli koselerden olusuyor
        Rectangle batBg = new Rectangle(180, 15, Color.web("#334155"));
        batBg.setArcWidth(10);
        batBg.setArcHeight(10);
        batBg.setStroke(Color.web("#64748b"));
        batteryFill = new Rectangle(180, 15, Color.web("#eab308"));
        batteryFill.setArcWidth(10);
        batteryFill.setArcHeight(10);
        // Arka plan alta dolgu uste gelecek sekilde  Stack da siralanir
        batBar.getChildren().addAll(batBg, batteryFill);

        // Simulasyon sirasinda kullanicinin batarya seviyesine disaridan dogrudan mudahale edebilmesi test amaciyle vb  icin eklenmis kontrol etiketidir
        Label lblManualBat = new Label("Manuel Batarya Seviyesi:");
        // Batarya icerisinde ki seviye Sari renk ve  Controller uzerinden width degeri dinamik guncellenecek
        lblManualBat.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        VBox.setMargin(lblManualBat, new Insets(6, 0, 0, 0));

        batterySlider = new Slider(0, 100, 100);
        batterySlider.setShowTickMarks(false);
        batterySlider.setShowTickLabels(false);
        batterySlider.setStyle("-fx-cursor: hand;");
        //Bu sayede batarya kismi supurge calisirken bile bataryayi degistirebilmeyi saglar
        batterySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingSlider) {
                controller.handleUpdateBattery(newVal.doubleValue());
                // Kullanicinin simulasyonun calismasi  esnasinda bataryayi anlik kontrol edebilmesi icin Observer
                // tasarim deseni ile Property dinlenmektedir.
            }
        });
        //!isUpdatingSlider kullanilarak sistem tarafindan yapilan guncellemeler ile kullanici tarafindan
        //yapilan manuel mudahaleler birbirinden izole edilmistir.
        statusBox.getChildren().addAll(lblStatus, statusGrid, batBar, lblManualBat, batterySlider);

        //Supurge uzerinden yapilacak kontroller
        Label lblControls = new Label("🎮 Kontroller");
        lblControls.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox playPause = new HBox(10);
        Button btnStart = new Button("▶ Başlat");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnStart, Priority.ALWAYS);
        //Baslat ve Duraklat butonlarinin yatay eksende ayni genisligi 50 - 50 esit olarak paylasmasi icin HBox icerisinde
        // Priority.ALWAYS parametresi kullanilmistir Bu sayede pencere boyutu degisse bile butonlar simetrik olarak esner.
        styleButton(btnStart, "primary");
        btnPause = new Button("⏸ Duraklat");
        btnPause.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnPause, Priority.ALWAYS);
        styleButton(btnPause, "secondary");
        playPause.getChildren().addAll(btnStart, btnPause);
        Button btnReset = new Button("🟥 Sıfırla");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        styleButton(btnReset, "danger");
        Button btnReturn = new Button("🏠 İstasyona Dön");
        btnReturn.setMaxWidth(Double.MAX_VALUE);
        styleButton(btnReturn, "primary");

        //View uzerindeki buton tetiklemeleri dogrudan isleme mantıgı
        // icermez Tum kulanici arayuz olaylari (e -> ...) araciligiyla Controller sinifina delege edilir.
        // Bu pratik View ve Controller arasindaki siki bagliligi onler.
        btnStart.setOnAction(e -> controller.startSimulation());
        btnPause.setOnAction(e -> controller.pauseSimulation());
        btnReset.setOnAction(e -> controller.resetSimulation());
        btnReturn.setOnAction(e -> controller.returnToStation());

        leftPanel.getChildren().addAll(
                lblTools, lblDirtType, dirtRow, lblObjects, toolsGrid,lblSpeed, speedSlider,
                lblAlgo, algoBox, statusBox, lblControls, playPause, btnReset, btnReturn);
        // Robotun hareket edecegi ana Grid ve kullanicinin konum takibini yapabilmesi
        // icin Controller'dan gelen gridWidth ve gridHeigh e gore arayuze dinamik olarak uretilen eksen cizimleri bu panelde yonetilir
        VBox rightPanel = new VBox();
        rightPanel.setAlignment(Pos.CENTER);
        //Ust X ekseni koordinatlari
        HBox topCoords = new HBox();
        // X ekseni numaralarini alttaki griddin sutunlariyla hizalamak icin sol tarafa y ekseni etiketlerinin kapladigi alana kadar kaydirma padding degeri uygulanmistir
        topCoords.setPadding(new Insets(0, 0, 5, CELL_SIZE + 5));
        // Harita genisligine gore X ekseni dinamik olarak uretilir.
        for (int i = 0; i < gridWidth; i++) {
            Label cl = new Label(String.valueOf(i));
            cl.setPrefWidth(CELL_SIZE);
            // Etiket genisligi, izgara hucresi genisligine sabitlenir
            cl.setAlignment(Pos.CENTER);
            cl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
            topCoords.getChildren().add(cl);
        }
        //Sol Y ekseni koordinatlari
        HBox gridWithY = new HBox(5);
        VBox leftCoords = new VBox();
        // Harita yuksekligine gore Y ekseni hucreleri dinamik olarak uretilir.
        for (int i = 0; i < gridHeight; i++) {
            Label cl = new Label(String.valueOf(i));
            cl.setPrefHeight(CELL_SIZE);
            // Etiket yuksekligi grid hucresi yuksekligine sabitlenir
            cl.setAlignment(Pos.CENTER);
            cl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
            leftCoords.getChildren().add(cl);
        }
        //Grid kismi insaasi
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;");
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                // Arka plan yani zemin, renk vb ve On plan Engel robot vb Gorseli nesnelerini
                // ust uste bindirebilmek icin StackPane kullanilmistir.
                StackPane stackPane = new StackPane();
                stackPane.setStyle("-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 0.5;");

                Rectangle bg = new Rectangle(CELL_SIZE, CELL_SIZE);
                bg.setFill(Color.TRANSPARENT);
                // Baslangicta tum hucreler bos ve seffaftir
                ImageView icon = new ImageView();
                // Ikonlarin hucre kenarlarina yapismasini engellemek icin -2 piksel kucultulup
                // iceriye 1'er piksel kaydirma yapildi.
                // setManaged(false) cagirilarak StackPane'in otomatik hizalama ozelligi
                // devre disi birakilmis boylece manuel koordinat atamalari gecerli kilinmistir.
                icon.setLayoutX(1);
                icon.setFitWidth(CELL_SIZE - 2);
                icon.setPreserveRatio(true);
                icon.setLayoutY(1);
                icon.setFitHeight(CELL_SIZE - 2);
                icon.setManaged(false);
                stackPane.getChildren().addAll(bg, icon);
                // Java'da (e -> ) ifadeleri dongu degiskenlerini yani x ve y'yi dogrudan alamaz
                // Sadece degistirilemez degiskenler okunabilecegi icin mevcut koordinatlar yerel sabitlere (cx, cy) kopyalanarak Controller'a  aktarilmistir.
                final int cx = x;
                final int cy = y;
                stackPane.setOnMouseClicked(e -> controller.handleCellClick(cx, cy));
                // Simulasyon calisirken user inteface agacini surekli calistirmak performansi dusurur. Bu yuzden her hucrenin arka plan ve ikon referanslari hizinda erisilebilmesi
                // icin 2 boyutlu dizilerde onbellege alinmistir.
                cellPanes[y][x] = stackPane;
                cellBackgrounds[y][x] = bg;
                cellImageViews[y][x] = icon;
                gridPane.add(stackPane, x, y);
            }
        }
        gridWithY.getChildren().addAll(leftCoords, gridPane);
        rightPanel.getChildren().addAll(topCoords, gridWithY);
        body.getChildren().addAll(leftPanel, rightPanel);
        rootView.setCenter(body);
        // Ana govde-->Sol panel ve Sag Matris BorderPane Center a yerlestirilir.
        // Alt Panel yani istatistikler
        HBox bottomPanel = new HBox(40);
        bottomPanel.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        bottomPanel.setAlignment(Pos.CENTER);
        // Alt panelin center a yapismasini onlemek icin ustten 15pixel  dis bosluk verilir.
        BorderPane.setMargin(bottomPanel, new Insets(15, 0, 0, 0));
        // Her bir istatistik icin ayri ayri VBox/Label hiyerarsisi kurmak
        // kod tekrarina sebep olur bu yuzden 'createStatBox' adinda bir yardimci
        // metot kullanilmistir. Bu yapi ui guncellemelerini tek bir merkeze toplar ve kod okunabilirligini artirir.
        lblTotalArea = createStatBox(bottomPanel, "🔵", "Toplam Alan", "0 m²", "#2563eb");
        lblCleanedAreaPercentage = createStatBox(bottomPanel, "🟢", "Temizlenen Alan", "0 m² (0%)", "#22c55e");
        lblLeftArea = createStatBox(bottomPanel, "⚪", "Kalan Alan", "0 m² (0%)", null);
        lblTime = createStatBox(bottomPanel, "🕒", "Geçen Süre", "00:00", "#ec4899");
        lblCollectedDust = createStatBox(bottomPanel, "✨", "Toplanan Kir", "0 birim", "#eab308");
        // Hazirlanan panel BorderPane iskeletinin en altina kilitlenerek arayuz insasi tamamlanir
        rootView.setBottom(bottomPanel);
    }


    // Arayüzün alt kısmında bulunan istatistik kutucuklarını (ikon, başlık ve değer) üreten standart metot.
    private Label createStatBox(HBox parent, String iconStr, String title, String value, String iconColor) {
        // ana kutuyu oluşturuyor.
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        //İkonu (emojiyi) ayarlıyor ve istenen renge boyuyor.
        Label icon = new Label(iconStr);
        if (iconColor != null) {
            icon.setStyle("-fx-font-size: 20px; -fx-text-fill: " + iconColor + ";");
        } else {
            icon.setStyle("-fx-font-size: 20px;");
        }
        // "Geçen Süre" ve "00:00" metin sütununu oluştur
        VBox textCol = new VBox(2);
        Label lblTitle = new Label(title); // Üstte küçük, gri renkte başlık
        lblTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Label lblValue = new Label(value); // Altta büyük, kalın ve beyaz renkte sayısal değer
        lblValue.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        textCol.getChildren().addAll(lblTitle, lblValue);
        //İkonu ve metinleri ana yatay kutuya ekleyip arayüze (parent) yerleştir
        box.getChildren().addAll(icon, textCol);
        parent.getChildren().add(box);
        return lblValue; // Simülasyon sırasında değerin güncellenebilmesi için sadece değer(lblValue) etiketini geri döndür
    }
    // Hazırlanan tüm arayüzün bulunduğu en dış ana paneli döndürür.(MainApp sınıfından çağır)
    public BorderPane getRootView() {
        return rootView;
    }
    // Sol paneldeki bağlı oldukları grupları döndüren metotlarımız, Controller sınıfından çağırılır.
    //1- engel ve nesne butonları(Silgi, Koltuk, Toz vb.)/haritaya tıklandığında hangi aracın seçili olduğunu anlamak için bunu kullanır.
    public ToggleGroup getToolGroup() {
        return toolGroup;
    }
    //2-"Temizlik Algoritması" seçenekleri/robotun hangi mantıkla hareket edeceğini (rotasını) anlamak için bu metodu kullanır.
    public ToggleGroup getAlgoGroup() {
        return algoGroup;
    }
    //3-robot hızını kontrol eden kaydırma çubuğu(Slider)/simülasyon animasyonlarını başlatırken oyun hızını ayarlamak için bunu kullanır.
    public Slider getSpeedSlider() {
        return speedSlider;
    }
    // Seçilen engelin haritada yatay eksende kaç hücre(cell) genişlik kaplayacağını belirler.
    public int getObstacleWidth(ObstacleType type) {
        if (type == ObstacleType.SOFA) return 1; // Koltuk: 1 hücre genişliğinde
        if (type == ObstacleType.TV) return 1; // TV Ünitesi: 1 hücre genişliğinde
        if (type == ObstacleType.TABLE) return 2; // Sehpalı Halı: 2 hücre genişliğinde
        if (type == ObstacleType.CHAIR) return 1; // Berjer: 1 hücre genişliğinde
        return 1; // duvar varsayılan olarak 1 kare genişliğinde
    }
    // Seçilen engelin haritada dikey eksende kaç hücre yükseklik kaplayacağını belirler.
    public int getObstacleHeight(ObstacleType type) {
        if (type == ObstacleType.SOFA) return 3; // Koltuk: 3 hücre uzunluğunda
        if (type == ObstacleType.TV) return 3; // TV Ünitesi: 3 hücre uzunluğunda
        if (type == ObstacleType.TABLE) return 2; // Sehpalı Halı: 2 hücre uzunluğunda
        if (type == ObstacleType.CHAIR) return 1; // Berjer: 1 hücre uzunluğunda
        return 1; // duvar varsayılan olarak 1 kare uzunluğunda
    }
    // Simülasyonun her her hücresi çağrılarak haritadaki tüm görselleri, renkleri ve rotaları ekrana çizer.
    public void updateGridUI(RoomGrid roomGrid, RobotModel robotModel, int robotDx, int robotDy, java.util.Queue<Cell> returnPath) {
        for (int y = 0; y < roomGrid.getHeight(); y++) {
            for (int x = 0; x < roomGrid.getWidth(); x++) {
                Cell cell = roomGrid.getCell(x, y);
                Rectangle bg = cellBackgrounds[y][x];
                ImageView icon = cellImageViews[y][x];
                // Varsayılan hücre boyutlarını ve yönünü sıfırla
                icon.setFitWidth(CELL_SIZE - 2);
                icon.setFitHeight(CELL_SIZE - 2);
                icon.setRotate(0);
                icon.setLayoutX(1);
                icon.setLayoutY(1);
                icon.setPreserveRatio(true);
                // Eski rota çizgilerini (kesik çizgiler) hücreden temizle
                cellPanes[y][x].getChildren().removeIf(node -> node instanceof Line);
                // Ulaşılamaz yerleri gölgelendir, temizlenen yerleri yeşil yap
                if (cell.isUnreachable()) {
                    bg.setFill(Color.rgb(0, 0, 0, 0.4)); // Koyu/Gri gölgeli
                } else if (cell.isCleaned()) {
                    bg.setFill(Color.web("rgba(34, 197, 94, 0.12)")); // Hafif yeşil iz
                } else {
                    bg.setFill(Color.TRANSPARENT);
                }
                Image imageToSet = null;
                if (x == robotModel.getX() && y == robotModel.getY()) {
                    cellPanes[y][x].toFront();
                    if (imgRobot != null && !imgRobot.isError()) {
                        imageToSet = imgRobot;
                        double robotRot = 0;
                        if (robotDx == 1 && robotDy == 0) robotRot = 90; // East
                        else if (robotDx == 0 && robotDy == 1) robotRot = 180; // South
                        else if (robotDx == -1 && robotDy == 0) robotRot = 270; // West
                        else if (robotDx == 0 && robotDy == -1) robotRot = 0; // North
                        icon.setRotate(robotRot);
                    } else {
                        bg.setFill(Color.RED);
                    }
                } else if (x == robotModel.getStationX() && y == robotModel.getStationY()) {
                    cellPanes[y][x].toFront();
                    if (imgStation != null && !imgStation.isError()) {
                        imageToSet = imgStation;
                        double imgW = imgStation.getWidth();
                        double imgH = imgStation.getHeight();
                        double ratio = (imgW > 0) ? (imgH / imgW) : 1.0;

                        double visualWidth = CELL_SIZE - 2;
                        double visualHeight = visualWidth * ratio;

                        icon.setFitWidth(visualWidth);
                        icon.setFitHeight(visualHeight);
                        icon.setLayoutX(1);
                        icon.setLayoutY(CELL_SIZE - 1 - visualHeight);
                    } else {
                        bg.setFill(Color.web("#15803d")); // İstasyonun Yeşili
                    }
                } else if (cell.isObstacle()) {
                    // mobilyaları çiz
                    int rx = cell.getObstacleRootX();
                    int ry = cell.getObstacleRootY();
                    ObstacleType type = cell.getObstacleType();
                    int rotation = cell.getObstacleRotation();
                    // Sadece kök hücresinde görseli çiz
                    if (x == rx && y == ry) {
                        cellPanes[y][x].toFront();
                        if (type == ObstacleType.SOFA && imgSofa != null && !imgSofa.isError()) imageToSet = imgSofa;
                        else if (type == ObstacleType.TV && imgTv != null && !imgTv.isError()) imageToSet = imgTv;
                        else if (type == ObstacleType.TABLE && imgTable != null && !imgTable.isError()) imageToSet = imgTable;
                        else if (type == ObstacleType.CHAIR && imgChair != null && !imgChair.isError()) imageToSet = imgChair;

                        if (imageToSet != null) {
                            double imgW = imageToSet.getWidth();
                            double imgH = imageToSet.getHeight();
                            double ratio = (imgW > 0) ? (imgH / imgW) : 1.0;

                            int baseW = getObstacleWidth(type);
                            int baseH = getObstacleHeight(type);
                            // Eşyayı kapladığı hücre sayısına göre genişlet
                            double visualWidth = (CELL_SIZE * baseW - 2) * sizeUpFurniture;
                            double visualHeight = (CELL_SIZE * baseH - 2) * sizeUpFurniture;

                            icon.setFitWidth(visualWidth);
                            icon.setFitHeight(visualHeight);
                            icon.setPreserveRatio(false);
                            icon.setRotate(rotation);

                            int w = (rotation == 90 || rotation == 270) ? baseH : baseW;
                            int h = (rotation == 90 || rotation == 270) ? baseW : baseH;

                            double fpW = w * CELL_SIZE;
                            double fpH = h * CELL_SIZE;

                            icon.setLayoutX((fpW - visualWidth) / 2.0);
                            icon.setLayoutY((fpH - visualHeight) / 2.0);
                        }
                    }
                    // Görsel yüklenemediyse veya yoksa, tüm hücreleri renkle doldur
                    boolean hasImage = false;
                    if (type == ObstacleType.SOFA && imgSofa != null && !imgSofa.isError()) hasImage = true;
                    else if (type == ObstacleType.TV && imgTv != null && !imgTv.isError()) hasImage = true;
                    else if (type == ObstacleType.TABLE && imgTable != null && !imgTable.isError()) hasImage = true;
                    else if (type == ObstacleType.CHAIR && imgChair != null && !imgChair.isError()) hasImage = true;

                    if (!hasImage) {
                        if (type == ObstacleType.WALL) {
                            bg.setFill(Color.web("#334155")); // gri duvar rengi
                        } else {
                            bg.setFill(Color.web("#8B4513")); // Kahverengi engel rengi
                        }
                    }
                } else if (cell.getDirtType() == DirtType.DUST) {
                    bg.setFill(Color.web("#9ca3af"));
                    if (imgDust != null && !imgDust.isError()) imageToSet = imgDust;
                } else if (cell.getDirtType() == DirtType.LIQUID) {
                    bg.setFill(Color.web("#3b82f6"));
                    if (imgLiquid != null && !imgLiquid.isError()) imageToSet = imgLiquid;
                } else if (cell.getDirtType() == DirtType.STAIN) {
                    bg.setFill(Color.web("#451a03"));
                    if (imgStain != null && !imgStain.isError()) imageToSet = imgStain;
                }
                icon.setImage(imageToSet);  // Bulunan resmi (robot, toz, mobilya) ekrana bas
            }
        }
        // İstasyona dönüş rotasını kesik çizgilerle çiz
        if ("RETURNING_TO_STATION".equals(robotModel.getCurrentMode()) && returnPath != null && !returnPath.isEmpty()) {
            java.util.List<Cell> pathList = new java.util.ArrayList<>(returnPath);
            Cell robotCell = roomGrid.getCell(robotModel.getX(), robotModel.getY());
            if (robotCell != null && !pathList.contains(robotCell)) {
                pathList.add(0, robotCell);
            }
            for (int i = 0; i < pathList.size() - 1; i++) {
                Cell curr = pathList.get(i);
                Cell next = pathList.get(i + 1);
                int dx = next.getX() - curr.getX();
                int dy = next.getY() - curr.getY();
                Line line = new Line(
                        CELL_SIZE / 2.0, CELL_SIZE / 2.0,
                        CELL_SIZE / 2.0 + dx * CELL_SIZE, CELL_SIZE / 2.0 + dy * CELL_SIZE);
                line.setStroke(Color.web("#22c55e")); // Açık yeşil rota rengi
                line.setStrokeWidth(3.5);
                line.getStrokeDashArray().addAll(6.0, 6.0); // Çizgiyi kesik kesik yap
                line.setManaged(false);
                StackPane pane = cellPanes[curr.getY()][curr.getX()];
                if (pane.getChildren().size() >= 2) {
                    pane.getChildren().add(1, line);
                } else {
                    pane.getChildren().add(line);
                }
            }
        }
    }
    // Robot bir kiri temizlediğinde, kirin küçülerek ve silinerek kaybolma animasyonunu oynatır.
    public void playCleaningAnimation(int x, int y, DirtType dirtType) {
        Node animNode;
        Image targetImage = null;
        if (dirtType == DirtType.DUST) targetImage = imgDust; // kir tipine göre hangi kirin animasyonunun oynatılacağını belirler
        else if (dirtType == DirtType.LIQUID) targetImage = imgLiquid;
        else if (dirtType == DirtType.STAIN) targetImage = imgStain;
// Kirin geçici bir kopyasını oluştur (Resim yoksa renkli bir hücre)
        if (targetImage != null && !targetImage.isError()) {
            ImageView tempIcon = new ImageView(targetImage);
            tempIcon.setFitWidth(CELL_SIZE - 2);
            tempIcon.setFitHeight(CELL_SIZE - 2);
            animNode = tempIcon;
        } else {
            Rectangle tempRect = new Rectangle(CELL_SIZE, CELL_SIZE);
            if (dirtType == DirtType.DUST) tempRect.setFill(Color.LIGHTGRAY);
            else if (dirtType == DirtType.LIQUID) tempRect.setFill(Color.LIGHTBLUE);
            else if (dirtType == DirtType.STAIN) tempRect.setFill(Color.DARKGREEN);
            animNode = tempRect;
        }
        gridPane.add(animNode, x, y); // Animasyon için hazırlanan geçici kiri haritaya (gridPane) ekle

        FadeTransition ft = new FadeTransition(Duration.millis(500), animNode);
        ft.setFromValue(1.0); //Şeffaflaşma Efekti: Yarım saniye içinde görünürlüğü 1'den 0'a indir
        ft.setToValue(0.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(500), animNode);
        st.setToX(0.0); //Küçülme Efekti: Yarım saniye içinde boyutu X ve Y ekseninde sıfırla
        st.setToY(0.0);
//İki efekti aynı anda oynat ve bitince geçici nodu ekrandan sil
        ParallelTransition pt = new ParallelTransition(animNode, ft, st);
        pt.setOnFinished(e -> gridPane.getChildren().remove(animNode)); // Çöp (Memory leak) oluşmasını engeller
        pt.play();
    }
    // Simülasyonun her adımında ekrandaki metinleri, süreleri ve batarya çubuğunu günceller.
    public void updateStatsUI(RobotModel robotModel, RoomGrid roomGrid, int secondsElapsed, int collectedDustCount) {
        // 1. Robotun o anki X,Y konumunu ve baktığı yönü ekrana yaz
        lblLocation.setText(String.format("(%d, %d)", robotModel.getX(), robotModel.getY()));
        lblDirection.setText(robotModel.getDirection());
        double bat = robotModel.getBattery(); // 2. Batarya çubuğunu güncelle
        lblBatText.setText(String.format("%.0f%%", bat));// Yüzdelik metni yaz
        batteryFill.setWidth((bat / 100.0) * 180.0); // Çubuğun genişliği
        if (bat <= 20.0) { // Batarya %20 ve altındaysa kırmızı (tehlike), değilse sarı renk yap
            lblBatText.setTextFill(Color.web("#ef4444"));
            batteryFill.setFill(Color.web("#ef4444"));
        } else {
            lblBatText.setTextFill(Color.web("#eab308"));
            batteryFill.setFill(Color.web("#eab308"));
        }
// 3. batarya çubuğunu (Slider'ı) güncelle
        isUpdatingSlider = true;
        if (batterySlider != null) {
            batterySlider.setValue(bat);
        }
        isUpdatingSlider = false; //(Tetiklenmeyi önlemek için bayrak kullan)
// 4. Haritadaki temizlenebilir ve temizlenmiş alanları say
        int totalCleanable = 0;
        int cleanedCount = 0;
        for (int y = 0; y < roomGrid.getHeight(); y++) {
            for (int x = 0; x < roomGrid.getWidth(); x++) {
                Cell c = roomGrid.getCell(x, y);
                if (!c.isObstacle()) { // Engelleri hesaba katma
                    totalCleanable++;
                    if (c.isCleaned()) {
                        cleanedCount++;
                    }
                }
            }
        }
        lblTotalArea.setText(totalCleanable + " m²"); // Alt paneldeki alan istatistiklerini (m² ve % olarak) ekrana yaz
        double pct = roomGrid.getCleanedPercentage();
        lblCleanedAreaPercentage.setText(String.format("%d m² (%%%d)", cleanedCount, (int) pct));
        lblLeftArea.setText(String.format("%d m² (%%%d)", totalCleanable - cleanedCount, 100 - (int) pct));
        int m = secondsElapsed / 60; // 5. Toplam saniyeyi Dakika:Saniye (MM:SS) formatına çevir
        int s = secondsElapsed % 60;
        lblTime.setText(String.format("%02d:%02d", m, s));
// 6. Hazneye çekilen toplam kir miktarını yaz
        lblCollectedDust.setText(collectedDustCount + " birim");
    }
    // Butonların görünümü (renk, yuvarlak köşe) ve tıklanma animasyonu
    private void styleButton(Button btn, String type) {
        // Bütün butonlar için geçerli olan ortak temel tasarım (kalın yazı, yuvarlak köşe)
        String baseStyle = "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12 5 12; -fx-font-size: 12px;";
        String colorStyle = "";
        // Butonun işlevine göre renk teması ve renk değişimleri
        if ("primary".equals(type)) { // Ana butonlar (Başlat, İstasyona Dön)
            colorStyle = "-fx-background-color: #2563eb; -fx-text-fill: white;"; // Koyu Mavi
            btn.setOnMouseEntered(
                    e -> btn.setStyle(baseStyle + "-fx-background-color: #3b82f6; -fx-text-fill: white;")); // Açık Mavi
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle + "-fx-background-color: #2563eb; -fx-text-fill: white;"));
        } else if ("success".equals(type)) { // // Başarılı işlemler (Success) için yeşil buton teması
            colorStyle = "-fx-background-color: #16a34a; -fx-text-fill: white;";
            btn.setOnMouseEntered(
                    e -> btn.setStyle(baseStyle + "-fx-background-color: #22c55e; -fx-text-fill: white;")); // Açık Yeşil
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle + "-fx-background-color: #16a34a; -fx-text-fill: white;"));
        } else if ("danger".equals(type)) { // Silme/Sıfırlama butonları
            colorStyle = "-fx-background-color: #dc2626; -fx-text-fill: white;";
            btn.setOnMouseEntered(
                    e -> btn.setStyle(baseStyle + "-fx-background-color: #ef4444; -fx-text-fill: white;")); // Koyu Kırmızı
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle + "-fx-background-color: #dc2626; -fx-text-fill: white;"));
        } else if ("secondary".equals(type)) { // İkincil butonlar
            colorStyle = "-fx-background-color: #334155; -fx-text-fill: white;";
            btn.setOnMouseEntered(
                    e -> btn.setStyle(baseStyle + "-fx-background-color: #475569; -fx-text-fill: white;")); // Açık Gri
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle + "-fx-background-color: #334155; -fx-text-fill: white;"));
        }
        btn.setStyle(baseStyle + colorStyle); // Temel stili ve seçilen renk stilini birleştirerek butona uygula
    }
    // Aç/Kapa özellikli butonların seçili olma ve seçili olmama durumlarına göre tasarımları
    private void styleToggleButton(ToggleButton btn, String type) {
        // Ortak temel tasarım (kalın font, yuvarlak köşe)
        String baseStyle = "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12 5 12; -fx-font-size: 12px;";
// Butonun o anki durumuna (seçili mi, değil mi) göre rengini hesaplayan fonksiyon
        Runnable updateStyle = () -> {
            String colorStyle;
            if (btn.isSelected()) { // BUTON SEÇİLİYSE: Daha koyu bir renk ve aktif olduğunu belli eden beyaz çerçeve
                if ("primary".equals(type)) {
                    colorStyle = "-fx-background-color: #1d4ed8; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6; -fx-text-fill: white;";
                } else if ("success".equals(type)) {
                    colorStyle = "-fx-background-color: #15803d; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6; -fx-text-fill: white;";
                } else if ("pink".equals(type)) { // Engel butonları için kullanılan pembe tema
                    colorStyle = "-fx-background-color: #9d174d; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6; -fx-text-fill: white;";
                } else if ("danger".equals(type)) { // kırmızı butonların teması
                    colorStyle = "-fx-background-color: #dc2626; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 6; -fx-text-fill: white;";
                } else {
                    colorStyle = "-fx-background-color: #2563eb; -fx-text-fill: white;";
                }
            } else { // BUTON SEÇİLİ DEĞİLSE: Normal, çerçevesiz varsayılan renkler
                if ("primary".equals(type)) {
                    colorStyle = "-fx-background-color: #2563eb; -fx-text-fill: white;";
                } else if ("success".equals(type)) { // Başarılı işlemler (Success) için yeşil buton teması
                    colorStyle = "-fx-background-color: #16a34a; -fx-text-fill: white;";
                } else if ("pink".equals(type)) {
                    colorStyle = "-fx-background-color: #db2777; -fx-text-fill: white;";
                } else if ("danger".equals(type)) {
                    colorStyle = "-fx-background-color: #334155; -fx-text-fill: white;"; // Kırmızı buton kapalıyken gri görünür
                } else {
                    colorStyle = "-fx-background-color: #334155; -fx-text-fill: white;";
                }
            }
            btn.setStyle(baseStyle + colorStyle);
        };
// Butona her tıklandığında (seçim durumu değiştiğinde) stili güncelle
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> updateStyle.run());
// Fare ile üzerine gelindiğinde (Sadece buton seçili DEĞİLSE rengi hafifçe aç)
        btn.setOnMouseEntered(e -> {
            if (!btn.isSelected()) {
                String hoverColor;
                if ("secondary".equals(type)) hoverColor = "#475569";
                else if ("success".equals(type)) hoverColor = "#22c55e";
                else if ("pink".equals(type)) hoverColor = "#ec4899";
                else if ("danger".equals(type)) hoverColor = "#ef4444";
                else hoverColor = "#3b82f6";
                btn.setStyle(baseStyle + "-fx-background-color: " + hoverColor + "; -fx-text-fill: white;");
            }
        });
        btn.setOnMouseExited(e -> updateStyle.run()); // Fare üzerinden çekildiğinde stili tekrar eski haline getir
        updateStyle.run();
    }
    // Simülasyon duraklatıldığında duraklat butonunun yazısını güncelle.
    public void setPause(boolean isPaused) {
        if (isPaused) {
            btnPause.setText("▶ Devam Et");
        } else {
            btnPause.setText("⏸ Duraklat");
        }}
    //arka plana zemin desenini döşer.
    public void enableFloorBackground() {
        if (imgFloor != null && !imgFloor.isError()) { // Eğer zemin resmi başarılı bir şekilde yüklenmişse işlemi başlat
            // Görselde 4x4 karo var. Her bir ızgara hücremiz 40px.
            // Orijinal resmi 160x160 piksele ölçeklersek, 1 karo = 1 hücre (40px) olur.
            // (163.5 değeri, ızgara aralarındaki sınır çizgilerinin yarattığı kaymayı telafi etmek için yaptık)
            BackgroundSize exactSize = new BackgroundSize(163.5, 163.5, false, false, false, false);
            gridPane.setBackground(new Background(new BackgroundImage( // Tüm ızgaranın arka planına bu resmi uygula
                    imgFloor,
                    BackgroundRepeat.REPEAT,    // Yatayda sürekli döşe
                    BackgroundRepeat.REPEAT,    // Dikeyde sürekli döşe
                    BackgroundPosition.DEFAULT, // Döşemeye sol üstten başla
                    exactSize)));                // Hesapladığımız 163.5x163.5 boyutunu kullan
        }
    }
    // Haritanın arka planındaki zemin desenini tamamen temizle ve varsayılan görünüme dön.
    public void disableFloorBackground() {
        gridPane.setBackground(null);
    }
}
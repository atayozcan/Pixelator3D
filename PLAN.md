# Implementation Plan: Parameters & PDF Generation

## Phase 1: Parameter Configuration (F2)

### 1.1 Domain Model - ArtworkConfig
Neue Klasse für alle Parameter:
```java
public class ArtworkConfig {
    private int gridWidth = 50;      // Rasterauflösung X
    private int gridHeight = 50;     // Rasterauflösung Y
    private int colorCount = 16;     // 8, 16, oder 32
    private boolean mode3D = false;  // 2D/3D Modus
    private OutputSize outputSize = OutputSize.A4;
}

public enum OutputSize { A4, A3, A2, A1, A0 }
```

### 1.2 Template erweitern
- `ArtworkConfig` zu Template hinzufügen
- Getter/Setter für Config

### 1.3 PixelationEngine erweitern
- Color Quantization implementieren (Farben auf 8/16/32 reduzieren)
- Grid-basierte Pixelierung (statt pixel-size, jetzt grid-width x grid-height)
- 3D-Effekt Option (Schatten/Tiefe)

### 1.4 ControlPanel UI erweitern
Neue Controls:
- Grid Resolution: 2 Spinner (Width x Height) oder Presets (50x50, 100x100, etc.)
- Color Count: ComboBox (8, 16, 32)
- 2D/3D Toggle: JToggleButton oder CheckBox
- Output Size: ComboBox (A4, A3, A2, A1, A0)
- "Generate PDF" Button

### 1.5 Creator Interface erweitern
```java
void applyConfig(ArtworkConfig config);
void generatePDF(File outputFile);
```

### 1.6 Real-time Preview (<500ms)
- Debounce bei Parameter-Änderungen
- Async Verarbeitung mit SwingWorker
- Nur Vorschau-Qualität (niedrigere Auflösung)

---

## Phase 2: PDF Generation (F5-F8)

### 2.1 PDF Library
- Apache PDFBox hinzufügen (pom.xml dependency)

### 2.2 PDFGenerator Klasse
```java
public class PDFGenerator {
    void generate(Template template, ArtworkConfig config, File output);
}
```

### 2.3 F5: Vollständige PDF-Vorlage
- Titelseite mit Vorschaubild
- Übersicht der verwendeten Materialien
- Rastervorlage
- Legende

### 2.4 F6: Bauanleitung
- Rastergrid mit Koordinaten (A1, A2, B1, B2...)
- Jede Zelle zeigt Farbcode
- Materialliste: "Rot: 45 Stück, Blau: 32 Stück, ..."

### 2.5 F7: Legende
- Farbcode → Materialname/Farbe Mapping
- Beispiel: "R1 = Rot (2cm Stäbchen)"

### 2.6 F8: Tiling (Aufteilung auf A4)
- Wenn OutputSize > A4: automatisch auf mehrere Seiten aufteilen
- Überlappungsmarkierungen für Zusammenkleben
- Seitennummerierung (1/4, 2/4, ...)

---

## Dateien zu ändern/erstellen

### Neue Dateien:
1. `artcreator/domain/ArtworkConfig.java` - Parameter-Klasse
2. `artcreator/domain/OutputSize.java` - Enum für Ausgabegrößen
3. `artcreator/creator/impl/ColorQuantizer.java` - Farbquantisierung
4. `artcreator/creator/impl/PDFGenerator.java` - PDF-Erstellung

### Zu ändern:
1. `pom.xml` - PDFBox dependency
2. `Template.java` - ArtworkConfig hinzufügen
3. `PixelationEngine.java` - Grid-basiert + Color quantization
4. `ControlPanel.java` - Neue UI Controls
5. `Creator.java` - Neue Methoden
6. `CreatorFacade.java` - State validation für neue Methoden
7. `CreatorImpl.java` - Implementierung
8. `Controller.java` - Event Handler für neue Controls

---

## Reihenfolge der Implementierung

1. ArtworkConfig + OutputSize erstellen
2. Template erweitern
3. ControlPanel UI bauen
4. ColorQuantizer implementieren
5. PixelationEngine erweitern
6. Creator Interface + Impl aktualisieren
7. PDFBox dependency hinzufügen
8. PDFGenerator implementieren
9. Tiling implementieren
10. Testen

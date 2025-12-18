# Pixelator3D

Pixel-Art Generator mit PDF-Bauanleitung.

## Features

- Bilder laden (JPG, PNG, GIF, BMP)
- Pixel-Groesse einstellen (2-50)
- Farbanzahl waehlen (8, 16 oder 32 Farben)
- 2D/3D Modus (3D zeigt Staebchen-Anzahl)
- Ausgabegroesse (A4 bis A0 mit automatischem Kacheln)
- PDF-Export mit Bauanleitung, Materialliste und Rastervorlage

## Starten

```bash
mvn compile exec:java -Dexec.mainClass=artcreator.Main
```

Oder als JAR:

```bash
mvn package
java -jar target/swelab-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Architektur

- Hexagonale Architektur (Ports & Adapters)
- Factory/Facade Pattern mit State-Validierung
- Observer Pattern fuer UI-Updates
- State Machine: HOME -> IMAGE_LOADED -> PIXELATED

## Technisch

- Java 25
- Swing UI (Libadwaita-Style)
- PDF-Generator ohne externe Abhaengigkeiten
- Median-Cut Algorithmus fuer Farbquantisierung

## Autoren

Atay Ozcan (95270) & Jose Acena (85534)

SWE Labor WS 2025/26 - HKA

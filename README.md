# Pixelator3D

Ein Bildbearbeitungsprogramm zum Pixelieren von Bildern.

## Was kann es?

- Bilder laden (JPG, PNG, GIF, BMP)
- Pixel-Größe einstellen (2-50)
- Bild pixelieren

## Wie starten?

```bash
mvn compile exec:java -Dexec.mainClass=artcreator.Main
```

Oder JAR bauen:

```bash
mvn package
java -jar target/swelab-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Architektur

- Hexagonale Architektur (Ports & Adapters)
- Factory/Facade Pattern
- Observer Pattern für State-Updates
- State Machine: HOME → IMAGE_LOADED → PIXELATED

## Autoren

- Atay Ozcan (95270)
- Jose Acena (85534)

SWE Labor WS 2025

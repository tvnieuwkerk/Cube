# Cube

## Description
Cube is a JavaFX desktop application that renders an interactive 3D Rubik’s Cube. The app models cubies, applies layer rotations, and updates the 3D scene so you can explore turns and cube rotations using keyboard controls.

## Usage
### Requirements
- Java 21+
- Maven 3.9+

### Run the app
```bash
mvn javafx:run
```

### Controls
- **Face turns:** `F B R L U D`
- **Modifiers:**
  - `Shift` = counter-clockwise
  - `Ctrl` = 180° turn
  - `Alt` = wide move
- **Slice moves:** `M E S`
- **Cube rotations:** `X Y Z`

## Technology Overview
- **Language:** Java 21
- **UI Framework:** JavaFX 21 (3D scene, materials, lighting)
- **Build Tool:** Maven (with javafx-maven-plugin)
- **Testing:** JUnit 5

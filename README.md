# Cube

Built with OpenAI's Codex (CLI and cloud versions), Cube is a JavaFX desktop application that renders an interactive 3D Rubik’s Cube. The app models cubies, applies layer rotations, and updates the 3D scene so you can explore turns and cube rotations using keyboard or mouse controls.

## Description
Cube is a JavaFX desktop application that renders an interactive 3D Rubik’s Cube. The app models cubies, applies layer rotations, and updates the 3D scene so you can explore turns and cube rotations using keyboard or mouse controls. It also includes a help window with animated turn previews, an algorithm runner, and a guided beginner method tab with validation indicators.

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
- **Mouse gestures:** click a face to select it, drag to rotate a face or slice, drag with a selected face to rotate the whole cube, and scroll to zoom.

### Extra features
- **Algorithm runner:** enter notation like `R U R' U'` and run it directly.
- **Help window:** view animated turn previews and keyboard hints.
- **Beginner method guide:** step-by-step cards with runnable algorithms and validation status.

## Technology Overview
- **Language:** Java 21
- **UI Framework:** JavaFX 21 (3D scene, materials, lighting)
- **Build Tool:** Maven (with javafx-maven-plugin)
- **Testing:** JUnit 5

## Beginner solving guide
Open **Help → Beginner Method** for a manual seven-stage guide. Solve the white cross on top,
then press **Ctrl+X** to put white below and yellow above for the remaining stages. Each
algorithm includes its required setup and stopping condition. **Run once** executes only
the displayed sequence; it does not choose a case or prepare the cube automatically.

The completion indicators check both piece locations and sticker directions, including all
earlier stages. They update after every move and can temporarily turn red during the final
corner-twisting procedure. Follow that procedure through all corners and align the top layer.
Camera controls change the viewpoint; they do not change which layer a move turns.

## Tests
Run `mvn test`. The suite covers cube moves and orientations, guide algorithms and staged
solving, plus JavaFX help and animation integration. JavaFX integration tests need an accessible
desktop display (or a virtual display); they are skipped on Linux when `DISPLAY` is unset.

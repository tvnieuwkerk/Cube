# Agent Notes

## Project Details
- **Java version:** Java 21 (see `pom.xml` compiler release).
- **Libraries/frameworks:** JavaFX 21 (`javafx-controls`, `javafx-graphics`).
- **JavaFX architecture:** MVVM-style separation with model (`nl.tvn.cube.model`), view model (`nl.tvn.cube.viewmodel`), and view (`nl.tvn.cube.view`).

## Testing
- **Framework:** JUnit 5.
- **Run tests:**
  ```bash
  mvn test
  ```

## Running the App
```bash
mvn javafx:run
```

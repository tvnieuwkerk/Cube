# Agent Notes

## Project Details
- **Java version:** Java 21 (see `pom.xml` compiler release).
- **Libraries/frameworks:** JavaFX 21 (`javafx-controls`, `javafx-graphics`).
- **JavaFX architecture:** MVVM-style separation with model (`nl.tvn.cube.model`), view model (`nl.tvn.cube.viewmodel`), and view (`nl.tvn.cube.view`).

## Mandatory tasks after finishing prompt
- run all tests with maven
- create changelog.md if it not yet exists in root of project
- add new section at the top of the list describing:
  - date and timestamp (UTC)
  - Title of change
  - summary of change request
  - summary of change request implementation

## Building project
- **Build**
  ```bash
  mvn compile
  ```

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

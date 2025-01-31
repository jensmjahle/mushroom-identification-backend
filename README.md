# bachelor-mushroom-identification-backend
This is a bachelorproject created by Anders Emil Bergan and Jens Martin Jahle. 


## Running the project
### For development
> Note: This will use the dev profile, which is configured to use an in-memory database. This means that the data will not be persisted between restarts.
#### Windows Powershell
```powershell 
mvn spring-boot:run -D"spring-boot.run.profiles=dev"
```
#### Linux/macOS
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### For production
```
mvn spring-boot:run
```

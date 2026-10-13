# SMART

# Commands
- `./mvnw clean javafx:run` - run dev (DEBUG logging by default)
- `./mvnw clean javafx:run -Dlog.level=INFO` - run dev with clean/release logging
- `./mvnw clean test` - run tests
- `./mvnw clean package -Pfat-jar` - build fat JAR
- `java -jar target/smart-0.0.1.jar` - run app (clean INFO logging by default)
- `java -Dlog.level=DEBUG -jar target/smart-0.0.1.jar` - run app with debug logging

# Requirements
- Linux
  - Ubuntu/Debian - dialout
  - Arch - uucp

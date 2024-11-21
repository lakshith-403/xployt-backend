# Xployt Setup with Apache Tomcat 10.1 and Auto-Reload

## Prerequisites

### Install Java 11 or Higher
1. Download and install the latest version of Java (11 or higher)
2. Set the JAVA_HOME environment variable:
   ```bash
   export JAVA_HOME=/path/to/java
   export PATH=$JAVA_HOME/bin:$PATH
   ```
3. Verify installation:
   ```bash
   java -version
   ```

### Install Apache Tomcat 10.1
1. Download Tomcat 10.1 from the Apache Tomcat website
2. Extract it to your desired location (e.g., `/usr/local/tomcat10` or `E:/Tomcat10`)
3. Set the CATALINA_HOME environment variable: (Exorting will only work in the current terminal session)
   ```bash
   export CATALINA_HOME=/path/to/tomcat
   export PATH=$CATALINA_HOME/bin:$PATH
   ```
4. Verify installation: (This should start the server. You can navigate to the tomcat directory and start the server from there as well.)
   ```bash
   # On Linux/Mac
   $CATALINA_HOME/bin/startup.sh
   
   # On Windows
   $CATALINA_HOME/bin/startup.bat
   ```
5. Visit http://localhost:8080 to check if Tomcat is running

### Test Tomcat Restarts 
1. Stop Tomcat: (Navigate to the tomcat directory and stop the server from there as well.)
   ```bash
   # On Linux/Mac
   $CATALINA_HOME/bin/shutdown.sh
   
   # On Windows
   $CATALINA_HOME/bin/shutdown.bat
   ```
2. Start it again to ensure stability

**Note:** If Tomcat fails to start, you may need to edit `startup.bat` or `startup.sh`

Example for Windows `startup.bat`: (I had to make a few changes like these to the original file to get it to work. If any issues come up, give a ring )
```bat
set JAVA_OPTS=-Djava.security.egd=file:/dev/./urandom
```

## Building and Deploying the Project

### Prepare the Codebase
1. Navigate to the project root directory:
   ```bash
   cd /path/to/codebase
   ```

2. Delete the `/target` directory:
   ```bash
   # On Linux/Mac
   rm -rf target
   
   # On Windows
   del /target
   ```

### Run Maven Commands
1. Build the project:
   ```bash
   mvn clean install
   ```
   This will generate `Xployt.war` in the `$CATALINA_HOME/webapps/` directory

### Deploy the WAR File
- Starting the Tomcat server will automatically unpack `Xployt.war` and create a `Xployt` directory in `$CATALINA_HOME/webapps/`

### Check the Application
- Open your browser and navigate to:
  ```
  http://localhost:8080/Xployt
  ```
- Verify if the application is running correctly

## Setting Up Auto-Reload for Code Changes

### Install Nodemon
1. Install nodemon using npm:
   ```bash
   npm install -g nodemon
   ```
   Alternatively, use `npx` to run it directly without global installation

### Run Auto-Reload Command
- Start the nodemon watcher:
  ```bash
  npx nodemon --watch src/main/java --ext java --exec ./watch.sh
  ```
  This command will:
  - Watch for changes in `.java` files
  - Recompile the code
  - Copy updated class files to `$CATALINA_HOME/webapps/Xployt/WEB-INF/classes`

### Modify server.xml for Reloading
1. Open `$CATALINA_HOME/conf/server.xml`
2. Modify the `<Context>` tag:
   ```xml
   <Context reloadable="true">
     <WatchedResource>WEB-INF/web.xml</WatchedResource>
     <WatchedResource>WEB-INF/tomcat-web.xml</WatchedResource>
     <WatchedResource>${catalina.base}/conf/web.xml</WatchedResource>
     <WatchedResource>WEB-INF/classes</WatchedResource>
   </Context>
   ```

### Restart Tomcat
1. Stop the server:
   ```bash
   $CATALINA_HOME/bin/shutdown.sh
   ```
2. Start it again:
   ```bash
   $CATALINA_HOME/bin/startup.sh
   ```

### Test Auto-Reload
- Make a small change in any `.java` file, save it, and verify that the changes reflect automatically in the application

## Handling Non-Java File Changes
For changes in `web.xml`, `pom.xml`, or other files outside `/src/main/java`:
```bash
mvn clean install
npx nodemon --watch src/main/java --ext java --exec ./watch.sh
```
This ensures the updated WAR is rebuilt and redeployed

## Summary of Commands

### Start Tomcat
```bash
$CATALINA_HOME/bin/startup.sh
```

### Stop Tomcat
```bash
$CATALINA_HOME/bin/shutdown.sh
```

### Build Project
```bash
mvn clean install
```

### Auto-Reload
```bash
npx nodemon --watch src/main/java --ext java --exec ./watch.sh
```

### Check Application
```
http://localhost:8080/Xployt
```

This setup ensures a smooth development workflow with automatic reloading for Java file changes while maintaining flexibility for configuration updates.
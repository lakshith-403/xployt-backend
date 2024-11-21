#!/bin/bash

# Log the current time
echo "Script triggered at $(date)"

# Find and log recently modified files
echo "Recently modified files:"
find src/main/java -type f -name "*.java" -mmin -1

# Compile the project
echo "Compiling project..."
mvn compile

# Check if the compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Set paths for the source and destination
WAR_PATH="./target/Xployt.war"
DEST_PATH="$CATALINA_HOME/webapps"
CLASSES_SRC="./target/classes"
CLASSES_DEST="$CATALINA_HOME/webapps/Xployt/WEB-INF/classes"

# Deploy WAR if necessary
if [ -f "$WAR_PATH" ]; then
    echo "Deploying WAR file..."
    cp "$WAR_PATH" "$DEST_PATH/"
    echo "WAR file copied to $DEST_PATH."
else
    echo "WAR file not found. Exiting."
    exit 1
fi

# Deploy classes if Tomcat directory exists
if [ -d "$CLASSES_DEST" ]; then
    echo "Copying class files to Tomcat..."
    cp -r "$CLASSES_SRC"/* "$CLASSES_DEST"
    if [ $? -ne 0 ]; then
        echo "Class file deployment failed. Exiting."
        exit 1
    fi
    echo "Class files successfully copied."
else
    echo "Destination directory $CLASSES_DEST not found. Please verify the path."
fi

echo "Deployment complete. Restart Tomcat if necessary."

#!/bin/bash

# Compile the project
echo "Compiling project..."
mvn compile

# Check if the compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Set paths for the source and destination
WAR_PATH="D:/My Projects/xployt-backend/target/classes"
DEST_PATH="E:/Program Files/Apache Software Foundation/Tomcat 10.1/webapps/Xployt/WEB-INF/classes"
SOURCE_FILE="D:/My Projects/xployt-backend/target/Xployt.war"

# Check if the source directory exists
if [ -d "$DEST_PATH" ]; then
    echo "Copying files from classes directory to Tomcat webapps..."
    
    # Use cp to copy all files and directories recursively
    cp -r "$WAR_PATH"/* "$DEST_PATH"
    
    # Check if copy operation was successful
    if [ $? -ne 0 ]; then
        echo "Copy failed. Exiting."
        exit 1
    fi
else
    echo "Classes directory not found at $WAR_PATH. Exiting."
fi

# Check if the destination path exists
if [ ! -d "$DEST_PATH" ]; then
    echo "Destination path does not exist. Copying WAR file..."
    cp "$SOURCE_FILE" "E:/Program Files/Apache Software Foundation/Tomcat 10.1/webapps/"
    echo "WAR file copied to Tomcat webapps directory."
else
    echo "Destination path exists. No need to copy the WAR file."
fi

echo "Done."

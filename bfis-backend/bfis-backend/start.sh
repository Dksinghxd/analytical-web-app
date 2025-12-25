#!/bin/bash

echo "=========================================="
echo "BFIS Backend Startup Script"
echo "=========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.8 or higher."
    exit 1
fi

echo "✅ Maven detected"
echo ""

# Build the project
echo "📦 Building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "✅ Build successful"
echo ""

# Run the application
echo "🚀 Starting BFIS Backend..."
echo "   Backend will be available at: http://localhost:8080"
echo "   API Documentation: http://localhost:8080/actuator"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

mvn spring-boot:run -pl bfis-api

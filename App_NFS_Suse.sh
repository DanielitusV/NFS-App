#!/bin/bash

# Script de ejecucion para NFS-App
# Solo terminal - requiere privilegios de administrador

cd "$(dirname "$0")" || exit 1

echo "========================================="
echo "       NFS-App"
echo "========================================="
echo ""

# Compilar si es necesario
if [ ! -f "build/libs/nfs-app-1.0.0.jar" ]; then
    echo "Compilando aplicacion..."
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        ./gradlew shadowJar
    elif command -v gradle &> /dev/null; then
        gradle shadowJar
    else
        echo "Error: No se encontro gradle"
        exit 1
    fi
fi

echo "Solicitando privilegios de administrador..."
echo ""

# Ejecutar con sudo
sudo java -jar build/libs/nfs-app-1.0.0.jar

#!/bin/bash

# Script de instalación para NFS-App en OpenSUSE

echo "========================================="
echo "Instalador de NFS-App"
echo "========================================="
echo ""

# Verificar que estamos en OpenSUSE
if [ ! -f /etc/os-release ]; then
    echo "Error: No se detecto OpenSUSE"
    exit 1
fi

# Verificar Java
echo "Verificando Java..."
if ! command -v java &> /dev/null; then
    echo "Java no encontrado. Instalando Java 17..."
    sudo zypper install -y java-17-openjdk-devel
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "Java encontrado: $JAVA_VERSION"
fi

# Verificar NFS Server
echo ""
echo "Verificando NFS Server..."
if ! rpm -q nfs-kernel-server &> /dev/null; then
    echo "NFS Server no encontrado. Instalando..."
    sudo zypper install -y nfs-kernel-server
else
    echo "NFS Server ya esta instalado"
fi

# Verificar pkexec
echo ""
echo "Verificando pkexec..."
if ! command -v pkexec &> /dev/null; then
    echo "pkexec no encontrado. Instalando polkit..."
    sudo zypper install -y polkit
else
    echo "pkexec encontrado"
fi

# Compilar aplicación
echo ""
echo "Compilando aplicacion..."
if [ -f "gradlew" ]; then
    chmod +x gradlew
    ./gradlew shadowJar
    if [ $? -eq 0 ]; then
        echo "Compilacion exitosa!"
    else
        echo "Error en la compilacion"
        exit 1
    fi
else
    echo "Error: gradlew no encontrado"
    exit 1
fi

# Crear script de ejecución
echo ""
echo "Creando script de ejecucion..."
cat > App_NFS_Suse.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java -jar build/libs/nfs-app-1.0.0.jar
EOF

chmod +x App_NFS_Suse.sh

echo ""
echo "========================================="
echo "Instalacion completada!"
echo "========================================="
echo ""
echo "Para ejecutar la aplicacion:"
echo "  ./nfs-app.sh"
echo ""
echo "O directamente:"
echo "  java -jar build/libs/nfs-app-1.0.0.jar"
echo ""


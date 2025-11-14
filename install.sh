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

# Autenticar con pkexec al inicio (funciona en terminal y GUI)
if pkexec echo "NFS App: Autenticación exitosa" >/dev/null 2>&1; then
    # Ejecutar la aplicación
    java -jar build/libs/nfs-app-1.0.0.jar
else
    # Mostrar error según el entorno disponible
    if command -v zenity &>/dev/null; then
        zenity --error --text="Error: Se requieren privilegios de administrador"
    elif command -v kdialog &>/dev/null; then
        kdialog --error "Error: Se requieren privilegios de administrador"
    else
        echo "Error: Se requieren privilegios de administrador"
    fi
    exit 1
fi
EOF

chmod +x App_NFS_Suse.sh

# Crear directorio para archivos temporales
echo ""
echo "Creando directorio de configuracion..."

# Obtener el usuario real (el que ejecutó sudo)
if [ -n "$SUDO_USER" ]; then
    REAL_USER="$SUDO_USER"
    REAL_HOME=$(eval echo ~$SUDO_USER)
else
    REAL_USER="$USER"
    REAL_HOME="$HOME"
fi

echo "Usuario: $REAL_USER"
echo "Directorio home: $REAL_HOME"

CONFIG_DIR="$REAL_HOME/.config/nfs-app"
sudo -u "$REAL_USER" mkdir -p "$CONFIG_DIR"

# Crear archivo temporal con permisos completos
TEMP_EXPORTS="$CONFIG_DIR/exports.tmp"
sudo -u "$REAL_USER" touch "$TEMP_EXPORTS"
chmod 777 "$TEMP_EXPORTS"
chown "$REAL_USER:users" "$TEMP_EXPORTS"
echo "Archivo temporal creado en: $TEMP_EXPORTS (permisos 777, dueño: $REAL_USER)"

echo ""
echo "========================================="
echo "Instalacion completada!"
echo "========================================="
echo ""
echo "Para ejecutar la aplicacion:"
echo "  ./App_NFS_Suse.sh"
echo ""
echo "O directamente:"
echo "  java -jar build/libs/nfs-app-1.0.0.jar"
echo ""


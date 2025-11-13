# NFS-App - Configurador de Servidor NFS para OpenSUSE

Aplicación de escritorio para configurar el servidor NFS en OpenSUSE 15.6, similar a Yast2-NFS-Server.

## Características

- ✅ Interfaz gráfica similar a Yast2-NFS
- ✅ Gestión completa de directorios exportados
- ✅ Configuración de reglas de host con todas las opciones NFS
- ✅ Carga automática del archivo `/etc/exports` existente
- ✅ Aplicación automática de cambios (reinicia servicio NFS)
- ✅ Validaciones de entrada
- ✅ Soporte para todas las opciones NFS estándar

## Requisitos

- OpenSUSE 15.6 (o superior)
- Java 17 o superior
- Servidor NFS instalado (`nfs-kernel-server`)
- Permisos de administrador (para aplicar cambios)

## Instalación

### 1. Instalar dependencias

```bash
# Instalar Java 17
sudo zypper install java-17-openjdk

# Instalar servidor NFS (si no está instalado)
sudo zypper install nfs-kernel-server
```

### 2. Compilar la aplicación

```bash
# Dar permisos de ejecución al script Gradle
chmod +x gradlew

# Compilar la aplicación
./gradlew shadowJar
```

El JAR se generará en: `build/libs/nfs-app-1.0.0.jar`

### 3. Ejecutar la aplicación

```bash
# Ejecutar directamente
java -jar build/libs/nfs-app-1.0.0.jar

# O usar el script proporcionado
chmod +x install.sh
./install.sh
```

## Uso

### Iniciar la aplicación

```bash
java -jar nfs-app-1.0.0.jar
```

**Nota:** La aplicación requiere permisos de administrador para aplicar cambios. Se solicitará la contraseña mediante `pkexec` cuando se guarden los cambios.

### Agregar un directorio para exportar

1. Click en "Add Directory"
2. Ingrese la ruta del directorio (ej: `/opt/docus`)
3. Se creará automáticamente una regla por defecto con `192.168.1.0/24(rw)`

### Configurar reglas de host

1. Seleccione un directorio de la lista
2. Click en "Add Host" para agregar una nueva regla
3. Configure:
   - **Host Wild Card**: IP, rango CIDR o `*` para todos
   - **Opciones NFS**: Seleccione las opciones deseadas
   - **anonuid/anongid**: Si se seleccionan, ingrese valores numéricos

### Guardar y aplicar cambios

1. Click en "Save & Apply"
2. Ingrese su contraseña cuando se solicite
3. La aplicación:
   - Guarda en `/etc/exports`
   - Reinicia el servicio `nfs-server`
   - Ejecuta `exportfs -rav`

## Opciones NFS Disponibles

- **rw/ro**: Lectura-escritura / Solo lectura
- **sync/async**: Escrituras síncronas / asíncronas
- **root_squash/no_root_squash**: Mapeo de usuario root
- **all_squash**: Mapea todos los usuarios a nobody
- **subtree_check/no_subtree_check**: Verificación de subárbol
- **secure/insecure**: Restricción de puertos
- **anonuid**: UID para usuarios anónimos (requiere valor numérico)
- **anongid**: GID para usuarios anónimos (requiere valor numérico)

Haga click en "Informacion de Opciones" para ver descripciones detalladas.

## Estructura del Proyecto

```
src/main/java/aso/nfsapp/
├── app/
│   └── Main.java                 # Punto de entrada
├── controller/
│   └── NfsController.java        # Lógica de negocio
├── model/
│   ├── ExportEntry.java          # Modelo de directorio exportado
│   └── HostRule.java             # Modelo de regla de host
├── service/
│   ├── ExportFileManager.java    # Gestión del archivo exports
│   └── SystemPaths.java          # Rutas del sistema
└── view/
    ├── MainWindow.java            # Ventana principal
    ├── DirectoryListPanel.java    # Panel de directorios
    ├── HostRulesTablePanel.java   # Panel de reglas
    └── HostRuleDialog.java        # Diálogo de configuración
```

## Ejemplo de Uso

### Configuración básica

1. Agregar directorio `/opt/docus`
2. Agregar regla: `*` con opciones `rw,root_squash`
3. Guardar y aplicar

Resultado en `/etc/exports`:
```
/opt/docus *(rw,root_squash)
```

### Configuración avanzada

1. Agregar directorio `/opt/shared`
2. Agregar regla: `192.168.1.0/24` con opciones `rw,sync,all_squash,anonuid=1000,anongid=1000`
3. Guardar y aplicar

Resultado en `/etc/exports`:
```
/opt/shared 192.168.1.0/24(rw,sync,all_squash,anonuid=1000,anongid=1000)
```

## Verificación

Después de aplicar cambios, verifique que el servidor NFS funciona:

```bash
# Verificar estado del servicio
sudo systemctl status nfs-server

# Ver exportaciones activas
sudo exportfs -v

# Probar montaje desde otra máquina
mount -t nfs <ip-servidor>:/opt/docus /mnt/nfs
```

## Solución de Problemas

### La aplicación no puede escribir en /etc/exports

- Asegúrese de tener `pkexec` instalado: `sudo zypper install polkit`
- Verifique que el usuario tenga permisos de administrador

### El servicio NFS no se reinicia

```bash
# Verificar logs
sudo journalctl -u nfs-server -n 50

# Reiniciar manualmente
sudo systemctl restart nfs-server
```

### Error al parsear /etc/exports existente

- La aplicación ignora líneas con formato incorrecto
- Revise manualmente el archivo si hay problemas

## Desarrollo

### Compilar desde código fuente

```bash
./gradlew build
```

### Ejecutar tests

```bash
./gradlew test
```

### Generar JAR ejecutable

```bash
./gradlew shadowJar
```

## Licencia

Este proyecto es parte de una tarea académica.

## Autor

Desarrollado para OpenSUSE 15.6 - Configuración de Servidor NFS

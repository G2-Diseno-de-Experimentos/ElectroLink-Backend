# Jenkins local con acceso a Docker

El `Jenkinsfile` tiene una etapa **Build Docker Image** que construye la imagen del backend con el mismo `Dockerfile` que usa Render, y una etapa **Approve Delivery** que espera la aprobación manual de esa versión. Para la primera, el contenedor de Jenkins necesita el cliente Docker y acceso al Docker del equipo.

La imagen `ci/jenkins/Dockerfile` parte de `jenkins/jenkins:lts-jdk21` y le agrega el cliente Docker y el plugin `buildx`. El daemon es el del equipo, compartido mediante el socket.

## Recrear el contenedor de Jenkins

La configuración, los jobs y el historial están en el volumen `jenkins_home`, así que se conservan al recrear el contenedor. En Git Bash, anteponer `MSYS_NO_PATHCONV=1` para que no convierta las rutas `/var/...`.

```bash
docker build -t electrolink-jenkins:lts-jdk21 ci/jenkins

docker stop jenkins
docker rename jenkins jenkins-old          # respaldo del contenedor anterior

docker run -d --name jenkins \
  -p 9089:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --group-add 0 \
  electrolink-jenkins:lts-jdk21
```

`--group-add 0` es necesario porque en Docker Desktop el socket pertenece a `root:root` con permisos 660, y Jenkins se ejecuta como el usuario `jenkins`.

Verificar que Jenkins puede usar Docker:

```bash
docker exec jenkins docker version --format "cli={{.Client.Version}} server={{.Server.Version}}"
```

Jenkins queda en `http://localhost:9089`.

## Volver al contenedor anterior

```bash
docker rm -f jenkins
docker rename jenkins-old jenkins
docker start jenkins
```

Cuando el contenedor nuevo esté funcionando, `jenkins-old` se puede eliminar con `docker rm jenkins-old`.

## Resultado del pipeline

| Etapa | Resultado |
|---|---|
| Build Docker Image | Imágenes `electrolink-backend:<número de build>` y `electrolink-backend:latest`, con la etiqueta `org.opencontainers.image.revision` igual al commit construido. |
| Approve Delivery | El build se detiene y espera hasta 30 minutos. En la vista de etapas, **Approve** la etiqueta como `electrolink-backend:release-candidate`; **Abort**, o vencer el plazo, termina el build como abortado. |

Mientras espera la aprobación, el build ocupa un ejecutor de Jenkins.

## Seguridad

Montar el socket le da a Jenkins control total del Docker del equipo. Usar esta configuración solo en el Jenkins local del equipo de desarrollo, no en un servidor compartido.

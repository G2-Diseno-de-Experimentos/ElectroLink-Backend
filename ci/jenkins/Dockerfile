# Jenkins LTS (JDK 21) with the Docker CLI, so the pipeline can build the
# backend image. The Docker daemon is the host one, shared through the socket:
# see docs/JENKINS.md.
FROM docker:27-cli AS docker-cli

FROM jenkins/jenkins:lts-jdk21

COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker-cli /usr/local/libexec/docker/cli-plugins/docker-buildx /usr/local/libexec/docker/cli-plugins/docker-buildx

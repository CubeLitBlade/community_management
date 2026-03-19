# Community Management System

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-4.x-brightgreen)
![License](https://img.shields.io/github/license/CubeLitBlade/community_management)
[![CI](https://github.com/CubeLitBlade/community_management/actions/workflows/gradle.yml/badge.svg)](https://github.com/CubeLitBlade/community_management/actions/workflows/gradle.yml)

## Quick Start

This project can be run with Docker or Podman. This guide assumes Podman. If you use Docker, replace `podman` with `docker` in the commands below.

### Prerequisites

The following runtime environment is recommended:

| Environment       | Notes                                   |
| ----------------- | --------------------------------------- |
| Docker / Podman   | Container runtime                       |
| Docker Compose    | Compose engine                          |
| JDK 25 (optional) | Required only if developing the backend |

Before you start, complete the following steps.

1. Copy the `.env.template` in the root directory and rename it to `.env`;

    ```bash
    cp .env.template .env
    ```

2. Update `.env` to match your local environment.

### Development

During development, you usually only need to start the dependent services, such as the database.

The backend uses Spring Boot’s Docker Compose support. When you start the backend from your IDE, it will start the required containers and load database settings from `.env`.

> [!IMPORTANT]
>
> Spring Boot Docker Compose is designed for Docker.
>
> To use it with Podman, you may need to install `docker-compose`, map the socket file, and install `podman-docker` to emulate the Docker CLI.
>
> For more information, see [Troubleshooting](#spring-boot-docker-compose-does-not-work-with-podman).

If Podman does not work for you, or if you prefer to start the services manually, follow these steps:

1. Open `/backend/src/main/resources/application.yaml`;

2. Set `spring.docker.compose.enabled` to `false`;

3. Configure the `spring.datasource` properties to point to your database;

4. Run the following command from the project root to start the database.

```bash
podman compose up
```

### Local Testing

To build the application JAR and run it in a container:

1. Change to the `/backend` to build the JAR;

    ```bash
    ./gradlew bootJar
    ```

2. Confirm that the generated JAR exists in `/backend/build/libs` ;

3. Run the following command from the project root.

```bash
podman compose -f compose.yaml -f compose.override.yaml up
```

This command builds an image from `/backend/Dockerfile`, copies the JAR into the container, and starts the application.

> [!WARNING]
>
> The default Dockerfile uses a Docker Hardened Image (DHI) as the Java runtime, so authentication is required.
>
> - If you have credentials, log in to `dhi.org` before running the command;
> - If that fails, modify the `FROM` instruction in `/backend/Dockerfile` to use a public JRE image.

### Production Simulation

Run the following command.

```bash
podman compose -f compose.yaml -f compose.prod.yaml up
```

In this case, Compose will download the published image from GitHub and deploy it automatically.

## Troubleshooting

### Spring Boot Docker Compose does not work with Podman
### Spring Boot Docker Compose does not work with Podman

Spring Boot Docker Compose support expects a Docker-compatible CLI.
When using Podman, the application may fail to start with an error similar to:

```text
Cannot run program "docker"
```

This happens because Spring Boot invokes the `docker` command directly.

A possible solution is to install a Docker-compatible wrapper and
enable the Podman socket.

Example (Fedora):

```bash
# Install a Docker-compatible CLI and a Compose engine
# Spring Boot Docker Compose invokes the `docker` command directly
sudo dnf install podman-docker docker-compose

# Enable the Podman API socket so Docker-compatible clients can connect
systemctl --user enable --now podman.socket

# Tell Docker-compatible tools to use the Podman socket
export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/$UID/podman/podman.sock
```

### JMX port conflict when running with WSL2 and IntelliJ IDEA

If the backend is run from IntelliJ IDEA on Windows while the project is stored in WSL2, the following errors may appear:

- `JMX connector server communication error`
- `Port already in use` (random high port)

This is usually caused by IntelliJ IDEA’s remote JMX agent port rather than your application server port.

Recommended workaround for local development:

1. Open the Spring Boot run configuration;
2. Disable `Enable JMX agent`;
3. Add VM option: `-Dspring.jmx.enabled=false`;
4. Run the backend.

When troubleshooting, check both WSL2 and Windows, since the port may be in use on either side:

In WSL2:

```bash
ss -tunlp | grep <PORT>
ps -ef | grep java
```

In Windows PowerShell:

```powershell
Get-NetTCPConnection -LocalPort <PORT> | Select-Object ProcessId, State
tasklist /FI "PID eq <PID>"
```

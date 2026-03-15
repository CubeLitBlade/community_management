# Community Management System

## How to Run

This project can be launched using Docker or Podman. This guide assumes you are using Podman. If you are using Docker, simply replace `podman` with `docker` in the commands below.

### Preparations

We recommend setting up the following runtime environment.

| Environment       | Note                                          |
| ----------------- | --------------------------------------------- |
| Docker / Podman   | Provides the most basic container environment |
| Docker Compose    | Compose engine                                |
| JDK 25 (optional) | If you fork this repository for development   |

Before starting, you need to complete the following preparations.

1. Copy the `.env.template` in the root directory and rename it to `.env`;

    ```bash
    cp .env.template .env
    ```

2. Modify the configuration in `.env` to match your own settings.

### For Development

During development, you typically only need to run dependent services (such as the database).

This project’s backend utilizes Spring Boot Docker Compose support. When you start the backend application from your IDE, it automatically launches the required containers and retrieves the database configuration from the `.env` file.

> NOTE
>
> Spring Boot Docker Compose is designed for Docker. To use it with Podman, you may need to install `docker-compose`, map the socket file, and install `podman-docker` to emulate the Docker CLI.

If you encounter any issues with Podman, or if you prefer to start it manually, you can follow these steps:

1. Open `/backend/src/main/resources/application.yaml`;

2. Set `spring.docker.compose.enabled` to `false`;

3. Configure the `spring.datasource` properties to point to your database;

4. Run the following command in the root directory to start the database.

```bash
podman compose up
```

### For Local Testing

To build the application JAR and run it in a containerized environment:

1. Navigate to the `/backend` to build the JAR;

    ```bash
    ./gradlew bootJar
    ```

2. Confirm the generated JAR file exists in `/backend/build/libs`;

3. Run the following command in the root directory.

```bash
podman compose -f compose.yaml -f compose.override.yaml up
```

This command builds an image based on `/backend/Dockerfile`, copies the JAR into the container, and runs it.

> NOTE
>
> The default Dockerfile uses a Docker Hardened Image (DHI) as the Java runtime, which requires authentication.
>
> - If you have credentials: Log in to dhi.org before running the command.
> - If you encounter issues: Try modifying the FROM instruction in /backend/Dockerfile to use a public JRE image.

### For Production Simulation

Run the following command directly.

```bash
podman compose -f compose.yaml -f compose.prod.yaml up
```

In this case, compose would download published image from GitHub and deploy it automatically.

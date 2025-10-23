# Use OpenJDK 21 base image
FROM openjdk:21-slim

# Install curl
RUN apt-get update && apt-get install -y curl

# Set the working directory inside the container (Path is dynamic will be created inside docker file system)
WORKDIR /app/tasktracker

# Copy your WAR file into the container (adjust paths as necessary)
COPY tasktracker-web/build/libs/tasktracker-web.war /app/tasktracker/tasktracker-web.war
COPY resources/application.properties /app/tasktracker/resources/application.properties

# Expose the necessary port for the Spring Boot application (default is 8080, but you're debugging on port 7000)
EXPOSE 8090
EXPOSE 7002
EXPOSE 5432

# Set JAVA_HOME (not necessary if you're using OpenJDK base image, but added for completeness)
# ENV JAVA_HOME="/home/asite/Desktop/Bhavik/Sboot_project/jdk-21.0.1"
# ENV PATH="${JAVA_HOME}/bin:${PATH}"
# ENV JAVA_HOME="/usr/local/openjdk-21"
# ENV PATH="$JAVA_HOME/bin:$PATH"


# Command to run the WAR file with custom java options (debugging and Spring properties)
CMD ["java", "-Xdebug", "-Xrunjdwp:server=y,transport=dt_socket,address=7002,suspend=n", \
    "-Djava.locale.providers=COMPAT,CLDR", \
    "-Djboss.server.home.dir=/app/tasktracker", \
    "-jar", "tasktracker-web.war", \
    "--spring.config.location=resources/application.properties"]

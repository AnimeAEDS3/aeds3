FROM openjdk:11

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

RUN javac Main.java
EXPOSE 8080

# Run the application
CMD ["java", "Main"]

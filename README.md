# Object_Oriented_System_Desig

# Environmental CO₂ Logging System

Object-Oriented System Design – SEN5000  
Team: Aadit Karnavat, Luca Martinet , Noé Kurata

## Overview

This project implements a simple client–server application for logging environmental CO₂ readings. Field researchers submit readings via a console client, and a multi-threaded server validates and stores them in a CSV file for later analysis.

## Features

- TCP client–server architecture using Java sockets.
- Support for up to 4 concurrent clients via a fixed thread pool.
- Server-side validation of user ID, postcode, and CO₂ ppm.
- Thread-safe CSV persistence with timestamp, userId, postcode, and CO₂ value.
- Modular client design (configuration, connection, prompt processing, console I/O).

## Architecture

**Server package (`Server`)**

- `BasicServer`: entry point; configures port, creates repository and `Co2LoggingServer`, then starts the server.
- `Co2LoggingServer`: listens on a TCP port and dispatches each client to a `ClientHandler` in a thread pool.
- `ClientHandler`: interacts with one client, prompts for data, validates inputs, and stores readings.
- `Co2Reading`: immutable value object representing a single CO₂ reading.
- `Co2ReadingRepository`: interface for persisting readings.
- `Co2ReadingCsvRepository`: CSV-based implementation of the repository; synchronized file writes.

**Client package (`Client`)**

- `BasicClient`: entry point; parses CLI args, connects to server, and runs the prompt loop.
- `ClientConfig`: validates host/port configuration.
- `ServerConnection`: manages the socket connection and I/O with the server.
- `IO` / `ConsoleIO`: abstraction and console implementation for user I/O.
- `PromptProcessor`: relays prompts between server and user and sends responses back.

## How to Run

### Prerequisites

- JDK 
- Git-cloned copy of this repository (private, as required in the brief).

### Start the server

From the project root:

Default port 8080
java Server.BasicServer

The server creates/uses `co2_readings.csv` in the working directory for storage.

### Start a client

In another terminal:
Follow the prompts to enter User ID, postcode, and CO₂ concentration (ppm). On success, the server confirms and appends a new row to the CSV file.

## Documentation and UML

The `docs/` folder contains:

- Use case, class, and sequence diagrams (PlantUML `.puml` plus exported images).  
- Material supporting the SDLC-based design and testing documentation submitted as a separate PDF for assessment.

## Testing

Testing focuses on:

- Valid and invalid input handling (empty fields, invalid/negative CO₂ values) in `ClientHandler`.
- Behaviour with multiple concurrent clients (up to four simultaneously) in `Co2LoggingServer`.  
- Correct CSV format and thread-safe writes in `Co2ReadingCsvRepository`.





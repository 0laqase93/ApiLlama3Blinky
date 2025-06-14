# API Llama3 Blinky

REST API developed to provide backend services for the BLinky mobile application. This API integrates a Llama3 artificial intelligence model to offer conversational interactions and intelligent event management.

## Project Description

BLinky API is a backend service that allows the BLinky mobile application to connect with a local Llama3 AI model. The API provides functionalities for:

- User authentication and management
- AI conversations using different personalities
- AI-powered event creation and management
- Storage and retrieval of conversation history

## Technologies Used

- **Spring Boot**: Framework for application development
- **PostgreSQL**: Relational database for persistent storage
- **Llama3**: Local AI model for natural language processing
- **JWT**: Token-based authentication for security
- **JPA/Hibernate**: Object-relational mapping for data access
- **Maven**: Dependency management and project building

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Llama3 model running locally (through Ollama or another implementation)
- Maven for dependency management

## Configuration

### Database

The application uses PostgreSQL as its database. Configure the connection parameters in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blinkydb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### AI Model

The application connects to a local Llama3 model. Configure the parameters in `application.properties`:

```properties
ia.url=http://localhost:11434
ia.model=llama3.2
```

### JWT

Configure the secret key and expiration time for JWT tokens:

```properties
jwt.secret=YourSecretKey
jwt.expiration=2592000000
```

## Project Structure

- **controller**: REST controllers to handle HTTP requests
- **service**: Business logic of the application
- **repository**: Interfaces for data access
- **model**: JPA entities representing database tables
- **security**: Security and authentication configuration
- **exception**: Centralized exception handling
- **dto**: Data transfer objects

## Main Endpoints

### Authentication

- `POST /api/auth/register`: Register new users
- `POST /api/auth/login`: Login and obtain JWT token
- `POST /api/auth/reset-password`: Password reset

### AI Interaction

- `POST /api/llama/send_prompt`: Send messages to the AI
- `POST /api/llama/clear_conversation`: Clear conversation history
- `POST /api/llama/create_event`: Create events using AI
- `POST /api/llama/create_future_event`: Create future events using AI

### Event Management

- `GET /api/events`: Get user events
- `POST /api/events`: Manual event creation
- `PUT /api/events/{id}`: Update events
- `DELETE /api/events/{id}`: Delete events

### Personality Management

- `GET /api/personalities`: Get available personalities
- `POST /api/personalities`: Create new personalities (admin only)
- `PUT /api/personalities/{id}`: Update personalities (admin only)
- `DELETE /api/personalities/{id}`: Delete personalities (admin only)

## Data Model

### User
- **id**: Unique identifier
- **email**: Email address (unique)
- **password**: Password (encrypted)
- **isAdmin**: Administrator role indicator
- **username**: Username

### Conversation
- **id**: Unique identifier
- **title**: Conversation title
- **createdAt**: Creation date
- **user**: Owner user (one-to-one relationship)
- **userMessages**: User messages (one-to-many relationship)
- **aiResponses**: AI responses (one-to-many relationship)

### Event
- **id**: Unique identifier
- **title**: Event title
- **startTime**: Start date and time
- **endTime**: End date and time
- **user**: Owner user (many-to-one relationship)
- **location**: Event location
- **description**: Event description

### Personality
- **id**: Unique identifier
- **name**: Personality name
- **basePrompt**: Base prompt that defines behavior
- **description**: Personality description

## Security

The API uses JWT token-based authentication. Each request to protected endpoints must include a valid token in the authorization header:

```
Authorization: Bearer {token}
```

## Development

To run the application in development mode:

```bash
mvn spring-boot:run
```

## Deployment

To build the project:

```bash
mvn clean package
```

To run the generated JAR:

```bash
java -jar target/ApiLlama3Blinky-0.0.1-SNAPSHOT.jar
```

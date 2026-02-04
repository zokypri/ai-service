

## Description
This project is a Kotlin based microservice that integrates with a Large Language Model (LLM). 
Its primary purpose is to provide AI-powered features within a microservice architecture.

## Api documentation
When running service locally swagger can be accessed with this link:

http://localhost:8089/ai-service/swagger-ui.html

## Features
- Integrates with an LLM (Claude) to extract useful information from Stacktrace error logs via a dedicated controller.
- Exposes a Swagger UI for easy API exploration and testing when running the service locally.

## Error log analysis
The service includes a controller that accepts Stacktrace error logs and utilizes the LLM to analyze and extract relevant information. 
This feature can be particularly useful for debugging and understanding complex error scenarios.
The service is using Claude Sonnet 4.5 model for error log analysis.
In order to run this service locally you need to set the following environment variables:
- `CLAUDE_API_KEY` : Your Claude API key.

## TODO

Fix retry for this error
{
"error": "Anthropic API is overloaded, consider retrying later"
}

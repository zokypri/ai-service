

## Description
This project is a Kotlin based microservice that integrates with a Large Language Model (LLM). 
Its primary purpose is to provide AI-powered features within a microservice architecture.

Integrates with an LLM (Claude) to extract useful information from Stacktrace error logs via a dedicated controller.

## Starting service locally
Exposes a Swagger UI for easy API exploration and testing when running the service locally through this link:
http://localhost:8089/ai-service/swagger-ui.html

In order to run this service locally you need to set the following environment variables:
- `ANTHROPIC_API_KEY` : Your Anthropic API key.

Start server with:
ANTHROPIC_API_KEY="your_api_key" mvn spring-boot:run

## Error log analysis
The service includes an endpoint that accepts Stacktrace error logs and utilizes the LLM to analyze and extract relevant information. 
This feature can be particularly useful for debugging and understanding complex error scenarios.
The service is using Claude Sonnet 4.5 model for error log analysis.

## Test case of importance
The test case `should fetch error log analysis from Claude`() from the ErrorLogAnalysisTest class is of particular importance. 
This test verifies the functionality of the error log analysis feature by sending a sample Stacktrace log to the controller and asserting that a valid response is received from the LLM.
It shows how the stack trace log is sent to the service and how the error log analysis is built from LLM response
Pay close attention to the files in src/test/resources/__files folder

## Technologies Used

# Concurrent Trie Dictionary

Author: Rodina Oleksandr IM-33

## Short description

Concurrent Trie Dictionary is a high-performance, concurrent prefix-tree (trie) library packaged as a Spring Boot application exposing a REST API. It supports single and bulk insert/delete operations, prefix search (including paged responses), autocomplete, and utility endpoints to inspect and manage keys.

This project includes an in-memory concurrent trie implementation and a thin REST layer to interact with it.

## Requirements

- Java 21 (configured in `pom.xml`)
- Maven 3.6+ to build and run tests
- Docker (optional, for container image)

## Build

From the project root run:

```bash
mvn -DskipTests package
```

This produces an executable JAR under `target/` (Spring Boot jar).

## Run

Run the packaged application:

```bash
java -jar target/concurrent-trie-dictionary-1.0.0-SNAPSHOT.jar
```

Or run directly with Maven during development:

```bash
mvn spring-boot:run
```

## Docker

Build an image (project includes a `Dockerfile`):

```bash
docker build -t concurrent-trie-dictionary .
```

Run container:

```bash
docker run -p 8080:8080 --rm concurrent-trie-dictionary
```

## Tests

Run unit and integration tests with:

```bash
mvn verify
```

## Benchmarks (JMH)

### How to run benchmarks:

1.  **Install the main library** (so the benchmark module can see it):

    ```bash
    mvn clean install -DskipTests
    ```

2.  **Build the benchmark module**:

    ```bash
    cd benchmarks
    mvn clean package
    ```

3.  **Run the benchmarks**:

    ```bash
    java -jar target/benchmarks.jar
    ```

## API: endpoints and examples

Base path: `/api/v1/dictionary`

All examples assume the application is running on `http://localhost:8080`.

1.  Insert single entry

- POST /api/v1/dictionary

Request body (JSON):

```json
{ "key": "apple", "value": "fruit" }
```

curl example:

```bash
curl -i -X POST http://localhost:8080/api/v1/dictionary \
	-H "Content-Type: application/json" \
	-d '{"key":"apple","value":"fruit"}'
```

Response: 201 Created on success.

2.  Bulk insert

- POST /api/v1/dictionary/bulk

Request body (JSON):

```json
{
  "entries": [
    { "key": "apple", "value": "fruit" },
    { "key": "application", "value": "software" }
  ],
  "atomic": true
}
```

curl example:

```bash
curl -i -X POST http://localhost:8080/api/v1/dictionary/bulk \
	-H "Content-Type: application/json" \
	-d '{"entries":[{"key":"apple","value":"fruit"},{"key":"application","value":"software"}],"atomic":true}'
```

Response: 201 Created for full success, 207 Multi-Status for partial success, or 400 Bad Request for failure.

3.  Search by key

- GET /api/v1/dictionary/{key}

Example:

```bash
curl -s http://localhost:8080/api/v1/dictionary/apple
```

Returns the stored value or structured result (JSON).

4.  Delete by key

- DELETE /api/v1/dictionary/{key}

Example:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/dictionary/apple
```

Response: 204 No Content on success.

5.  Bulk delete

- DELETE /api/v1/dictionary/bulk

Request body (JSON):

```json
{
  "keys": ["apple", "application"],
  "atomic": true,
  "ignoreMissing": false
}
```

curl example:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/dictionary/bulk \
	-H "Content-Type: application/json" \
	-d '{"keys":["apple","application"],"atomic":true,"ignoreMissing":false}'
```

Response: 200 OK, 207 Multi-Status, or 400 Bad Request depending on result.

6.  Exists

- GET /api/v1/dictionary/exists/{key}

Example:

```bash
curl -s http://localhost:8080/api/v1/dictionary/exists/apple
```

Returns JSON boolean: `true` or `false`.

7.  Autocomplete

- GET /api/v1/dictionary/autocomplete?prefix=ap\&limit=10

Example:

```bash
curl -s "http://localhost:8080/api/v1/dictionary/autocomplete?prefix=ap&limit=5"
```

Returns a JSON array of string suggestions. `limit` defaults to 10 and is clamped between 1 and 100.

8.  Prefix search (all results)

- GET /api/v1/dictionary/prefix?prefix=app

Example:

```bash
curl -s "http://localhost:8080/api/v1/dictionary/prefix?prefix=app"
```

Returns a JSON array of objects `{key, value}`.

9.  Clear

- DELETE /api/v1/dictionary/clear

Example:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/dictionary/clear
```

Clears the whole trie. Response: 204 No Content.

10. Starts-with check

- GET /api/v1/dictionary/starts-with/{prefix}

Example:

```bash
curl -s http://localhost:8080/api/v1/dictionary/starts-with/app
```

Returns JSON boolean indicating whether any key starts with the given prefix.

11. Get all keys

- GET /api/v1/dictionary/keys

Example:

```bash
curl -s http://localhost:8080/api/v1/dictionary/keys
```

Returns a JSON array of all keys stored in the dictionary.

12. Create Checkpoint

- POST /api/v1/dictionary/checkpoints

Creates a snapshot of the current state of the trie in memory.

Example:

```bash
curl -i -X POST http://localhost:8080/api/v1/dictionary/checkpoints
```

Response: 201 Created. The body contains the generated **Checkpoint ID** (e.g., `1`).

13. List Checkpoints

- GET /api/v1/dictionary/checkpoints

Returns a map of all available checkpoints, where the key is the Checkpoint ID and the value is the size of the dictionary at that snapshot.

Example:

```bash
curl -s http://localhost:8080/api/v1/dictionary/checkpoints
```

Response example: `{"1":5, "2":10}`

14. Rollback to Checkpoint

- POST /api/v1/dictionary/checkpoints/{id}/rollback

Reverts the dictionary to the state stored in the specified checkpoint. **Note:** Any changes made after this checkpoint will be lost.

Example (rolling back to checkpoint ID 1):

```bash
curl -i -X POST http://localhost:8080/api/v1/dictionary/checkpoints/1/rollback
```

Response: 200 OK on success, or 404 Not Found if the checkpoint ID does not exist.

15. Delete Checkpoint

- DELETE /api/v1/dictionary/checkpoints/{id}

Deletes a specific checkpoint from memory to free up resources.

Example (deleting checkpoint ID 1):

```bash
curl -i -X DELETE http://localhost:8080/api/v1/dictionary/checkpoints/1
```

Response: 204 No Content on success, or 404 Not Found if the ID is invalid.

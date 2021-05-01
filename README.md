# Lightweight Contract Broker

Build using [Ktor](https://ktor.io/), [Arrow](https://arrow-kt.io/)
and [exposed](https://github.com/JetBrains/Exposed).

## Workflows

### Create and fulfill contract

```text
      +-------------+  +-------------+  +--------------+
      |             |  |             |  |              |
      | Consumer    |  | Broker      |  | Provider     |
      |             |  |             |  |              |
      +-----+-------+  +------+------+  +-------+------+
            |                 |                 |
+-----------+                 |                 |
|           |                 |                 |
|create     |                 |                 |
|test       |                 |                 |
|           |                 |                 |
+-----------> mock            |                 |
            | component       |                 |
            |                 |                 |
            | create contract |                 |
            +----------------->                 |
            |                 |                 |
            |                 |                 |
            |                 +----------------->
            |                 |                 |
            |                 | test fails      +------------+
            |                 <-----------------+            |
            |                 | test fails      | implement  |
            |                 <-----------------+ component  |
            |                 |                 | v1.0.0     |
            |                 |                 <------------+
            |                 | test passes     |
            |                 <-----------------+
            |                 |                 |
            |                 |                 |
```

### Provider has to update contract

```text
      +-------------+  +-------------+  +--------------+
      |             |  |             |  |              |
      | Consumer    |  | Broker      |  | Provider     |
      |             |  |             |  |              |
      +-----+-------+  +------+------+  +-------+------+
            |                 |                 |
            |                 | test passes     |
            |                 <-----------------+
            |                 |                 |
            |                 | test fails      +------------+
            |                 <-----------------+            |
            |                 | test fails      | implement  |
            |                 <-----------------+ component  |
            |                 |                 | v1.0.1     |
            |                 |                 <------------+
            |                 | test fails      |
            |                 <-----------------+
            |     go talk to the other team     |
            <-----------------+-----------------+
            |                 |                 |
+-----------+                 |                 |
|           |                 |                 |
|create     |                 |                 |
|test       |                 |                 |
|           |                 |                 |
+-----------> mock            |                 |
            | component       |                 |
            |                 |                 |
            | update contract |                 |
            +----------------->                 |
            |                 |  test passes    |
            |                 <-----------------+
            |                 |                 |
            |                 |                 |
```

### Update to a new version, driven by provider



## Running

```
./gradlew test
./gradlew run
```

Starts with an inmem h2 that will be lost after teardown.

## Running with a postgres

```bash
export ENV=prod 
export database_url=jdbc:postgresql://localhost:12346/test 
export database_user=admin 
export database_password=secret 
./gradlew run
```

## API

### Get Contracts

```
curl http://localhost:8097/contracts
```

### Add Contract

```
curl -X POST \
  http://localhost:8097/contracts/ \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{ 
	"provider": "home",
	"consumer": "nav",
	"element": "home",
	"fileLines": ["the lines for the contract"]
}'
```

### Get Contract by Id

```
curl http://localhost:8097/contracts/1
```

### Add Testresult to contract

```
curl -X PUT \
  http://localhost:8097/contracts/1/testResults \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"date": [2021,4,20,15,57,45,434000000],
    "result": "Success",
    "version": "1"
}'
```

Result can be `Success` or `Failure`
# Simple Transfer API test

Code starts server with exposing little CRUD APIs:
```
/accounts/:id  - POST, GET, DELETE
/transfers/:id - POST, GET
```

## Getting started

Server Could be started with params:
```
--port         -  8080 by default
--context-path -  /api by default
```

Example url by default start: http://localhost:8080/api/accounts/ 

### Models

**Account**

| name | type | description |
| --------- | ---- | ------------- | 
| accountId | UUID | An account Id |
| lastModified | Instant | Timestamp of last modification |
| balance | int | Current balance |

**Transfer**

| name | type | description |
| --------- | ---- | ------------- | 
| transferId | UUID | An transfer Id |
| date | Instant | Timestamp of transfer |
| accountFrom | UUID | An account From Id |
| accountTo | UUID | An account To Id |
| amount | int | Amount of balance should be transferred |
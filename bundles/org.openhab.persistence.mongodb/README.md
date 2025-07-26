# MongoDB Persistence

This service allows you to persist state updates using the MongoDB database.
It supports writing information to a MongoDB document store, as well as querying from it.

## Configuration

This service can be configured in the file `services/mongodb.cfg`.

| Property   | Default | Required | Description                                                                  |
| ---------- | ------- | :------: | ---------------------------------------------------------------------------- |
| url        |         |   Yes    | connection URL to address MongoDB.  For example, `mongodb://localhost:27017` |
| database   |         |   Yes    | database name                                                                |
| collection |         |   Yes    | set collection to "" if it shall generate a collection per item              |

If you have a username and password it looks like this: url = mongodb://[username]:[password]@[localhost]:27017/[database]
The database is required: <https://mongodb.github.io/mongo-java-driver/3.9/javadoc/com/mongodb/MongoClientURI.html>

All item and event related configuration is done in the file `persistence/mongodb.persist`.

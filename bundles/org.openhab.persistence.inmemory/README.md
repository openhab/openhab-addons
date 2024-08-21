# InMemory Persistence

The InMemory persistence service provides a volatile storage, i.e. it is cleared on shutdown.
Because of that the `restoreOnStartup` strategy is not supported for this service.

The main use-case is to store data that is needed during runtime, e.g. temporary storage of forecast data that is retrieved from a binding.

The default strategy for this service is `forecast`.
Unlike other persistence services, you MUST add a configuration, otherwise no data will be persisted.
To avoid excessive memory usage, it is recommended to persist only a limited number of items and use a strategy that stores only data that is actually needed.

The service has a global configuration option `maxEntries` to limit the number of datapoints per item, the default value is `512`.
When the number of datapoints is reached and a new value is persisted, the oldest (by timestamp) value will be removed.
A `maxEntries` value of `0` disables automatic purging.

# MapDB Persistence

The [MapDB](https://mapdb.org/) persistence service is based on a simple key-value store that only saves the last value.
MapDB is useful for restoring items that have the `restoreOnStartup` strategy because other persistence options have some drawbacks if only the last value is needed on restarts.

Some disadvantages of other persistence services compared to MapDB are that they:

- grow in time
- require complex installs (`influxdb`, `jdbc`, `jpa`)
- `rrd4j` cannot store all item types (only numeric types)

It is only possible to query the last value and not other historic values because the MapDB persistence service can only store one value per item.

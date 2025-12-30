# Geocoding Profile Transformation Service

Transformation to convert geo coordinates into human readable string address and vice versa.

## OpenStreetMap Geocoding

Geocoding transformation service using [Nominatim API for OpenStreetMap](https://nominatim.org/release-docs/latest/) to resolve

- geo coordinates into a human readable string ([reverse geocoding](https://nominatim.org/release-docs/latest/api/Reverse/))   
- an address string into geo coordinates ([geocoding](https://nominatim.org/release-docs/latest/api/Search/)) 

### Reverse Geocoding

The reverse geocoding is applied when the channel updates the `Location` with latitude and longitude geo coordinates.
These will be resolved into a human readable address.
 
### Geocoding

Geocding is applied if you send a string command towards the item.
The API is translating this search string into geo coordinates which are send via the channel towards the handler.
Of course this makes only sense if the channel is declared as writable.
Formulate your string command as precise as possible to avoid ambiguous results.
E.g. _Springfield US_ command will deliver multiple results and only one is chosen for the transformation. 

### Configuration

| Configuration Parameter | Type | Description                                                                                      |
|-------------------------|------|--------------------------------------------------------------------------------------------------|
| `format`                | text | Country specific address formatting                                                              |
| `resolveDuration`       | text | Duration between reverse geocoding executions. Minimum: 1 minute                                 |
| `language`              | text | Preferred language of the result. Only necessary if openHAB locale settings shall be overwritten |

Select preferred display `format` for reverse geocoding.
Options:

- `row_address`: `street` `house-number`, `postcode` `city` `district`
- `us_address`: `house-number` `street`, `city` `district` `postcode`
- `json`: unformatted JSON response

Note that [address fields](https://nominatim.org/release-docs/latest/api/Output/#addressdetails) may be missing e.g. for rural areas. 
Default format is `row_address`.

The `resolveDuration` defines the minimum time between two reverse geocoding transformations.
An external API is called to resolve the geo coordinates and it shall not be queried too frequent.
Channel updates within the duration are omitted.
After the configured duration expired the last received location will be transformed. 
Minimum configurable duration is 1 minute.
Default is 5 minutes (`5m`).

The API calls are performed with your openHAB locale settings.
This can be overwritten with `language` configuration parameter using [Java Locale format](https://www.oracle.com/java/technologies/javase/jdk21-suported-locales.html).

### Example 

```java
String <itemName> { channel="<locationChannelUID>"[profile="transform:osm-geocoding",format="us_address",resolveDuration="10m",language="en-US"]}
```

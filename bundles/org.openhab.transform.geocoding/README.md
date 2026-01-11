# Geocoding Profile Transformation Service

Transformation to convert 

- geo coordinates into human readable string address (reverse geocoding)
- string address description into geo coordinates (geocoding)

## Reverse Geocoding

The reverse geocoding is applied when the channel updates the `Location` with latitude and longitude geo coordinates.
These will be resolved into a human readable address.

Reverse geocoding is throttled by the profile with `resolveInterval` to avoid frequent calling.
 
## Geocoding

Geocoding  is applied if you send a string command towards the item.
The API is translating this search string into geo coordinates which are send via the channel towards the handler.
Of course this makes sense only if the channel is declared as writable.
Formulate your string command as precise as possible to avoid ambiguous results.
E.g. _Springfield US_ command will deliver multiple results and only one is chosen for the transformation. 

Geocoding is not throttled by the profile.

## Configuration

| Configuration Parameter | Type | Description                                                                                      |
|-------------------------|------|--------------------------------------------------------------------------------------------------|
| `provider`              | text | Provider which is used to execute geocoding request                                              |
| `resolveInterval`       | text | Interval of reverse geocoding executions. Minimum: 1 minute                                      |
| `format`                | text | Country specific address formatting                                                              |
| `language`              | text | Preferred language of the result. Only necessary if openHAB locale settings shall be overwritten |

Select `provider` which shall be used to resolve addresses.
Currently one provider [Nominatim / OpenStreetMap](#nominatim--openstreetmap-provider) is available which is the default option. 

The `resolveInterval` defines the minimum time between two reverse geocoding transformations.
An external API is called to resolve the geo coordinates and it shall not be queried too frequent.
Channel updates within the Interval are omitted.
After the configured interval is expired the last received location will be transformed. 
Minimum configurable interval is 1 minute.
Default is 5 minutes (`5m`).

Select preferred display `format` for reverse geocoding.
Options:

- Address format Rest of World `address_row`: `street` `house-number`, `postcode` `city` `district` as default
- Address format US/UK `address_us`: `house-number` `street`, `city` `district` `postcode`
- Unformatted JSON response: `json` 

Note that [address fields](https://nominatim.org/release-docs/latest/api/Output/#addressdetails) may be missing e.g. for rural areas. 

The API calls are performed with your openHAB locale settings.
This can be overwritten with `language` configuration parameter using [Java Locale format](https://www.oracle.com/java/technologies/javase/jdk21-suported-locales.html).

## Provider

### Nominatim / OpenStreetMap Provider

Geocoding transformation provider using [Nominatim API for OpenStreetMap](https://nominatim.org/release-docs/latest/) to resolve

- geo coordinates into a human readable string ([reverse geocoding](https://nominatim.org/release-docs/latest/api/Reverse/))   
- an address string into geo coordinates ([geocoding](https://nominatim.org/release-docs/latest/api/Search/)) 

**You must respect the** [Nominatim Usage Policy](https://operations.osmfoundation.org/policies/nominatim/)!
You need to estimate your call frequency towards the _Nominatim_ provider.
For reverse geocoding the configuration parameter `resolveInterval` with minimum resolve time of 1 minute shall fulfill the throttling requirements.
For geocoding there's no throttling.
Each user needs to respect the maximum allowed frequency of 1 call per second.
The required `User-Agent` is provided by this transformation profile. 
Credits to [Nominatim](https://nominatim.org) and [OpenStreetMap](https://www.openstreetmap.org/) to provide this free service!
 
### Example 

```java
String <itemName> { channel="<locationChannelUID>"[profile="transform:geocoding",provider="nominatim-osm",format="us_address",resolveInterval="10m",language="en-US"]}
```

# Geocoding Profile Transformation Service

**Note**
This isn't an _instant_ transformation profile!
It's using OpenStreetMap API service to convert geo coordinates into an address an vice versa!
This takes time and it cannot be performed with e.g. secondly updates.
Minimum required time is 1 minute.

## Rationale
Several bindings / services are providing location based channels like

- MercedesMe, Volvo (Vehicles)
- torque2mqtt (Vehicles)
- gps trackers
- e-bikes

Transfomration can be used to translate
- geo coordinates into a human readable string (reverse geocoding)   
- an address string into geo coordinates (geocoding) if channel is writeable

 
## Revesre Geocoding

Translates geo coordinate state updates into human readable string.
Based on the location values are present or not.
While driving on the highway address information is limited while parking at your home location should be more precise.

## Geocoding

Translates a string into geo coordinates. 
Be very precise on the search string.
If the search is ambigious it may be translated into wrong geo coordinates.

## Configuration

Uses open streetmap API - decoding takes time

duration - minimum time between reverse geocoding resolving

```java
String <itemName> { channel="<channelUID>"[profile="transform:GEO">]}
```

https://nominatim.org/release-docs/latest/api/Output/

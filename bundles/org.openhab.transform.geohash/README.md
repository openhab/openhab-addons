# Geohash Transformation Service

Transforms the input (likely to be a PointType expressing latitude,longitude) to the corresponding geohash.
This transform also handle the opposite operation, transforming a geohash into equivalent latitude and longitude.

See details about GeoHashes from [Wikipedia](https://en.wikipedia.org/wiki/Geohash)

The first parameter passed to Geohash transform is the precision level (from 1 to 12), the second being the coordinates
of the point to transform.

## Example

### General Setup


**Rule**

```java
rule "Your Rule Name"
when
    Item YourTriggeringItem changed
then
    var geohash = transform("GEOHASH","6", YourTriggeringItem.state.toString)
    yourFormattedItem.sendCommand(formatted.toString) 
end
```

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String Voc_Geohash "Position Geohash [%s]"  {channel="volvooncall:vehicle:glh:XC60:position#location" [profile="transform:GEOHASH", precision="6"]}

```


# Ondilo Binding

This binding integrates Ondilo ICO pool monitoring devices with openHAB, allowing you to monitor and automate your pool environment using openHAB’s rules and UI.

## Supported Things

`bridge:` Represents your Ondilo account (OAuth2)

`ondilo:` Represents an individual pool device

Ondilo ICO Pool as well as Spa devices are supported. Chlor as well as salt water.

## Discovery

Pools are discovered automatically after the bridge is authorized and online. Each pool will appear as a new Thing in the inbox.

## Thing Configuration

### Ondilo Bridge

- **url**: The URL of the openHAB instance. Required for the redirect during oAuth2 flow (e.g. `http://localhost:8080`)
- **refreshInterval**: Polling interval of the bridge (i.e. config changes) (default: `1800 s`)

### Ondilo Pool/Spa

- **id**: The Id of an Ondilo ICO Pool/Spa device. Set via discovery service (e.g. `1234`)
- **refreshInterval**: Polling interval of the device (i.e. measures) (default: `600 s`)

ICO takes measures every hour. Higher polling will not increase the update interval.
The requests to the Ondilo Customer API are limited to the following per user quotas:

- 5 requests per second
- 30 requests per hour

Bridge Thing performs 1 request per cycle - 2 per hour with default interval
Pool/Spa Thing performs 3 request per cycle - 18 per hour with default interval

## Channels

### Measures Channels

| Channel ID                | Type                    | Advanced | Access | Description                                 |
|---------------------------|-------------------------|----------|--------|---------------------------------------------|
| temperature               | Number:Temperature      | false    | R      | Water temperature in the pool               |
| ph                        | Number                  | false    | R      | pH value of the pool water                  |
| orp                       | Number:ElectricPotential| false    | R      | Oxidation-reduction potential (ORP)         |
| salt                      | Number:Density          | false    | R      | Salt concentration in the pool              |
| tds                       | Number:Density          | false    | R      | Total dissolved solids in the pool          |
| battery                   | Number:Dimensionless    | false    | R      | Battery level of the device                 |
| rssi                      | Number:Dimensionless    | false    | R      | Signal strength (RSSI)                      |
| value-time                | DateTime                | true     | R      | Timestamp of the set of measures            |
| poll-update               | Switch                  | true     | R/W    | Poll status update from the cloud (get latest measures, not a trigger for new measures) |

### Recommendations Channels

| recommendation-id         | Number                  | true     | R      | Unique ID of the current recommendation     |
| recommendation-title      | String                  | false    | R      | Title of the current recommendation         |
| recommendation-message    | String                  | false    | R      | Message of the current recommendation       |
| recommendation-created_at | String                  | true     | R      | Creation time of the current recommendation |
| recommendation-updated_at | String                  | true     | R      | Last update time of the current recommendation |
| recommendation-status     | String                  | false    | R      | Status of the current recommendation        |
| recommendation-deadline   | String                  | true     | R      | Deadline of the current recommendation      |

## Example

### ondilo.thing

```Java
Bridge ondilo:bridge:mybridge [ url="http://localhost:8080", refreshInterval=1800 ]  {
    Thing ondilo "<id_received_from_discovery>" [ mowerId="<id_received_from_discovery>" ] {
    }
```

### ondilo.items

```java
Number:Temperature        Ondilo_Temperature  "Pool Temperature [%.1f %unit%]"  { channel="ondilo:ondilo:mybridge:myOnilo:measure#temperature" }
Number                    Ondilo_pH           "Pool pH [%d]"                    { channel="ondilo:ondilo:mybridge:myOnilo:measure#ph" }
Number:ElectricPotential  Ondilo_ORP          "Pool ORP [%.1f %unit%]"          { channel="ondilo:ondilo:mybridge:myOnilo:measure#orp" }
Number:Density            Ondilo_Salt         "Pool Salt [%.0f %unit%]"         { channel="ondilo:ondilo:mybridge:myOnilo:measure#salt" }
Number:Dimensionless      Ondilo_Battery      "Pool Battery [%d %]"             { channel="ondilo:ondilo:mybridge:myOnilo:measure#battery" }
Number:Dimensionless      Ondilo_RSSI         "Pool RSSI [%.0f]"                { channel="ondilo:ondilo:mybridge:myOnilo:measure#rssi" }

String                    Ondilo_RecTitle     "Recommendation Title [%s]"       { channel="ondilo:ondilo:mybridge:myOnilo:recommendation#title" }
String                    Ondilo_RecMessage   "Recommendation Message [%s]"     { channel="ondilo:ondilo:mybridge:myOnilo:recommendation#message" }
String                    Ondilo_RecStatus    "Recommendation Status [%s]"      { channel="ondilo:ondilo:mybridge:myOnilo:recommendation#status" }
```

### ondilo.sitemap

```perl
sitemap demo label="Ondilo" {
    Frame label="Measures" {
        Text        item=Ondilo_Temperature
        Text        item=Ondilo_pH
        Text        item=Ondilo_ORP
        Text        item=Ondilo_Battery
        Text        item=Ondilo_RSSI
    }
    Frame label="Recommendations" {
        Text        item=Ondilo_RecTitle
        Text        item=Ondilo_RecMessage
        Text        item=Ondilo_RecStatus
    }
}
```

## Troubleshooting

- If authorization fails, check the openHAB log for error messages and verify your redirect URI
- For more details, enable TRACE logging for `org.openhab.binding.ondilo`

## Resources

- [Ondilo API Documentation](https://interop.ondilo.com/docs/api/customer/v1)
- [openHAB Community Forum](https://community.openhab.org/t/request-ondilo-binding/98164)

# Ondilo Binding

This binding integrates Ondilo ICO pool monitoring devices with openHAB, allowing you to monitor and automate your pool environment using openHAB’s rules and UI.

## Supported Things

- `account:` Represents your Ondilo Account (authentication using OAuth2 flow)
- `ondilo:` Represents an individual Ondilo ICO device

Ondilo ICO Pool as well as Spa devices are supported.
Chlor as well as salt water.

## Discovery

Ondilo ICOs are discovered automatically after the `account` is authorized and online.
Each Ondilo ICO will appear as a new Thing in the inbox.

## Thing Configuration

### `account` Thing Configuration

- **url**: The URL of the openHAB instance. Required for the redirect during OAuth2 authentication flow (e.g. `http://localhost:8080`)
- **refreshInterval**: Polling interval in seconds (default: `900 s`)

### `ondilo` Thing Configuration

- **id**: The Id of an Ondilo ICO device. Set via discovery service (e.g. `12345`)

Ondilo ICO takes measures every hour.
Higher polling will not increase the update interval.
The binding automatically adjusts the polling schedule to match the expected time of the next measure, which is typically 1 hour (plus 4 minutes buffer) after the previous measure.

The requests to the Ondilo Customer API are limited to the following per user quotas:

- 5 requests per second
- 30 requests per hour

Example using default interval:

- `account` Thing performs 2 request per cycle - 8 requests per hour per Ondilo Account
- `ondilo` Thing performs 4 requests per cycle - 16 requests per hour per Ondilo ICO

## Channels

### `account` Channels

| Channel ID                | Type                    | Advanced | Access | Description                                            |
|---------------------------|-------------------------|----------|--------|--------------------------------------------------------|
| poll-update               | Switch                  | true     | R/W    | Poll status update from the cloud (get latest measures, not a trigger for new measures) |

### Measures Channels

| Channel ID                | Type                     | Advanced | Access | Description                                            |
|---------------------------|--------------------------|----------|--------|--------------------------------------------------------|
| temperature               | Number:Temperature       | false    | R      | Water temperature in the pool                          |
| temperature-trend         | Number:Temperature       | true     | R      | Change in water temperature since last measure         |
| ph                        | Number                   | false    | R      | pH value of the pool water                             |
| ph-trend                  | Number                   | true     | R      | Change in pH value since last measure                  |
| orp                       | Number:ElectricPotential | false    | R      | Oxidation-reduction potential (ORP)                    |
| orp-trend                 | Number:ElectricPotential | true     | R      | Change in ORP since last measure                       |
| salt                      | Number:Density           | false    | R      | Salt concentration in the pool (salt pools only)       |
| salt-trend                | Number:Density           | true     | R      | Change in salt concentration since last measure        |
| tds                       | Number:Density           | false    | R      | Total dissolved solids in the pool (chlor pools only)  |
| tds-trend                 | Number:Density           | true     | R      | Change in TDS since last measure                       |
| battery                   | Number:Dimensionless     | false    | R      | Battery level of the device                            |
| rssi                      | Number:Dimensionless     | false    | R      | Signal strength (RSSI)                                 |
| value-time                | DateTime                 | true     | R      | Timestamp of the set of measures                       |

### Recommendations Channels

| Channel ID                | Type                    | Advanced | Access | Description                                            |
|---------------------------|-------------------------|----------|--------|--------------------------------------------------------|
| recommendation-id         | Number                  | true     | R      | Unique ID of the current recommendation                |
| recommendation-title      | String                  | false    | R      | Title of the current recommendation                    |
| recommendation-message    | String                  | false    | R      | Message of the current recommendation                  |
| recommendation-created-at | String                  | true     | R      | Creation time of the current recommendation            |
| recommendation-updated-at | String                  | true     | R      | Last update time of the current recommendation         |
| recommendation-status     | String                  | false    | R/W    | Status of the current recommendation (`waiting`/`ok`)<br/>`sendCommand("ok")` to validate current `waiting` recommendation |
| recommendation-deadline   | String                  | true     | R      | Deadline of the current recommendation                 |

### Configuration Channels

| Channel ID                | Type           | Advanced | Access | Description                                   |
|---------------------------|----------------|----------|--------|-----------------------------------------------|
| temperature-low           | Number         | true     | R      | Minimum water temperature                     |
| temperature-high          | Number         | true     | R      | Maximum water temperature                     |
| ph-low                    | Number         | true     | R      | Minimum pH value                              |
| ph-high                   | Number         | true     | R      | Maximum pH value                              |
| orp-low                   | Number         | true     | R      | Minimum ORP value                             |
| orp-high                  | Number         | true     | R      | Maximum ORP value                             |
| salt-low                  | Number         | true     | R      | Minimum salt concentration (salt pools only)  |
| salt-high                 | Number         | true     | R      | Maximum salt concentration (salt pools only)  |
| tds-low                   | Number         | true     | R      | Minimum TDS value (chlor pools only)          |
| tds-high                  | Number         | true     | R      | Maximum TDS value (chlor pools only)          |

## Full Example

### Thing Configuration

```Java
Bridge ondilo:account:ondiloAccount [ url="http://localhost:8080", refreshInterval=900 ] {
    Thing ondilo 12345 [ id=12345 ] {   // 12345 is an example of the id received via discovery
    }
```

### Item Configuration

```java
Number:Temperature        Ondilo_Temperature  "Pool Temperature [%.1f %unit%]"  { channel="ondilo:ondilo:ondiloAccount:12345:measure#temperature" }
Number                    Ondilo_pH           "Pool pH [%d]"                    { channel="ondilo:ondilo:ondiloAccount:12345:measure#ph" }
Number:ElectricPotential  Ondilo_ORP          "Pool ORP [%.1f %unit%]"          { channel="ondilo:ondilo:ondiloAccount:12345:measure#orp" }
Number:Density            Ondilo_Salt         "Pool Salt [%.0f %unit%]"         { channel="ondilo:ondilo:ondiloAccount:12345:measure#salt" }
Number:Dimensionless      Ondilo_Battery      "Pool Battery [%d %]"             { channel="ondilo:ondilo:ondiloAccount:12345:measure#battery" }
Number:Dimensionless      Ondilo_RSSI         "Pool RSSI [%.0f]"                { channel="ondilo:ondilo:ondiloAccount:12345:measure#rssi" }

String                    Ondilo_RecTitle     "Recommendation Title [%s]"       { channel="ondilo:ondilo:ondiloAccount:12345:recommendation#title" }
String                    Ondilo_RecMessage   "Recommendation Message [%s]"     { channel="ondilo:ondilo:ondiloAccount:12345:recommendation#message" }
String                    Ondilo_RecStatus    "Recommendation Status [%s]"      { channel="ondilo:ondilo:ondiloAccount:12345:recommendation#status" }
```

### Sitemap Configuration

```perl
sitemap demo label="Ondilo ICO" {
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

- If authorization fails, check the openHAB log for error messages and verify your redirect URI `url`
- For more details, enable TRACE logging for `org.openhab.binding.ondilo`

## Resources

- [Ondilo API Documentation](https://interop.ondilo.com/docs/api/customer/v1)
- [openHAB Community Forum](https://community.openhab.org/t/request-ondilo-binding/98164)
- [Pool Monitor Card Widget](https://community.openhab.org/t/pool-monitor-card/164693)

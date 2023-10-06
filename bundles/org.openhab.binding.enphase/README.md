# Enphase Binding

This is the binding for the [Enphase](https://enphase.com/) Envoy Solar Panel gateway.
The binding uses the local API of the Envoy gateway.
Some calls can be made without authentication and some use a user name and password.
The default user name is `envoy` and the default password is the last 6 numbers of the serial number.
The Envoy gateway updates the data every 5 minutes.
Therefore using a refresh rate shorter doesn't provide more information.

## Supported Things

The follow things are supported:

- `envoy` The Envoy gateway thing, which is a bridge thing.
- `inverter` An Enphase micro inverter connected to a solar panel.
- `relay`  An Enphase relay.

Not all Envoy gateways support all channels and things.
Therefore some data on inverters and the relay may not be available.
The binding auto detects which data is available and will report this in the log on initialization of the gateway bridge.

## Discovery

The binding can discover Envoy gateways, micro inverters and relays.
If login access is needed the Bridge `envoy` needs to be configured after discovering and adding before other things can be discovered.
In that case, after configuring the login, run discovery again.

## Thing Configuration

### Bridge configuration

Depending on the software version of the Envoy gateway thing `envoy` there are different configuration options needed.
Newer versions of the Envoy software (> version 7) require a different authentication method.
Because the configuration is different, different bridge things are available.

The following options are relevant for all envoy versions:

| parameter    | required | description                                                                                                 |
|--------------|----------|-------------------------------------------------------------------------------------------------------------|
| serialNumber | yes      | The serial number of the Envoy gateway which can be found on the gateway                                    |
| hostname     | no       | The host name/ip address of the Envoy gateway. Leave empty to auto detect                                   |
| refresh      | no       | Period between data updates. The default is the same 5 minutes the data is actually refreshed on the Envoy  |

#### Envoy below version 7

For Envoy versions below 7 has the following authentication configuration options are relevant:

| parameter    | required | description                                                                                                 |
|--------------|----------|-------------------------------------------------------------------------------------------------------------|
| username     | no       | The user name to the Envoy gateway. Leave empty when using the default user name                            |
| password     | no       | The password to the Envoy gateway. Leave empty when using the default password                              |

#### Envoy from version 7

For Envoy versions 7 and newer has the following authentication configuration options are relevant:

| parameter    | required | description                                                                                                 |
|--------------|----------|-------------------------------------------------------------------------------------------------------------|
| autoJwt      | yes      | Specify if the JWT access token should be obtained by logging in or if the jwt is provided manually         |
| username     | yes/no   | The user name to the Entrez server. Required if auto Jwt is true                                            |
| password     | yes/no   | The password to the Entrez server. Required if auto Jwt is true                                             |
| siteName     | yes/no   | The name of the site. Can be found above the Site Id in the Enphase app. Required when autoJwt is true      |
| jwt          | yes/no   | The jwt is required if autoJWT is false, if it's true jwt is not used                                       |

### Thing configuration

The micro inverter `inverter` and `relay` things have only 1 parameter:

| parameter    | required | description                       |
|--------------|----------|-----------------------------------|
| serialNumber | yes      | The serial number of the inverter |

## Channels

The `envoy` thing has can show both production as well as consumption data.
There are channel groups for `production` and `consumption` data.
The `consumption` data is only available if the gateway reports this.
An example of a production channel name is: `production#wattsNow`.

| channel            | type          | description                           |
|--------------------|---------------|---------------------------------------|
| wattHoursToday     | Number:Energy | Watt hours produced today             |
| wattHoursSevenDays | Number:Energy | Watt hours produced the last 7 days   |
| wattHoursLifetime  | Number:Energy | Watt hours produced over the lifetime |
| wattsNow           | Number:Power  | Latest watts produced                 |

The `inverter` thing has the following channels:

| channel         | type         | description                          |
|-----------------|--------------|--------------------------------------|
| lastReportWatts | Number:Power | Last reported power delivery         |
| maxReportWatts  | Number:Power | Maximum reported power               |
| lastReportDate  | DateTime     | Date of last reported power delivery |

The following channels are only available if supported by the Envoy gateway:

The `relay` thing has the following channels:

| channel         | type         | description                                            |
|-----------------|--------------|--------------------------------------------------------|
| relay           | Contact      | Status of the relay.                                   |
| line1Connected  | Contact      | If power line 1 is connected. If closed it's connected |
| line2Connected  | Contact      | If power line 2 is connected. If closed it's connected |
| line2Connected  | Contact      | If power line 3 is connected. If closed it's connected |

The `inverter` and `relay` have the following additional advanced channels:

| channel         | type               | description                          |
|-----------------|--------------------|--------------------------------------|
| producing       | Switch (Read Only) | If the device is producing           |
| communicating   | Switch (Read Only) | If the device is communicating       |
| provisioned     | Switch (Read Only) | If the device is provisioned         |
| operating       | Switch (Read Only) | If the device is operating           |

## Full Example

Things example:

```java
Bridge enphase:envoy:789012 "Envoy" [ serialNumber="12345789012" ] {
  Things:
    inverter 123456 "Enphase Inverter 123456" [ serialNumber="789012123456" ]
    inverter 223456 "Enphase Inverter 223456" [ serialNumber="789012223456" ]
}
```

Items example:

```java
Number:Power  envoyWattsNow          "Watts Now [%d %unit%]"          { channel="enphase:envoy:789012:production#wattsNow" }
Number:Energy envoyWattHoursToday    "Watt Hours Today [%d %unit%]"   { channel="enphase:envoy:789012:production#wattHoursToday" }
Number:Energy envoyWattHours7Days    "Watt Hours 7 Days [%.1f kWh]"   { channel="enphase:envoy:789012:production#wattHoursSevenDays" }
Number:Energy envoyWattHoursLifetime "Watt Hours Lifetime [%.1f kWh]" { channel="enphase:envoy:789012:production#wattHoursLifetime" }

Number:Power i1LastReportWatts "Last Report [%d %unit%]"                          { channel="enphase:inverter:789012:123456:lastReportWatts" }
Number:Power i1MaxReportWatts  "Max Report [%d %unit%]"                           { channel="enphase:inverter:789012:123456:maxReportWatts" }
DateTime     i1LastReportDate  "Last Report Date [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" { channel="enphase:inverter:789012:123456:lastReportDate" }

Number:Power i2LastReportWatts "Last Report [%d %unit%]"                          { channel="enphase:inverter:789012:223456:lastReportWatts" }
Number:Power i21MaxReportWatts "Max Report [%d %unit%]"                           { channel="enphase:inverter:789012:223456:maxReportWatts" }
DateTime     i2LastReportDate  "Last Report Date [%1$tY-%1$tm-%1$td %1$tH:%1$tM]" { channel="enphase:inverter:789012:223456:lastReportDate" }
```

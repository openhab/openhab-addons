# SolarEdge Binding

The SolarEdge binding retrieves live data from SolarEdge sites via the central web API.
This binding should in general be compatible with all inverter models that upload data to the SolarEdge portal.
Only read access is supported.

## Supported Things

This binding provides only one Thing type: `generic`, which represents a SolarEdge site or installation.
As the name suggests, it is generic, which means it applies to all available inverters.
Create one Thing per SolarEdge site ID available in your account; data from multiple inverters assigned to the same site is aggregated by SolarEdge.
Additional components such as batteries are automatically supported.
Inverters that have a meter attached allow more detailed measurements.
Either a SolarEdge Modbus meter or an S0 meter (export or consumption meter) can be used.
While the S0 meter is the cheaper solution, the SolarEdge meter can be used as a combined import and export meter and therefore allows even more detailed measurements.
For more details please see here:

- [SolarEdge meter](https://www.solaredge.com/products/pv-monitoring/accessories/css-wattnode-modbus-meter)
- [Avoiding Feed-In limitations with consumption meters](https://www.solaredge.com/solutions/feed-in-limitation-and-metering-solution#)
- [Detailed description of meter setup](https://solaredge.com/sites/default/files/feed-in_limitation_application_note.pdf)

## Discovery

Auto-Discovery is not supported, as access requires authentication.

## Thing Configuration

The following configuration parameters are available for this Thing:

- **tokenOrApiKey** (optional for OAuth)<br>
The official API key when using Monitoring API V1, the App API Key for Fleet Access when using Monitoring API V2 with `API_KEY` authentication, or, when using the unofficial private API, a token that can be retrieved from your browser cookie store while logged into the SolarEdge website.
For information about obtaining a Monitoring API V1 key, see the [SolarEdge video](https://www.youtube.com/watch?v=iR26nmL5bXg).
The private API token is stored in a cookie called `SPRING_SECURITY_REMEMBER_ME_COOKIE`.
When using this token, see also `usePrivateApi` and `meterInstalled`.
For Firefox, use the built-in [Storage Inspector](https://developer.mozilla.org/en-US/docs/Tools/Storage_Inspector) to retrieve the token.

- **publicApiVersion** (optional)<br>
Version of the official Monitoring API: `V1` (default) or `V2`.
The [SolarEdge developer documentation](https://api-docs.solaredge.com/) presents Monitoring API V2 as the live production API on the SolarEdge ONE for Developers platform and no longer marks it as beta.
Monitoring API V1 is planned for deprecation on November 1, 2026; new integrations should use V2.

- **publicApiAuthentication** (optional)<br>
Authentication for Monitoring API V2: `API_KEY` (default) for a Fleet Access App API Key in `tokenOrApiKey`, or `OAUTH` for site access.

- **oAuthClientId**, **oAuthClientSecret** (required for OAuth)<br>
Credentials of an application registered in the SolarEdge developer portal.
Configure the application's redirect URL to point to `http(s)://<openhab-host>:<port>/solaredge/oauth/callback`.
After the Thing is initialized, open the authorization URL shown in its status description.
The access token and each rotated refresh token are stored by openHAB; they do not belong in the Thing configuration.

- **solarId** (required)<br>
ID of your site at SolarEdge (can be found in the URL after successful login: <https://monitoring.solaredge.com/solaredge-web/p/site/> **&lt;solarId&gt;** /#/dashboard)

- **usePrivateApi** (optional)<br>
Can be set to true to use the private API.
The private API has no limit regarding query frequency but is less stable.
The private API will only gather live data if a meter is available.
Monitoring API V1 has a limit of 300 queries per day but should be more reliable and stable.
Set this to true when using a token retrieved from the browser in `tokenOrApiKey`.
See also `meterInstalled`. (Default: false)

- **meterInstalled** (optional)<br>
Can be set to true for setups that contain a meter that is connected to the inverter.
A meter allows more detailed data retrieval.
This must be set to true when using a token retrieved from the browser in `tokenOrApiKey`.
With Monitoring API V1, this can be set either to true or false when using the API key.
Monitoring API V2 retrieves available meter and storage telemetry independently of this setting. (Default: false)

- **liveDataPollingInterval** (optional)<br>
Interval (minutes) in which live data values are retrieved from SolarEdge.
Monitoring API V1 requires at least 10 minutes; Monitoring API V2 and the private API allow intervals down to 1 minute.
For V2, select an interval that fits the credits and calls-per-minute limit of your developer tier. (Default: 10)

- **aggregateDataPollingInterval** (optional)<br>
Interval (minutes) in which aggregate data values are retrieved from SolarEdge.
Monitoring API V1 requires at least 60 minutes; Monitoring API V2 and the private API allow intervals down to 5 minutes.
For V2, select an interval that fits the credits and calls-per-minute limit of your developer tier. (Default: 60)

- **batteryCriticalLevel** (optional)<br>
Used with the private API and Monitoring API V2.
Battery charge level below which the battery is considered critical. (Default: 10)

## Monitoring API V2 OAuth Authorization

OAuth site access is intended for accessing a specific SolarEdge site on behalf of its owner.
The SolarEdge username and password are entered only on the SolarEdge authorization page and are never stored in openHAB.
A browser is normally required only for the initial authorization; openHAB subsequently renews the access automatically using the stored refresh token.

### SolarEdge Application Setup

1. Register an application in the [SolarEdge developer portal](https://developer.solaredge.com/).
1. Enable the `SITE_DATA` and `DEVICE_DATA` permissions described in the official [Authentication documentation](https://api-docs.solaredge.com/docs/developer-platform/b087f82c79d78-authentication).
   `DEVICE_DATA` is required for meter and storage telemetry.
1. Configure the application's redirect URL as:

   ```text
   http(s)://<openhab-host>:<port>/solaredge/oauth/callback
   ```

   The scheme, host, port, and path must match the URL registered at SolarEdge.
   The browser performing the authorization must be able to reach this openHAB URL after SolarEdge redirects it.
1. Copy the generated client ID and client secret into the Thing configuration.
   Treat the client secret as a password and do not publish it.

### Thing Setup and Initial Authorization

Configure the Thing with:

- `usePrivateApi=false`
- `publicApiVersion=V2`
- `publicApiAuthentication=OAUTH`
- the SolarEdge site ID in `solarId`
- the registered application's credentials in `oAuthClientId` and `oAuthClientSecret`

Do not enter the SolarEdge username, password, access token, or refresh token in `tokenOrApiKey`.

After initialization, the Thing changes to `OFFLINE (CONFIGURATION_PENDING)` and displays a clickable SolarEdge authorization link in its status description.
The complete URL is also available in the `oauthAuthorizationUrl` Thing property.
Open the link and complete these steps:

1. Sign in directly on the SolarEdge website.
1. Approve access to the site.
1. Allow SolarEdge to redirect the browser to the configured openHAB callback URL.
1. Wait for the success page, then return to openHAB.

The binding exchanges the one-time authorization code for an access token and a refresh token.
Both tokens are stored in the openHAB storage associated with the Thing.
Access tokens are renewed automatically, and every rotated refresh token returned by SolarEdge replaces the previously stored refresh token.
SolarEdge currently issues access tokens with a lifetime of two hours; the binding uses the lifetime returned by the token endpoint and refreshes shortly before expiry.
After a successful API request, the Thing changes to `ONLINE`.

If authorization is revoked or token renewal permanently fails, the Thing returns to `CONFIGURATION_PENDING` and must be authorized again using the new URL in its status description.

OAuth Site Access differs from Fleet Access authentication.
An App API Key for Fleet Access is entered in `tokenOrApiKey` and uses `publicApiAuthentication=API_KEY`; it does not require this browser authorization workflow.
Both authentication methods use the same Monitoring API V2 endpoints and channel mappings.

### Monitoring API V2 Usage Limits

Monitoring API V2 usage is governed by the calls-per-minute limit and monthly credit allowance of the SolarEdge developer tier.
These limits are shared by all applications in the same developer account.
See the official [Tiers and Rate Limits](https://api-docs.solaredge.com/docs/developer-platform/pwh8p4k2wtajh-tiers-and-rate-limits) documentation for current values.

One live polling cycle uses three API requests: site power, meter telemetry, and storage telemetry.
One aggregate polling cycle uses three API requests: production, meter, and storage time series at daily resolution for day, week, and month values.
The three corresponding requests at monthly resolution for year values run once per day.
With the default intervals of 10 minutes for live data and 60 minutes for aggregate data, continuous polling produces approximately 15,210 requests in 30 days, excluding authentication checks and retries.
This exceeds the current Free tier allowance, so increase the polling intervals or select a suitable developer tier before using V2 continuously.
During testing in August 2026, SolarEdge continued to return successful responses after the developer portal showed 4,684 credits used in a cycle with 2,000 included Free tier credits.
A subsequent long-running binding test completed approximately 4,400 Monitoring API requests per 24 hours and 12 OAuth token rotations without an HTTP error or Thing status change; the portal showed 2,198 calls and credits for a single day.
This contradicts the documented Free tier behavior and indicates that the included credit allowance was not enforced as an immediate hard API limit during that test.
This observed behavior is not guaranteed and may change; configure polling intervals to remain within the allowance of the selected developer tier.
The Thing property `apiCallsLast30Days` shows the locally recorded number of Monitoring API V2 requests attempted by this Thing during the last 720 hours.
The counter is retained across openHAB restarts and includes authentication checks and retries, but not OAuth token requests or calls made by other Things or applications sharing the same SolarEdge developer account.
If provided by SolarEdge, the Thing properties `apiRateLimitMinute` and `apiRateLimitRemainingMinute` show the minute limit and its remaining calls from the latest response.
On HTTP 429, `apiRateLimitRetryAfter` shows the gateway's retry delay when available; the binding does not immediately retry rate-limited requests.

## Channels

Available channels depend on the specific setup, e.g., if a meter and/or a battery is present.
All numeric channels use the [UoM feature](https://openhab.org/blog/2018/02/22/units-of-measurement.html).
This means you can easily change the desired unit (e.g., MWh instead of kWh) just in your item definition.
The following channels are currently available:

With Monitoring API V2, meter telemetry supplies grid import and export while storage telemetry supplies battery charge and discharge.
If SolarEdge does not provide a direct consumption series, the binding derives consumption using this energy balance:

```text
consumption = production + import + battery discharge - export - battery charge
```

The same calculation is used for live power and the day, week, month, and year energy channels.
Inputs used for one calculation must be no more than two minutes apart; until the complete balance is available, the binding keeps the previous channel state.
For aggregate periods, direct PV self-consumption and its coverage are derived as follows:

```text
self-consumption = production - export - battery charge
self-consumption coverage = self-consumption / consumption * 100
```

Negative results caused by measurement rounding are limited to zero.
For Monitoring API V2, `pv_status`, `load_status`, `grid_status`, and `battery_status` retain the existing `Active`/`Idle` values and are derived from the corresponding power values.
`battery_critical` is derived from `battery_level` and `batteryCriticalLevel`; it is `UNDEF` when no battery level is available.
The `meterInstalled` setting is not required for Monitoring API V2; channels remain `UNDEF` when SolarEdge does not provide the corresponding telemetry.
Remarks in the table that require `meterInstalled` apply to Monitoring API V1 and the private API, not to Monitoring API V2.

| Channel Type ID                               | Item Type            | Description                                      | Remark                                           |
| --------------------------------------------- | -------------------- | ------------------------------------------------ | ------------------------------------------------ |
| live#production                               | Number:Power         | Current PV production                            | generally available                              |
| live#pv_status                                | String               | Current PV status                                | requires meter attached and 'meterInstalled' set |
| live#consumption                              | Number:Power         | Current power consumption                        | requires meter attached and 'meterInstalled' set |
| live#load_status                              | String               | Current load status                              | requires meter attached and 'meterInstalled' set |
| live#battery_charge                           | Number:Power         | Current charge flow                              | requires battery                                 |
| live#battery_discharge                        | Number:Power         | Current discharge flow                           | requires battery                                 |
| live#battery_charge_discharge                 | Number:Power         | Current battery flow (+ charge, - discharge)     | requires battery                                 |
| live#battery_level                            | Number:Dimensionless | Current charge level                             | requires battery                                 |
| live#battery_status                           | String               | Current battery status                           | requires battery                                 |
| live#battery_critical                         | String               | true or false                                    | requires battery                                 |
| live#import                                   | Number:Power         | Current import from grid                         | requires meter attached and 'meterInstalled' set |
| live#export                                   | Number:Power         | Current export to grid                           | requires meter attached and 'meterInstalled' set |
| live#grid_status                              | String               | Current grid status                              | requires meter attached and 'meterInstalled' set |
| aggregate_day#production                      | Number:Energy        | Day Aggregate PV production                      | general available                                |
| aggregate_day#consumption                     | Number:Energy        | Day Aggregate power consumption                  | requires meter attached and 'meterInstalled' set |
| aggregate_day#selfConsumptionForConsumption   | Number:Energy        | Day consumption supplied directly by PV          | requires meter attached and 'meterInstalled' set |
| aggregate_day#selfConsumptionCoverage         | Number:Dimensionless | Day consumption coverage by direct PV            | requires meter attached and 'meterInstalled' set |
| aggregate_day#batterySelfConsumption          | Number:Energy        | Day Aggregate self consumption from battery      | requires battery                                 |
| aggregate_day#import                          | Number:Energy        | Day Aggregate import from grid                   | requires meter attached and 'meterInstalled' set |
| aggregate_day#export                          | Number:Energy        | Day Aggregate export to grid                     | requires meter attached and 'meterInstalled' set |
| aggregate_week#production                     | Number:Energy        | Week Aggregate PV production                     | requires meter attached and 'meterInstalled' set |
| aggregate_week#consumption                    | Number:Energy        | Week Aggregate power consumption                 | requires meter attached and 'meterInstalled' set |
| aggregate_week#selfConsumptionForConsumption  | Number:Energy        | Week consumption supplied directly by PV         | requires meter attached and 'meterInstalled' set |
| aggregate_week#selfConsumptionCoverage        | Number:Dimensionless | Week consumption coverage by direct PV           | requires meter attached and 'meterInstalled' set |
| aggregate_week#batterySelfConsumption         | Number:Energy        | Week Aggregate self consumption from battery     | requires battery                                 |
| aggregate_week#import                         | Number:Energy        | Week Aggregate import from grid                  | requires meter attached and 'meterInstalled' set |
| aggregate_week#export                         | Number:Energy        | Week Aggregate export to grid                    | requires meter attached and 'meterInstalled' set |
| aggregate_month#production                    | Number:Energy        | Month Aggregate PV production                    | general available                                |
| aggregate_month#consumption                   | Number:Energy        | Month Aggregate power consumption                | requires meter attached and 'meterInstalled' set |
| aggregate_month#selfConsumptionForConsumption | Number:Energy        | Month consumption supplied directly by PV        | requires meter attached and 'meterInstalled' set |
| aggregate_month#selfConsumptionCoverage       | Number:Dimensionless | Month consumption coverage by direct PV          | requires meter attached and 'meterInstalled' set |
| aggregate_month#batterySelfConsumption        | Number:Energy        | Month Aggregate self consumption from battery    | requires battery                                 |
| aggregate_month#import                        | Number:Energy        | Month Aggregate import from grid                 | requires meter attached and 'meterInstalled' set |
| aggregate_month#export                        | Number:Energy        | Month Aggregate export to grid                   | requires meter attached and 'meterInstalled' set |
| aggregate_year#production                     | Number:Energy        | Year Aggregate PV production                     | general available                                |
| aggregate_year#consumption                    | Number:Energy        | Year Aggregate power consumption                 | requires meter attached and 'meterInstalled' set |
| aggregate_year#selfConsumptionForConsumption  | Number:Energy        | Year consumption supplied directly by PV         | requires meter attached and 'meterInstalled' set |
| aggregate_year#selfConsumptionCoverage        | Number:Dimensionless | Year consumption coverage by direct PV           | requires meter attached and 'meterInstalled' set |
| aggregate_year#batterySelfConsumption         | Number:Energy        | Year Aggregate self consumption from battery     | requires battery                                 |
| aggregate_year#import                         | Number:Energy        | Year Aggregate import from grid                  | requires meter attached and 'meterInstalled' set |
| aggregate_year#export                         | Number:Energy        | Year Aggregate export to grid                    | requires meter attached and 'meterInstalled' set |

## Full Example

### Thing

- Minimum configuration

```java
solaredge:generic:se2200 [ tokenOrApiKey="...", solarId="..."]
```

- With polling intervals

```java
solaredge:generic:se2200[ tokenOrApiKey="...", solarId="...", liveDataPollingInterval=..., aggregateDataPollingInterval=... ]
```

- Monitoring API V2 with OAuth

```java
solaredge:generic:se2200 [ solarId="4711", publicApiVersion="V2", publicApiAuthentication="OAUTH", oAuthClientId="...", oAuthClientSecret="..." ]
```

- Monitoring API V2 with Fleet Access App API Key

```java
solaredge:generic:se2200 [ tokenOrApiKey="...", solarId="4711", publicApiVersion="V2", publicApiAuthentication="API_KEY" ]
```

- Full configuration

```java
solaredge:generic:se2200     [ tokenOrApiKey="secret", solarId="4711", meterInstalled=true, usePrivateApi=true, liveDataPollingInterval=15, aggregateDataPollingInterval=60 ]
```

- Multiple inverters

```java
solaredge:generic:home1 [ tokenOrApiKey="...", solarId="..."]
solaredge:generic:home2  [ tokenOrApiKey="...", solarId="..."]
```

### Items

```java
Number:Power            SE2200_Live_Production   "PV Produktion [%.2f %unit%]"    {channel="solaredge:generic:se2200:live#production"}
Number:Dimensionless    SE2200_Live_Level        "Batterieladung"                 {channel="solaredge:generic:se2200:live#battery_level"}
Number:Energy           SE2200_Day_Production    "PV Produktion [%.2f kWh]"       {channel="solaredge:generic:se2200:aggregate_day#production"}
```

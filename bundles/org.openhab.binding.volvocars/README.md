# Volvo Cars Binding

This binding integrates Volvo cars using the official [Volvo Cars APIs](https://developer.volvocars.com): the Connected Vehicle API, the Energy API and the Location API.
It reads the state of doors, windows, locks, fuel, battery, charging, trip statistics, location and maintenance information, and lets you lock and unlock the car, start and stop the climatization or the engine, and make the car honk or flash its lights.

The binding replaces the [VolvoOnCall Binding](https://www.openhab.org/addons/bindings/volvooncall/), which relies on the discontinued Volvo On Call API.

## Supported Things

- `account`: A bridge representing a Volvo ID account, accessed through an application you register on the Volvo Cars developer portal.
- `vehicle`: A car connected to the Volvo ID account.

All cars supported by the Volvo Cars app and the Volvo Cars APIs should work.
Which values and commands are available depends on the model, its powertrain and the market.
Channels for features the car does not have (e.g. fuel channels of an electric car or remote commands the car does not support) are removed automatically.

## Prerequisites

The Volvo Cars APIs can only be used through your own application on the [Volvo Cars developer portal](https://developer.volvocars.com).
The application must be **published**: only a published application gets the OAuth client credentials (client ID and client secret) the binding needs to sign in to your Volvo ID.
An unpublished application only provides the VCC API key and short-lived test tokens, which are not sufficient.

1. Sign in to the developer portal with the Volvo ID you use in the Volvo Cars app.
1. Create an application in your account.
   The portal shows a primary _VCC API key_ for it.
1. Click _Publish_ for the application and fill in the form:
   - Creator and public information, e.g. your name, your contact details and a business purpose like "Private openHAB integration".
     These details are not used by the binding.
   - Scopes: expand every section and select all scopes listed below.
     The binding requests all of them by default, and Volvo ID rejects the authorization with `invalid_scope` if the application lacks one of them.
   - Redirect URI: see [Redirect URI](#redirect-uri).
1. Confirm the summary.
   The confirmation page shows the client ID and the client secret.
   Note them together with the VCC API key.

### Redirect URI

After you have signed in, Volvo ID sends your browser to the redirect URI together with the authorization code.
Volvo is very strict about this URI:

- It must use `https`.
  Volvo rejects `http` URIs when publishing the application.
- It must match the `redirectUri` of the bridge exactly, including scheme, host, port, path and a trailing slash, if any.
- Only your browser opens it, so it does not have to be reachable from the internet.

There are two ways to complete the authorization:

- Register the authorization page of the binding on the HTTPS port of openHAB, e.g. `https://openhab:8443/volvocars` (replace `openhab` with the host name or IP address of your openHAB server).
  openHAB serves HTTPS on port 8443 with a self-signed certificate by default, so your browser shows a certificate warning that you have to accept.
  The authorization then completes automatically.
- Register any other `https` URI, e.g. one that is not reachable at all.
  Your browser shows an error page after you have signed in.
  Copy the complete URL from its address bar and paste it into the form on the authorization page of the binding, see [Authorization](#authorization).

### Scopes

Scopes used by the binding:

| Scope                                                                         | Used for                                               |
|-------------------------------------------------------------------------------|--------------------------------------------------------|
| `openid`                                                                      | Required                                               |
| `conve:vehicle_relation`                                                      | Vehicle list and details, discovery                    |
| `conve:doors_status`, `conve:lock_status`, `conve:windows_status`             | Groups `doors` and `windows`                           |
| `conve:engine_status`, `conve:odometer_status`, `conve:command_accessibility` | Group `status`                                         |
| `conve:fuel_status`, `conve:battery_charge_level`                             | Groups `fuel` and `battery`                            |
| `energy:capability:read`, `energy:state:read`                                 | Group `battery` (charging information)                 |
| `conve:trip_statistics`                                                       | Group `trip`                                           |
| `conve:diagnostics_workshop`, `conve:diagnostics_engine_status`               | Group `maintenance`                                    |
| `conve:brake_status`, `conve:tyre_status`, `conve:warnings`                   | Group `maintenance`                                    |
| `conve:commands`, `conve:climatization_start_stop`                            | Group `control`                                        |
| `conve:lock`, `conve:unlock`                                                  | Locking and unlocking                                  |
| `conve:engine_start_stop`                                                     | Remote engine start                                    |
| `conve:honk_flash`                                                            | Honk and flash                                         |
| `location:read`                                                               | Group `location`                                       |

If you cannot select some of the scopes for your application, list the scopes it has in the `scopes` parameter of the bridge.
Values that require a scope that was not granted remain `UNDEF`.

## Discovery

Once the `account` bridge is online, the cars connected to the Volvo ID account are discovered automatically.
Discovery runs in the background once an hour and can also be started manually.

## Thing Configuration

### `account` Bridge Configuration

| Name           | Type | Description                                                                                                             | Default | Required | Advanced |
|----------------|------|-------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| `clientId`     | text | Client ID of the application                                                                                            | N/A     | yes      | no       |
| `clientSecret` | text | Client secret of the application                                                                                        | N/A     | yes      | no       |
| `apiKey`       | text | VCC API key of the application                                                                                          | N/A     | yes      | no       |
| `redirectUri`  | text | Redirect URI exactly as registered for the application, e.g. `https://openhab:8443/volvocars`                           | N/A     | yes      | no       |
| `scopes`       | text | Space separated list of scopes to request. Leave empty to request all scopes listed in [Prerequisites](#prerequisites). | (all)   | no       | yes      |

### Authorization

After the bridge has been created, it stays offline until openHAB has been authorized to access the Volvo ID account:

1. Open the authorization page of the binding in a browser, e.g. `http://openhab:8080/volvocars` or `https://openhab:8443/volvocars`.
1. Click _Authorize_ next to the account and sign in with your Volvo ID.
1. Volvo redirects your browser to the configured redirect URI.
   If that is the authorization page of the binding, the authorization completes automatically.
   Otherwise, copy the complete URL from the address bar of your browser and paste it into the form on the authorization page.
   The authorization must be completed within 15 minutes.

The binding stores the refresh token in the openHAB user data and renews the access token automatically.
Volvo limits how long the authorization of a self-registered application stays valid.
Users of other integrations report that they have to sign in again about every 7 days.
When the authorization has expired or was revoked, the bridge goes offline with a hint to the authorization page and you have to authorize again.
Changing the client ID also requires a new authorization.

### `vehicle` Thing Configuration

| Name                  | Type    | Description                                                                                  | Default | Required | Advanced |
|-----------------------|---------|----------------------------------------------------------------------------------------------|---------|----------|----------|
| `vin`                 | text    | Vehicle Identification Number                                                                | N/A     | yes      | no       |
| `refreshInterval`     | integer | Interval in minutes to refresh doors, windows, status, fuel, battery, trip and location data | 5       | no       | no       |
| `slowRefreshInterval` | integer | Interval in minutes to refresh rarely changing maintenance data (service, warnings, tyres)   | 30      | no       | yes      |
| `engineRunTime`       | integer | Duration in minutes (1-15) the engine keeps running after a remote start                     | 15      | no       | yes      |

`slowRefreshInterval` must not be shorter than `refreshInterval`.

Every refresh issues several requests to the Volvo Cars APIs.
Keep the refresh interval moderate to stay within the request limits of your application.

## Channels

The channels are organized in groups.

### Group `doors`

| Channel       | Type    | Read/Write | Description                                                             |
|---------------|---------|------------|-------------------------------------------------------------------------|
| `lock`        | Switch  | RW         | Central lock (`ON` = locked). Send `ON` to lock and `OFF` to unlock.    |
| `front-left`  | Contact | R          | Front left door                                                         |
| `front-right` | Contact | R          | Front right door                                                        |
| `rear-left`   | Contact | R          | Rear left door                                                          |
| `rear-right`  | Contact | R          | Rear right door                                                         |
| `hood`        | Contact | R          | Hood                                                                    |
| `tailgate`    | Contact | R          | Tailgate                                                                |
| `tank-lid`    | Contact | R          | Fuel tank lid or charging port lid                                      |

Doors and windows reported as ajar are `OPEN`.

### Group `windows`

| Channel       | Type    | Read/Write | Description        |
|---------------|---------|------------|--------------------|
| `front-left`  | Contact | R          | Front left window  |
| `front-right` | Contact | R          | Front right window |
| `rear-left`   | Contact | R          | Rear left window   |
| `rear-right`  | Contact | R          | Rear right window  |
| `sunroof`     | Contact | R          | Sunroof            |

### Group `status`

| Channel          | Type          | Read/Write | Description                                                                           |
|------------------|---------------|------------|---------------------------------------------------------------------------------------|
| `engine-running` | Switch        | R          | Engine is running                                                                     |
| `odometer`       | Number:Length | R          | Total distance driven                                                                 |
| `availability`   | String        | R          | `AVAILABLE` or the reason the car cannot receive remote commands, e.g. `CAR_IN_USE`   |

### Group `fuel`

Only available for cars with a combustion engine.

| Channel                  | Type          | Read/Write | Description                      |
|--------------------------|---------------|------------|----------------------------------|
| `fuel-amount`            | Number:Volume | R          | Amount of fuel in the tank       |
| `distance-to-empty-tank` | Number:Length | R          | Remaining range on fuel          |

### Group `battery`

Only available for electric and plug-in hybrid cars.
Except for `battery-level` and `distance-to-empty-battery`, the values require a car supporting the Energy API.

| Channel                     | Type                   | Read/Write | Description                                                             |
|-----------------------------|------------------------|------------|-------------------------------------------------------------------------|
| `battery-level`             | Number:Dimensionless   | R          | State of charge of the high voltage battery                             |
| `electric-range`            | Number:Length          | R          | Remaining electric range                                                |
| `distance-to-empty-battery` | Number:Length          | R          | Remaining electric range according to the trip statistics               |
| `charger-connection-status` | String                 | R          | `CONNECTED`, `DISCONNECTED` or `FAULT`                                  |
| `charging-status`           | String                 | R          | `CHARGING`, `IDLE`, `DONE`, `DISCHARGING`, `SCHEDULED` or `ERROR`       |
| `charging-type`             | String                 | R          | `AC`, `DC` or `NONE`                                                    |
| `charger-power-status`      | String                 | R          | Power status of the charger, e.g. `PROVIDING_POWER`                     |
| `estimated-charging-time`   | Number:Time            | R          | Estimated time until the target battery level is reached                |
| `target-battery-level`      | Number:Dimensionless   | R          | Battery level the car charges to                                        |
| `charging-current-limit`    | Number:ElectricCurrent | R          | Charging current limit                                                  |
| `charging-power`            | Number:Power           | R          | Current charging power                                                  |

### Group `trip`

| Channel                      | Type          | Read/Write | Description                                                           |
|------------------------------|---------------|------------|-----------------------------------------------------------------------|
| `average-speed`              | Number:Speed  | R          | Average speed since the manual trip meter was reset                   |
| `average-speed-automatic`    | Number:Speed  | R          | Average speed since the automatic trip meter was reset                |
| `trip-meter-manual`          | Number:Length | R          | Distance since the manual trip meter was reset                        |
| `trip-meter-automatic`       | Number:Length | R          | Distance since the automatic trip meter was reset                     |
| `average-fuel-consumption`   | Number        | R          | Average fuel consumption in l/100km (combustion engine only)          |
| `average-energy-consumption` | Number        | R          | Average energy consumption in kWh/100km (electric and hybrid only)    |

### Group `location`

| Channel     | Type         | Read/Write | Description                       |
|-------------|--------------|------------|-----------------------------------|
| `position`  | Location     | R          | Location of the car               |
| `heading`   | Number:Angle | R          | Direction the car is heading to   |
| `timestamp` | DateTime     | R          | Time the location was recorded    |

The location is not available in all markets.

### Group `maintenance`

These values are refreshed with the maintenance refresh interval.

| Channel                   | Type          | Read/Write | Description                                                                                |
|---------------------------|---------------|------------|--------------------------------------------------------------------------------------------|
| `service-warning`         | String        | R          | Service status, e.g. `NO_WARNING` or `DISTANCE_DRIVEN_TIME_FOR_SERVICE`                    |
| `distance-to-service`     | Number:Length | R          | Remaining distance until the next service                                                  |
| `time-to-service`         | Number:Time   | R          | Remaining time until the next service                                                      |
| `engine-hours-to-service` | Number:Time   | R          | Remaining engine hours until the next service                                              |
| `washer-fluid-warning`    | String        | R          | Washer fluid level warning (`NO_WARNING`, `TOO_LOW`)                                       |
| `brake-fluid-warning`     | String        | R          | Brake fluid level warning (`NO_WARNING`, `TOO_LOW`)                                        |
| `oil-level-warning`       | String        | R          | Engine oil level warning (`NO_WARNING`, `SERVICE_REQUIRED`, `TOO_LOW`, `TOO_HIGH`)         |
| `coolant-level-warning`   | String        | R          | Engine coolant level warning (`NO_WARNING`, `TOO_LOW`)                                     |
| `tyre-front-left`         | String        | R          | Tyre pressure warning (`NO_WARNING`, `VERY_LOW_PRESSURE`, `LOW_PRESSURE`, `HIGH_PRESSURE`) |
| `tyre-front-right`        | String        | R          | Tyre pressure warning                                                                      |
| `tyre-rear-left`          | String        | R          | Tyre pressure warning                                                                      |
| `tyre-rear-right`         | String        | R          | Tyre pressure warning                                                                      |
| `light-failure`           | Switch        | R          | `ON` if at least one exterior light reports a failure                                      |
| `light-failures`          | String        | R          | Comma separated list of failed lights, e.g. `brakeLightLeft, fogLightRear`                 |

### Group `control`

| Channel               | Type   | Read/Write | Description                                                                    |
|-----------------------|--------|------------|--------------------------------------------------------------------------------|
| `climatization`       | Switch | W          | Send `ON` to start and `OFF` to stop the climatization                         |
| `engine-remote-start` | Switch | W          | Send `ON` to start the engine for the configured run time and `OFF` to stop it |
| `honk-flash`          | String | W          | Send `HONK`, `FLASH` or `HONK_AND_FLASH`                                       |
| `lock-reduced-guard`  | Switch | W          | Send `ON` to lock the car with reduced alarm guard, e.g. to leave a pet inside |
| `last-command-status` | String | R          | Result of the last command, e.g. `COMPLETED`, `REJECTED` or `FAILED: <reason>` |

`climatization`, `engine-remote-start` and `lock-reduced-guard` act as push buttons: they switch back to `OFF` once the command was sent successfully.
If sending the command failed, they keep their state and `last-command-status` shows the reason.

The car needs some time to execute a command.
The binding refreshes the state of the car 15 seconds after a command.

## Full Example

### `volvocars.things`

```java
Bridge volvocars:account:myaccount "Volvo Account" [ clientId="...", clientSecret="...", apiKey="...", redirectUri="https://openhab:8443/volvocars" ] {
    Thing vehicle xc40 "Volvo XC40" [ vin="YV1XZ00000000000", refreshInterval=5 ]
}
```

### `volvocars.items`

```java
Switch               Volvo_Locked          "Locked"                 { channel="volvocars:vehicle:myaccount:xc40:doors#lock" }
Contact              Volvo_Tailgate        "Tailgate [%s]"          { channel="volvocars:vehicle:myaccount:xc40:doors#tailgate" }
Number:Length        Volvo_Odometer        "Odometer [%.0f km]"     { channel="volvocars:vehicle:myaccount:xc40:status#odometer" }
Number:Dimensionless Volvo_Battery         "Battery [%.0f %%]"      { channel="volvocars:vehicle:myaccount:xc40:battery#battery-level" }
String               Volvo_Charging        "Charging [%s]"          { channel="volvocars:vehicle:myaccount:xc40:battery#charging-status" }
Location             Volvo_Position        "Position"               { channel="volvocars:vehicle:myaccount:xc40:location#position" }
Switch               Volvo_Climatization   "Climatization"          { channel="volvocars:vehicle:myaccount:xc40:control#climatization" }
String               Volvo_CommandStatus   "Last Command [%s]"      { channel="volvocars:vehicle:myaccount:xc40:control#last-command-status" }
```

### `volvocars.sitemap`

```perl
sitemap volvocars label="Volvo" {
    Frame label="Status" {
        Switch item=Volvo_Locked
        Text item=Volvo_Tailgate
        Text item=Volvo_Odometer
        Text item=Volvo_Battery
        Text item=Volvo_Charging
        Mapview item=Volvo_Position height=5
    }
    Frame label="Control" {
        Switch item=Volvo_Climatization
        Text item=Volvo_CommandStatus
    }
}
```

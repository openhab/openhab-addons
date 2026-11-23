# Pleos Binding

This binding integrates connected vehicles from **Hyundai**, **Kia**, and **Genesis** into openHAB using the **Pleos Vehicle Data API (For Private Users)**.
It provides read-only access to vehicle telematics data such as battery state of charge (SoC), charging status, fuel level, driving range, GPS location, door and closure states, tire pressures, climate control, seat heating/ventilation, and dashboard warning indicators.

> **Note:** Currently, the Pleos Vehicle Data API for Private Users is only available for vehicles registered in the **European Union (EU)**.

## Supported Things

| Thing Type ID | Type | Description |
| :--- | :--- | :--- |
| `account` | Bridge | Represents a Pleos Developer Playground account for a specific brand (`HYUNDAI`, `KIA`, or `GENESIS`) and manages API authentication and polling. |
| `vehicle` | Thing | Represents an individual connected vehicle linked to the Pleos account, identified by its Vehicle Identification Number (VIN). |

## Prerequisites & API Credentials

To use this binding, you need personal API credentials from the **Pleos Playground**:

1. Log in to the [Pleos Playground](https://developer.pleos.ai/playground) with your Hyundai, Kia, or Genesis account.
1. Navigate to **Vehicle Data API -> Getting Started -> For Private Users -> Issue a My Vehicle API Key**.
1. Consent to data sharing for your vehicle(s) and generate your **Client ID** and **Client Secret**.

## Discovery

Once the `account` bridge is configured and `ONLINE`, the binding automatically discovers all vehicles that have been authorized for data sharing in your Pleos account and adds them to the openHAB Inbox.
The Vehicle Identification Number (`vin`) is used as the representation property to prevent duplicate entries.

## Thing Configuration

### `account` Bridge Configuration

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :---: | :--- |
| `clientId` | text | Yes | - | The issued Client ID from your Pleos Developer Playground. |
| `clientSecret` | text | Yes | - | The corresponding Client Secret from your developer workspace. |
| `brand` | text | Yes | - | Target vehicle brand infrastructure (`HYUNDAI`, `KIA`, or `GENESIS`). |
| `refreshInterval` | integer | No | `15` | Polling interval in minutes to fetch updated vehicle data (minimum: `5`, maximum: `1440`). |
| `secretCreationDate` | text | No | - | Date when the Client Secret was generated (`YYYY-MM-DD`). Enables 90-day expiration tracking. |

## Account Channels

If `secretCreationDate` is configured on the `account` bridge, the binding automatically tracks the 90-day validity period of your Pleos Client Secret and provides the following channels:

| Channel ID | Item Type | Kind | Description |
| :--- | :--- | :---: | :--- |
| `secret-expires` | `DateTime` | State | Calculated expiration timestamp (90 days after `keyCreationDate`). |
| `secret-remaining-days` | `Number:Time` | State | Remaining days until the Client Secret expires (`d`). |
| `secret-expiry-warning` | `Switch` | State | Switches to `ON` when the remaining validity drops below 7 days. |
| `secret-expiry-event` | - | Trigger | Fires events (`EXPIRING_7_DAYS`, `EXPIRING_2_DAYS`, `EXPIRED`) at 7 days, 2 days, and every 6 hours below 2 days before expiration. |

> **Note:** The Pleos binding does not automatically renew the `clientSecret` when its validity period expires. It is the user's own responsibility to log in to their Pleos Playground account in a timely manner, renew the `clientSecret`, and update it within the Pleos API account "thing."

### `vehicle` Thing Configuration

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :---: | :--- |
| `vin` | text | Yes | - | The 17-character Vehicle Identification Number (VIN) of the vehicle. |

## Channels

All channels provided by the `vehicle` thing are read-only and organized into logical channel groups.
Channels specific to a powertrain type (such as high-voltage battery charging or fuel tank level) are added dynamically based on the detected vehicle type:

- **Electric vehicles (`EV`) and Plug-in Hybrids (`PE`)** provide the channels in the `charging` group.
- **Internal combustion engines (`GN`), Hybrids (`HV`), Plug-in Hybrids (`PE`), and Fuel Cell vehicles (`FV`)** provide the channels in the `fuel` group.
- If a vehicle does not support a specific powertrain feature, the corresponding channel group and its channels are not created on the Thing.

### Group `vehicle` (Vehicle Status)

| Channel ID                     | Item Type       | Description                                                             |
| :----------------------------- | :-------------- | :---------------------------------------------------------------------- |
| `vehicle#distance-to-empty`    | `Number:Length` | Estimated driving range on current charge or fuel.                      |
| `vehicle#distance-type`        | `String`        | Powertrain range estimation basis (`EV`, `GASOLINE`, `DIESEL`, `PHEV`). |
| `vehicle#location`             | `Location`      | Current geographical GPS position (`latitude,longitude,altitude`).     |
| `vehicle#latitude`             | `Number`        | Current GPS latitude of the vehicle.                                    |
| `vehicle#longitude`            | `Number`        | Current GPS longitude of the vehicle.                                   |
| `vehicle#altitude`             | `Number:Length` | Altitude above sea level (m).                                           |
| `vehicle#location-last-update` | `DateTime`      | Timestamp when the GPS position was recorded.                           |
| `vehicle#speed`                | `Number:Speed`  | Vehicle ground speed (km/h).                                            |
| `vehicle#heading`              | `Number:Angle`  | Driving direction heading angle (°).                                    |
| `vehicle#odometer`             | `Number:Length` | Cumulative vehicle mileage.                                             |
| `vehicle#ignition`             | `String`        | Primary ignition power state (`OFF`, `ACC`, `ON`, `START`).             |
| `vehicle#ignition-acc`         | `Switch`        | Indicates if the accessory circuit (infotainment/radio) is active.      |
| `vehicle#ignition-ign1`        | `Switch`        | Indicates if the vehicle electronics circuit is active.                 |
| `vehicle#ignition-ign3`        | `Switch`        | Indicates if the high-voltage / drive-ready circuit is active.          |
| `vehicle#sleep-mode`           | `String`        | Power-saving sleep mode status (`OFF`, `ON`, `INVALID`).                |
| `vehicle#parking-position`     | `Switch`        | Indicates whether the automatic transmission is in Park (`P`).          |
| `vehicle#last-update`          | `DateTime`      | Timestamp of the last data transmission from the vehicle.               |

### Group `charging` (High-Voltage Battery & Charging)

> These channels are created dynamically and are only present for vehicles that support high-voltage charging (`EV`, `PE`).

| Channel ID                    | Item Type              | Description                                                                                          |
| :---------------------------- | :--------------------- | :--------------------------------------------------------------------------------------------------- |
| `charging#soc`                 | `Number:Dimensionless` | Current high-voltage battery state of charge (%).                                                    |
| `charging#capacity`            | `Number:Energy`        | Maximum high-voltage battery capacity (Wh).                                                          |
| `charging#charging`            | `Switch`               | Indicates whether charging is currently active (`ON`/`OFF`).                                         |
| `charging#plugin`              | `String`               | Charge cable connection status (`UNPLUGGED`, `PLUGGED`, `INVALID`).                                  |
| `charging#charge-status`       | `String`               | Current charging mode (`NOT_CHARGING`, `CHARGING_AC`, `CHARGING_DC`, `FINISHED`, `WAITING_SCHEDULE`). |
| `charging#remain-time`         | `Number:Time`          | Estimated remaining charging time (minutes).                                                         |
| `charging#target-soc-standard` | `Number:Dimensionless` | Configured target state of charge for AC standard charging (%).                                      |
| `charging#target-soc-quick`    | `Number:Dimensionless` | Configured target state of charge for DC fast charging (%).                                          |

### Group `fuel` (Fuel & Tank Level)

> This channel is created dynamically and is only present for vehicles with a combustion engine or fuel tank (`GN`, `HV`, `PE`, `FV`).

| Channel ID        | Item Type              | Description                                                 |
| :---------------- | :--------------------- | :---------------------------------------------------------- |
| `fuel#fuel-level` | `Number:Dimensionless` | Current fuel tank level for combustion/hybrid vehicles (%). |

### Group `doors` (Doors & Closures)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `doors#door-front-left` | `String` | Front left door state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#door-front-right` | `String` | Front right door state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#door-rear-left` | `String` | Rear left door state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#door-rear-right` | `String` | Rear right door state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#hood` | `String` | Engine hood state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#trunk` | `String` | Trunk / tailgate state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |
| `doors#sunroof` | `String` | Sunroof state (`OPEN`, `CLOSED`, `LOCKED`, `INVALID`). |

### Group `tires` (Tires & Tire Pressure)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `tires#tire-pressure-front-left` | `Number:Pressure` | Measured front left tire pressure. |
| `tires#tire-pressure-front-right` | `Number:Pressure` | Measured front right tire pressure. |
| `tires#tire-pressure-rear-left` | `Number:Pressure` | Measured rear left tire pressure. |
| `tires#tire-pressure-rear-right` | `Number:Pressure` | Measured rear right tire pressure. |
| `tires#tire-warning-fl` | `Switch` | Low tire pressure warning for front left tire (`ON` = Warning, `OFF` = OK). |
| `tires#tire-warning-fr` | `Switch` | Low tire pressure warning for front right tire (`ON` = Warning, `OFF` = OK). |
| `tires#tire-warning-rl` | `Switch` | Low tire pressure warning for rear left tire (`ON` = Warning, `OFF` = OK). |
| `tires#tire-warning-rr` | `Switch` | Low tire pressure warning for rear right tire (`ON` = Warning, `OFF` = OK). |
| `tires#tire-warning-low-generic` | `Switch` | Generic low tire pressure warning indicator (`ON` = Warning, `OFF` = OK). |

### Group `climate` (Climate Control)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `climate#climate-status` | `Switch` | Indicates whether climate control is currently active (`ON`/`OFF`). |
| `climate#climate-target-temp` | `Number:Temperature` | Configured HVAC target temperature setpoint. |
| `climate#climate-mode` | `String` | Current operating mode of the HVAC system (`OFF`, `AUTO`, `MANUAL`, `DEFROST`, `ECO`). |
| `climate#climate-speed` | `Number` | HVAC fan blower speed level (`-1` = Auto, `0` = Off, `1`–`8` = Level). |
| `climate#outside-temperature` | `Number:Temperature` | Measured outside ambient temperature (if supported by the vehicle). |
| `climate#windshield-heater` | `Switch` | Indicates whether front windshield defrost heater is active. |
| `climate#steering-wheel-heater` | `Switch` | Indicates whether steering wheel heater is active. |

### Group `seats` (Seats Comfort)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `seats#seat-front-left-heat` | `String` | Front left seat heating intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-front-right-heat` | `String` | Front right seat heating intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-front-left-vent` | `String` | Front left seat ventilation intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-front-right-vent` | `String` | Front right seat ventilation intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-rear-left-heat` | `String` | Rear left seat heating intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-rear-right-heat` | `String` | Rear right seat heating intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-rear-left-vent` | `String` | Rear left seat ventilation intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |
| `seats#seat-rear-right-vent` | `String` | Rear right seat ventilation intensity (`OFF`, `LOW`, `MIDDLE`, `HIGH`). |

### Group `warnings` (Vehicle Warning Indicators)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `warnings#warning-wash-fluid` | `Switch` | Low washer fluid warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-brake-oil` | `Switch` | Low brake fluid warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-engine-oil` | `Switch` | Engine oil / check engine warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-smartkey-battery` | `Switch` | Smart key low battery warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-low-fuel` | `Switch` | Low fuel warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-headlamp` | `Switch` | Front headlamp defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-turn-front-left` | `Switch` | Front left turn signal defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-turn-front-right` | `Switch` | Front right turn signal defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-stop-rear-left` | `Switch` | Rear left brake lamp defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-stop-rear-right` | `Switch` | Rear right brake lamp defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-turn-rear-left` | `Switch` | Rear left turn signal defect warning (`ON` = Warning, `OFF` = OK). |
| `warnings#warning-lamp-turn-rear-right` | `Switch` | Rear right turn signal defect warning (`ON` = Warning, `OFF` = OK). |

### Group `freezer` (Freezer Compartment)

| Channel ID | Item Type | Description |
| :--- | :--- | :--- |
| `freezer#freezer-active` | `Switch` | Indicates whether the built-in refrigeration unit is running (`ON`/`OFF`). |
| `freezer#freezer-temperature` | `Number:Temperature` | Measured interior temperature of the freezer compartment (°C). |

## Full Example

### Thing Configuration

```java
Bridge pleos:account:myaccount "Pleos Account" [
    clientId="YOUR_CLIENT_ID",
    clientSecret="YOUR_CLIENT_SECRET",
    brand="HYUNDAI",
    refreshInterval=15,
    secretCreationDate="2026-11-15"
] {
    Thing vehicle ioniq5 "Hyundai IONIQ 5" [
        vin="KMHKN81AFNU000000"
    ]
}
```

### Item Configuration

```java
Group gIoniq5 "Hyundai IONIQ 5" <car> ["Vehicle"]

// Account Monitoring Items
DateTime Pleos_SecretExpires "Client Secret Expires [%1\$td.%1\(tm.\%1\)tY %1\(tH:\%1\)tM]" <time> { channel="pleos:account:myaccount:secret-expires" }
Number:Time Pleos_SecretRemaining "Remaining Key Validity [%.0f d]" <time> { channel="pleos:account:myaccount:secret-remaining-days" }
Switch Pleos_SecretWarning "Key Expiry Warning" <alarm> { channel="pleos:account:myaccount:secret-expiry-warning" }

// Battery & Charging
Number:Dimensionless Ioniq5_SoC "Battery Level [%.0f %%]" <battery> (gIoniq5) ["Measurement", "Level"] { channel="pleos:vehicle:myaccount:ioniq5:charging#soc" }
Switch Ioniq5_Charging "Charging Active" <battery> (gIoniq5) ["Status", "Level"] { channel="pleos:vehicle:myaccount:ioniq5:charging#charging" }
String Ioniq5_ChargeStatus "Charging Mode [%s]" <battery> (gIoniq5) ["Status", "Info"] { channel="pleos:vehicle:myaccount:ioniq5:charging#charge-status" }
Number:Time Ioniq5_RemainTime "Remaining Charge Time [%.0f min]" <time> (gIoniq5) ["Forecast", "Duration"] { channel="pleos:vehicle:myaccount:ioniq5:charging#remain-time" }

// Range & Location
Number:Length Ioniq5_Range "Estimated Range [%.0f km]" <motion> (gIoniq5) ["Forecast"] { channel="pleos:vehicle:myaccount:ioniq5:vehicle#distance-to-empty" }
Number:Length Ioniq5_Odometer "Odometer [%.1f km]" <line> (gIoniq5) ["Measurement"] { channel="pleos:vehicle:myaccount:ioniq5:vehicle#odometer" }
Location Ioniq5_Location "Vehicle Location" <map> (gIoniq5) ["Measurement", "GeoLocation"] { channel="pleos:vehicle:myaccount:ioniq5:vehicle#location" }

// Doors & Climate
String Ioniq5_DoorFL "Front Left Door [%s]" <door> (gIoniq5) ["Status", "LockState"] { channel="pleos:vehicle:myaccount:ioniq5:doors#door-front-left" }
String Ioniq5_Trunk "Trunk [%s]" <door> (gIoniq5) ["Status", "LockState"] { channel="pleos:vehicle:myaccount:ioniq5:doors#trunk" }
Switch Ioniq5_Climate "Climate Control" <climate> (gIoniq5) ["Status", "Heating"] { channel="pleos:vehicle:myaccount:ioniq5:climate#climate-status" }
Number:Temperature Ioniq5_TargetTemp "Target Temperature [%.1f °C]" <temperature> (gIoniq5) ["Status", "Temperature"] { channel="pleos:vehicle:myaccount:ioniq5:climate#climate-target-temp" }

// Tire Pressure
Number:Pressure Ioniq5_TireFL "Tire Pressure Front Left [%.2f bar]" <pressure> (gIoniq5) ["Measurement", "Pressure"] { channel="pleos:vehicle:myaccount:ioniq5:tires#tire-pressure-front-left" }
```

### Rules Configuration

```java
rule "Pleos API Client Secret Expiration Alert"
when
    Channel "pleos:account:myaccount:secret-expiry-event" triggered
then
    val action = getActions("mail", "mail:smtp:mysmtp") // Or use sendNotification() for openHAB Cloud
    switch (receivedEvent) {
        case "EXPIRING_7_DAYS": {
            logWarn("Pleos", "Pleos Client Secret expires in less than 7 days!")
            // action.sendMail("user@example.com", "Pleos API Warning", "Your Pleos Client Secret expires in less than 7 days.")
        }
        case "EXPIRING_2_DAYS": {
            logWarn("Pleos", "Pleos Client Secret expires in less than 2 days!")
            // action.sendMail("user@example.com", "Pleos API Urgent", "Your Pleos Client Secret expires in less than 2 days! Please renew it in the Pleos Playground.")
        }
        case "EXPIRED": {
            logError("Pleos", "Pleos Client Secret has expired!")
            // action.sendMail("user@example.com", "Pleos API Expired", "Your Pleos Client Secret has expired. Data polling will fail until renewed.")
        }
    }
end
```

### Sitemap Configuration

```perl
sitemap pleos label="Hyundai IONIQ 5" {
    Frame label="Battery & Range" {
        Text item=Ioniq5_SoC
        Text item=Ioniq5_Charging
        Text item=Ioniq5_ChargeStatus
        Text item=Ioniq5_RemainTime
        Text item=Ioniq5_Range
        Text item=Ioniq5_Odometer
    }
    Frame label="Status & Climate" {
        Text item=Ioniq5_DoorFL
        Text item=Ioniq5_Trunk
        Text item=Ioniq5_Climate
        Text item=Ioniq5_TargetTemp
        Text item=Ioniq5_TireFL
    }
    Frame label="Position" {
        Mapview item=Ioniq5_Location height=8
    }
}
```

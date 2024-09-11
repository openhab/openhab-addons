# SunSynk Binding

This binding integrates the [Sun Synk Connect web services](https://www.sunsynk.net/).
This binding is used to connect your openHAB system with Sun Synk Connect (where you log in and find Your Inverters).
From the binding, you will get status of your inverters and also command channels where you can control them.
Since the binding uses a polling mechanism, there may be some latency depending on your setting regarding refresh time.

## Introduction

You will require to have installed a Sun Synk inverter with a WiFi [Data logger](https://www.sunsynk.org/remote-monitoring) (or [e-linter](https://www.e-linter.com/)) connected to the Sun Synk App or Connect.
See [Data Logger set up](https://www.sunsynk.org/_files/ugd/39fbfb_a325b6884e684c4ba1a3ad80afd5da20.pdf) or [Sun Synk Web](https://www.sunsynk.org/remote-monitoring).
It is recommended, but not necessary that the "data interval" of your Gateway is set via Sun Synk Connect to 60s for best latency.
If you do not have that setting available you can request it set via Sun Synk, your installer or you can ask for an [User Level Access Change Request](https://www.sunsynk.org/remote-monitoring)

This binding uses your Sun Synk Connect credentials to access Sun Synk's web services via an openHAB Bridge (SunSynk Account).
The bridge manages the account authentication and the discovery of SunSynk Inverter Things. Only the Inverter Thing is currently supported.

Acknowledgements:

- [Power Forum](https://powerforum.co.za/topic/12604-sunsynk-wifi-dongle-hacking/page/3/)
- [AsTheSeaRises](https://github.com/AsTheSeaRises/SunSynk_API)
- [jamesridgway](https://github.com/jamesridgway/sunsynk-api-client/tree/main)
- [kellerza](https://github.com/kellerza/sunsynk)

## Supported Things

|Name            | Thing type    |  Thing Id  |
|----------------|---------------|------------|
|SunSynk Account | Bridge Thing  | account    |
|SunSynk Inverter| Thing         | inverter   |

## Discovery

The binding supports discovery via configuring your login and password in an openHAB bridge.

1. Add the sunsynk binding.
1. Add a new thing of type SunSynk Account via the SunSynk Binding and configure with username and password.
1. Go to Inbox press \[+\] and via the SunSynk Account start discovery \[Scan\] of inverters.
1. Inverters should appear in your inbox!

The SunSynk Account bridge thing will discover connected inverters through the UI Scan service.
When using the UI Scan service all the parameters for an Inverter Thing are discovered.

- Inverter Serial maps to the Sun Synk Connect inverter serial number
- Inverter Name maps to the Sun Synk Connect inverter alias
- Refresh time (advanced) default 60s; determines the interval between polls of Sun Synk Connect. A value above 60 is enforced. When setting this remember your inverter values are only published by Sun Synk Connect at the rate set by "data interval".

The refresh rate is limited to once every 60s to prevent too many requests from the Sun Synk Connect API, although there is no rate limit, the Sun Synk data is fully refreshed at the "data interval" set in Sun Synk Connect, at best that is every 60s.
This can mean the data in openHAB is more than 1 minute delayed from real-time.
Commands sent (from openHAB) to Sun Synk are buffered up until the next refresh interval and as they take a while to propagate through to your inverter, some channels are not refreshed (read back) from Sun Synk Connect until the next minute.

The SunSynk Account requires the user e-mail address and password used to login to Sun Synk Connect.

## Thing Configuration

### `sunsynk:account` Bridge Thing Configuration

| Name            | Type    | Description                                     | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------|---------|----------|----------|
| email           | text    | Email address used to login Sun Synk Connect    | N/A     | yes      | no       |
| password        | text    | Password to access the Sun Synk Connect account | N/A     | yes      | no       |

### `sunsynk:inverter:` Thing Configuration

| Name         | Type    | Description                                 | Default | Required | Advanced |
|--------------|---------|---------------------------------------------|---------|----------|----------|
| alias        | text    | The Sun Synk Connect inverter alias         | N/A     | yes      | no       |
| serialnumber | text    | The Sun Synk Connect inverter serial number | N/A     | yes      | no       |
| refresh      | integer | Interval the device is polled in sec        | 60      | yes      | yes      |

## Channels

The SunSynk Account has no channels.
The SunSynk Inverter has the following channels.

| Channel                        | Type                    | R/W | Description                       | Advanced |
|--------------------------------|-------------------------|-----|-----------------------------------|----------|
|battery-soc                     |Number:Dimensionless     | R   | Inverter battery % charge         | no       |
|battery-grid-voltage            |Number:ElectricPotential | R   | Battery dc electric-voltage       | no       |
|battery-grid-current            |Number:ElectricCurrent   | R   | Battery dc electric-current       | no       |
|battery-grid-power              |Number:Power             | R   | Battery dc electric-power         | no       |
|battery-temperature             |Number:Temperature       | R   | Battery temperature               | no       |
|inverter-ac-temperature         |Number:Temperature       | R   | Inverter ac temperature           | no       |
|inverter-dc-temperature         |Number:Temperature       | R   | Inverter dc temperature           | no       |
|inverter-grid-power             |Number:Power             | R   | Inverter ac electric-power        | no       |
|inverter-grid-voltage           |Number:ElectricPotential | R   | Inverter ac electric-voltage      | no       |
|inverter-grid-current           |Number:ElectricCurrent   | R   | Inverter ac electric-current      | no       |
|inverter-solar-energy-today     |Number:Energy            | R   | Solar dc energy generated today   | no       |
|inverter-solar-energy-total     |Number:Energy            | R   | Solar dc energy generated to date | no       |
|inverter-solar-power-now        |Number:Power             | R   | Solar dc electric-current         | no       |
|interval-1-grid-charge          |Switch                   | R/W | Interval 1 grid charge on/off     | yes      |
|interval-1-grid-time            |DateTime                 | R/W | Interval 1 start grid charge time | yes      |
|interval-1-grid-capacity        |Number:Dimensionless     | R/W | Interval 1 battery charge target  | yes      |
|interval-1-grid-power-limit     |Number:Power             | R/W | Interval 1 charge power limit     | yes      |
|interval-2-grid-charge          |Switch                   | R/W | Interval 2 grid charge on/off     | yes      |
|interval-2-grid-time            |DateTime                 | R/W | Interval 2 start grid charge time | yes      |
|interval-2-grid-capacity        |Number:Dimensionless     | R/W | Interval 2 battery charge target  | yes      |
|interval-2-grid-power-limit     |Number:Power             | R/W | Interval 2 charge power limit     | yes      |
|interval-3-grid-charge          |Switch                   | R/W | Interval 3 grid charge on/off     | yes      |
|interval-3-grid-time            |DateTime                 | R/W | Interval 3 start grid charge time | yes      |
|interval-3-grid-capacity        |Number:Dimensionless     | R/W | Interval 3 battery charge target  | yes      |
|interval-3-grid-power-limit     |Number:Power             | R/W | Interval 3 charge power limit     | yes      |
|interval-4-grid-charge          |Switch                   | R/W | Interval 4 grid charge on/off     | yes      |
|interval-4-grid-time            |DateTime                 | R/W | Interval 4 start grid charge time | yes      |
|interval-4-grid-capacity        |Number:Dimensionless     | R/W | Interval 4 battery charge target  | yes      |
|interval-4-grid-power-limit     |Number:Power             | R/W | Interval 4 charge power limit     | yes      |
|interval-5-grid-charge          |Switch                   | R/W | Interval 5 grid charge on/off     | yes      |
|interval-5-grid-time            |DateTime                 | R/W | Interval 5 start grid charge time | yes      |
|interval-5-grid-capacity        |Number:Dimensionless     | R/W | Interval 5 battery charge target  | yes      |
|interval-5-grid-power-limit     |Number:Power             | R/W | Interval 5 charge power limit     | yes      |
|interval-6-grid-charge          |Switch                   | R/W | Interval 6 grid charge on/off     | yes      |
|interval-6-grid-time            |DateTime                 | R/W | Interval 6 start grid charge time | yes      |
|interval-6-grid-capacity        |Number:Dimensionless     | R/W | Interval 6 battery charge target  | yes      |
|interval-6-grid-power-limit     |Number:Power             | R/W | Interval 6 charge power limit     | yes      |
|interval-1-gen-charge           |Switch                   | R/W | Interval 1 generator charge on/of | yes      |
|interval-2-gen-charge           |Switch                   | R/W | Interval 2 generator charge on/of | yes      |
|interval-3-gen-charge           |Switch                   | R/W | Interval 3 generator charge on/of | yes      |
|interval-4-gen-charge           |Switch                   | R/W | Interval 4 generator charge on/of | yes      |
|interval-5-gen-charge           |Switch                   | R/W | Interval 5 generator charge on/of | yes      |
|interval-6-gen-charge           |Switch                   | R/W | Interval 6 generator charge on/of | yes      |
|inverter-control-timer          |Switch                   | R/W | Inverter control timer on/off     | yes      |
|inverter-control-work-mode      |String                   | R/W | Inverter work mode 1, 2 or 3      | yes      |
|inverter-control-energy-pattern |String                   | R/W | Inverter energy pattern 1 or 2    | yes      |

### Full Example

#### sunsynk.things

```java
Bridge sunsynk:account:xxx @ "Loft" [email= "user.symbol@domain.", password="somepassword"]{
    Thing inverter E1234567R1231234567890 @ "Loft" [alias= "My Inverter", serialnumber= "1234567890", refresh= 60]
}
```

#### sunsynk.items

```java
Switch                      Interval1GridCharge         "Switch on Grid Charge for Interval 1"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-1-grid-charge"}
Switch                      Interval2GridCharge         "Switch on Grid Charge for Interval 2"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-2-grid-charge"}
Switch                      Interval3GridCharge         "Switch on Grid Charge for Interval 3"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-3-grid-charge"}
Switch                      Interval4GridCharge         "Switch on Grid Charge for Interval 4"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-4-grid-charge"}
Switch                      Interval5GridCharge         "Switch on Grid Charge for Interval 5"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-5-grid-charge"}
Switch                      Interval6GridCharge         "Switch on Grid Charge for Interval 6"         {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-6-grid-charge"}

Switch                      Interval1GenCharge          "Switch on Generator Charge for Interval 1"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-1-gen-charge"}
Switch                      Interval2GenCharge          "Switch on Generator Charge for Interval 2"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-2-gen-charge"}
Switch                      Interval3GenCharge          "Switch on Generator Charge for Interval 3"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-3-gen-charge"}
Switch                      Interval4GenCharge          "Switch on Generator Charge for Interval 4"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-4-gen-charge"}
Switch                      Interval5GenCharge          "Switch on Generator Charge for Interval 5"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-5-gen-charge"}
Switch                      Interval6GenCharge          "Switch on Generator Charge for Interval 6"    {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-6-gen-charge"}

DateTime                    Interval1GridTime           "Time for Interval 1"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-1-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}
DateTime                    Interval2GridTime           "Time for Interval 2"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-2-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}
DateTime                    Interval3GridTime           "Time for Interval 3"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-3-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}
DateTime                    Interval4GridTime           "Time for Interval 4"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-4-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}
DateTime                    Interval5GridTime           "Time for Interval 5"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-5-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}
DateTime                    Interval6GridTime           "Time for Interval 6"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-6-grid-time", widget="widget:rlk_datetime_standalone"[label="Time Picker"]}

Number:Dimensionless        Interval1GridCapacity       "Charge Target Interval 1"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-1-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}
Number:Dimensionless        Interval2GridCapacity       "Charge Target Interval 2"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-2-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}
Number:Dimensionless        Interval3GridCapacity       "Charge Target Interval 3"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-3-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}
Number:Dimensionless        Interval4GridCapacity       "Charge Target Interval 4"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-4-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}
Number:Dimensionless        Interval5GridCapacity       "Charge Target Interval 5"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-5-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}
Number:Dimensionless        Interval6GridCapacity       "Charge Target Interval 6"                     {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-6-grid-capacity", widget="oh-slider-card",listWidget="oh-slider-item"[title="Target SOC",subtitle="Set % SOC"]}

Number:Power                Interval1GridPowerLimit     "Max Charge Power Interval 1"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-1-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}
Number:Power                Interval2GridPowerLimit     "Max Charge Power Interval 2"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-2-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}
Number:Power                Interval3GridPowerLimit     "Max Charge Power Interval 3"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-3-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}
Number:Power                Interval4GridPowerLimit     "Max Charge Power Interval 4"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-4-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}
Number:Power                Interval5GridPowerLimit     "Max Charge Power Interval 5"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-5-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}
Number:Power                Interval6GridPowerLimit     "Max Charge Power Interval 6"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:interval-6-grid-power-limit", listWidget="oh-slider-item"[title="Target Power Limit",subtitle="Set Limit in Watts", min=0, max=8000,step=1000]}

Number:Dimensionless        BatterySOC                  "Battery SOC [%s]"                             {channel ="sunsynk:inverter:xxx:1234567R1231234567890:battery-soc"}
Number:ElectricPotential    BatteryGridVoltage          "Battery Grid Voltage"                         {channel="sunsynk:inverter:xxx:1234567R1231234567890:battery-grid-voltage"}
Number:ElectricCurrent      BatteryGridCurrent          "Battery Grid Current"                         {channel="sunsynk:inverter:xxx:1234567R1231234567890:battery-grid-current"}
Number:Power                BatteryGridPdower           "Battery Grid Power"                           {channel="sunsynk:inverter:xxx:1234567R1231234567890:battery-grid-power"}
Number:Temperature          BatteryTemperature          "Battery Temperatue "                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:battery-temperature"}
Number:Temperature          InverterACTemperature       "Inverter AC Temperature"                      {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-ac-temperature"}
Number:Temperature          InverterDCTemperature       "Inverter DC Temperature"                      {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-dc-temperature"}
Number:Power                InverterGridPower           "Inverter Grid Power"                          {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-grid-power"}
Number:ElectricPotential    InverterGridVoltage         "Inverter Grid Voltage"                        {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-grid-voltage"}
Number:ElectricCurrent      InverterGridCurrent         "Inverter Grid Current"                        {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-grid-current"}
Number:Energy               InverterSolarEnergyToday    "Inverter Energy Today"                        {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-solar-energy-today"}
Number:Energy               InverterSolarEnergyTotal    "Inverter Enery Gross"                         {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-solar-energy-total"}
Number:Power                InverterSolarPowerNow       "Inverter Power"                               {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-solar-power-now"}

Switch                      Interval6ControlTimer       "Switch on System Mode Timer"                  {channel="sunsynk:inverter:xxx:1234567R1231234567890:inverter-control-timer"}
String                      InverterControlWorkMode     "System Work Mode 0, 1 or 2"                   {channel="sunsynk:inverter:a1a6340bc0:E4701229R3312211229948:inverter-control-work-mode"}
String                      InverterControlPattern      "System Mode Energy Pattern 0 or 1"            {channel="sunsynk:inverter:a1a6340bc0:E4701229R3312211229948:inverter-control-energy-pattern"}
```

## DateTime Widget

The items file above adds Metadata: Default Standalone widget: [rlk_datetime_standalone](https://community.openhab.org/t/datetime-standalone-widget/127966) to the DateTime items. Only the time portion of the DateTime item is important.

Be sure to understand the time zone set up for the inverter, this can either be synchronised with Sun Synk servers, which in the UK at least applies daylight saving, or free-wheeling locally.
The times set in the DateTime items using the widget are not adjusted to any time zone and are sent to the SunSynk API as Strings where they will be applied directly to your inverter.
This is in contrast to other solar / energy APIs that use Zulu (GMT) time.

## Debugging

After installation, to gain further information on any issues you encounter you can turn on Debug [Logging](https://www.openhab.org/docs/administration/logging.html) either through the [karaf console](https://www.openhab.org/docs/administration/console.html) or through the openHAB UI.

# SEMSPortal Binding

This binding can help you include statistics of your SEMS / GoodWe solar panel installation into openHAB.
It is a read-only connection that maps collected parameters to openHAB channels.
It provides current, day, month and total yields, as well as some income statistics if you have configured these in the SEMS portal.
It requires a power station that is connected through the internet to the SEMS portal.

## Supported Things

This binding provides two Thing types: a bridge to the SEMS Portal, and the Power Stations which are found at the Portal.
The Portal (``semsportal:portal``) represents your account in the SEMS portal.
The Power Station (``semsportal:station``) is an installation of a Power Station or inverter that reports to the SEMS portal and is available to your account.

## Discovery

Once you have configured a Portal Bridge, the binding will discover all Power Stations that are available to this account.
You can trigger discovery in the add new Thing section of openHAB.
Select the SEMS binding and press the Scan button.
The discovered Power Stations will appear as new Things.

## Thing Configuration

The configuration of the Portal Thing (Bridge) is pretty straight forward.
You need to have your power station set up in the SEMS portal, and you need to have an account that is allowed to view the power station data.
You should log in at least once in the portal with this account to activate it.
The Portal needs the username and password to connect and retrieve the data.
You can configure the update frequency between 1 and 60 minutes.
The default is 5 minutes.

Power Stations have no settings and will be auto discovered when you add a Portal Bridge.

If you prefer manual configuration of things in thing files, you need to supply the power station UUID.
It can be found in the SEMS portal URL after you have logged in.
The URL will look like this:

```text
https://www.semsportal.com/PowerStation/PowerStatusSnMin/xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

Where the part after the last / character is the UUID to be used.

Example portal configuration with a station:

```java
Bridge semsportal:portal:myPortal [ username="my@username.com", password="MyPassword" ] {
    station solarPanels "Solar Panels" [ stationUUID="xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
}
```

## Channels

The Portal(Bridge) has no channels.
The Power Station Thing has the following channels:

| channel       | type          | description                                                                                                |
| ------------- | ------------- | ---------------------------------------------------------------------------------------------------------- |
| lastUpdate    | DateTime      | Last time the powerStation sent information to the portal                                                  |
| currentOutput | Number:Power  | The current output of the powerStation in Watt                                                             |
| todayTotal    | Number:Energy | Todays total generation of the station in kWh                                                              |
| monthTotal    | Number:Energy | This month's total generation of the station in kWh                                                        |
| overallTotal  | Number:Energy | The total generation of the station since installation, in kWh                                             |
| todayIncome   | Number        | Todays income as reported by the portal, if you have configured the power rates of your energy provider    |
| totalIncome   | Number        | The total income as reported by the portal, if you have configured the power rates of your energy provider |

## Parameters

The Power Station Thing has no configuration parameters when auto discovered.
When using thing files you need to provide the station UUID.

| Parameter   | Required? | Description                                                                      |
| ----------- | :-------: | -------------------------------------------------------------------------------- |
| stationUUID |     X     | UUID of the station. Can be found on the SEMS portal URL (see description above) |

The Bridge has the following configuration parameters:

| Parameter | Required? | Description                                                                                           |
| --------- | :-------: | ----------------------------------------------------------------------------------------------------- |
| username  |     X     | Account name (email address) at the SEMS portal. Account must have been used at least once to log in. |
| password  |     X     | Password of the SEMS portal                                                                           |
| interval  |           | Number of minutes between two updates. Between 1 and 60 minutes, defaults to 5 minutes                |

## Credits

This binding has been created using the information provided by RogerG007 in this forum topic: <https://community.openhab.org/t/connecting-goodwe-solar-panel-inverter-to-openhab/85480>

# Linky Binding

This binding enables the exploitation of electricity consumption data, mainly for the French market. 
It supports different functionalities:

- Connection to Enedis to retrieve consumption data online.
- Direct connection to an electricity meter, such as Linky Meter, to access real-time data.
- Connection to the data.gouv.fr API to obtain regulated electricity prices.
- Connection to the RTE API to get Tempo Red/White/Blue calendar information.


## Migration

The new binding will need some tweak to you configuration to work.
Mainly the new binding use Bridge to access Enedis data, so you will have to add this bridge to your configuration.
Step are:

1. add a bridge definition
	
	Bridge linky:enedis:local "EnedisWebBridge" [ 
		username="laurent@clae.net", 
		password="Mnbo32tyu123!", 
		internalAuthId="eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0.u_mxXO7_d4I5bLvJzGtc2MARvpkYv0iM0EsO6a24k-tW9493_Myxwg.LVlfephhGTiCBxii8bRIkA.GOf9Ea8PTGshvkfjl62b6w.hSH97IkmBcEAz2udU-FqQg" 
		]  
	{ 
	}  

2. Move username, password & internalAuthId configuration parameter from the old linky thing to the bridge thing.

3. add the bridge reference to the thing

	Thing linky:linky:linkremotemelody "Linky Melody" (linky:enedis:local)

4. Possibly restart openhab instance to take change in effect

5. Possibly start to use the new channel add by the new binding.
   Old channel will work out of the box without any config modification as we keep the same group name / channel name.

## Getting Consumption Data Online

The new binding version can use multiple bridges to access consumption data.
You can use :

- The enedis bridge: Uses the old Enedis API, based on the Enedis website, to gather data.
- The myelectricaldata bridge: Uses the new REST Enedis API via the MyElectricalData proxy site to access the data.
- TThe enedis-api bridge: Also uses the new REST Enedis API, but gathers data directly from the Enedis site.

You first need to create an Enedis account [here](https://mon-compte-client.enedis.fr/)  if you don't already have one. 
Ensure that you have accepted their conditions and check that you can see graphs on the website, especially the hourly view.
 Enedis may require your permission the first time to start collecting hourly data.

The binding will not provide this information unless this step is completed.

Advantage and Disadvantage of Each Method.

- Enedis bridge is the older method.
- MyelectricalData and enedis bridges both use the new API format, making them less prone to changes in website architecture.
- MyelectricalData bridge is managed by a third-party provider but is stable.
- Enedis-api bridge directly connects to Enedis but currently requires a complex registration process with Enedis. 
  This limitation will likely be resolved in the near future, making Enedis-api Bridge the preferred method.

### Bridge Configuration

To retrieve data, the Linky device needs to be linked to a LinkyBridge. The available bridge options are enedis, myelectricaldata, and enedis-api.

#### Enedis Bridge

If you select enedis bridge, you will need :

- To create an Enedis account : https://mon-compte-client.enedis.fr/
- To provide your credentials: username, password, and InternalAuthId.

      | Parameter      | Description                                |
      |----------------|--------------------------------------------|
      | username       | Your Enedis platform username.             |
      | password       | Your Enedis platform password.             |
      | internalAuthId | The internal authentication ID.            |
	  | timezone       | The timezone at the location of your linky |

    This version is compatible with the latest Enedis Web API (deployed from June 2020). To bypass the captcha login, log in via a standard browser (e.g., Chrome, Firefox) and retrieve the user cookies (internalAuthId).

    Instructions for Firefox :

    1. Go to <https://mon-compte-client.enedis.fr/>.
    2. Select "Particulier" from the drop down and click "Connexion".
    3. Enter your Enedis account email and check "Je ne suis pas un robot".
    4. Click "Suivant".
    5. Enter your Enedis password and click "Connexion à Espace Client Enedis".
    6. Navigate to your Enedis account environment, then return to the previous page in your browser.
    7. Log out from your Enedis account.
    8. Repeat steps 1-2. This time, open the developer tools window (F12) and select the "Storage" tab.
    9. Under "Cookies", select "https://mon-compte-client.enedis.fr/". Locate the "internalAuthId" entry and copy its value into your OpenHAB configuration.
	
	A new timezone parameter has been introduced. If you don't put a value, it will default to the timezone of your openHAB installation. 
	This parameter can be useful if you read data from a Linky in a different timezone.

    ```java
    Bridge linky:enedis:local "EnedisWebBridge" [ username="example@domaine.fr", password="******", internalAuthId="******" ]
    ```

#### Myelectricaldata Bridge

If you select MyElectricalData bridge, you will need :

- To create an Enedis account : https://mon-compte-client.enedis.fr/

- To follow these steps to initialize the token:
  
  You can access the procedure from the connectlinky page available from your openhab: https://home.myopenhab.org/connectlinky/index.

  You will find screenshoot of the procedure in the following directory
  [doc/myelectricaldata/](doc/myelectricaldata/index.md)

  1. Go to the connectlinky page on OpenHAB.
  2. Follow the first two steps of the wizard and click "Access Enedis".
  3. Log into your Enedis account.
  4. Authorize data collection for your PRM ID.<br/>
     If you have multiple Linky meters, repeat the procedure for each one separately; selecting multiple meters at once will not work.


  5. You will then be redirect to a confirmation page on MyElectricalData web site
  6. Return to OpenHAB, go to "connectlinky/myelectricaldata-step3", select your PRM ID from the dropdown, and click "Retrieve Token".
  7. A confirmation page will appear if everything is correctly set up.

    ```java
    Bridge linky:my-electrical-data:local "MyElectricalBridge" [  ]
    ```

#### Enedis Bridge
If you select enedis bridge, you will need :

- To create an Enedis account : https://mon-compte-client.enedis.fr/

- Follow these steps to initialize the token. 

  You can access the procedure from the connectlinky page available from your openhab: https://home.myopenhab.org/connectlinky/index.

    You will find screenshoot of the procedure in the following directory
    [doc/enedis/](doc/enedis/index.md)


    1. Go to the connectlinky page on OpenHAB.
    2. Follow the first two steps of the wizard and click "Access Enedis".
    3. Log into your Enedis account.
    4. Authorize data collection for your PRM ID.
    5. A confirmation page will appear if everything is correctly set up.


    ```java
    Bridge linky:enedis-api:localSB "EnedisBridgeSandbox" [  clientId="myClientId...", clientSecret="myClientSecret..."	]  
    ```

### Thing Configuration  

The remote bridge works with Linky devices to retrieve consumption data from a remote API or website.

You can have multiple Linky devices in your setup if you have different houses or multiple Linky meters linked to your account. 
To do this, simply create multiple Linky devices and set the prmId to match your meter ID. 
You can find the meter ID on the Enedis website or directly on your Linky meter.

You can switch the Linky device from one bridge to another if you experience issues with a particular bridge. 
The data retrieved will be almost identical regardless of the bridge you use. 
Only a few contract-related items may differ between the web bridge and the API bridge.

The device has the following configuration parameters:

| Parameter      | Description                                                                                          |
|----------------|------------------------------------------------------------------------------------------------------|
| prmId          | The prmId linked to the Linky Handler.                                                               |
| token          | Optional: Required if a token is necessary to access this Linky device (used for MyElectricalData).  |



```java
Thing linky:linky:linkyremotexxxx "Linky Remote xxxx" (linky:enedis:local) [ prmId="xxxx" ]
Thing linky:linky:linkyremotexxxx "Linky Remote xxxx" (linky:enedis-api:local) [ prmId="xxxx" ]
Thing linky:linky:linkyremotexxxx "Linky Remote xxxx" (linky:myelectricaldata:local) [ prmId="xxxx", token="myElectricalDataToken" ]
```

### Thing Channels

The retrieved information is available in multiple groups.

- The Main group will give information about the contract linked to this linky.

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | main#identitiy                                    | info              | The full name of the contract older                                           |
  | main#contract-subscribed-power                    | info              | The subscribed max Power                                                      |
  | main#contract-last-activationdate                 | info              | The contract activation date                                                  |
  | main#contract-distribution-tariff                 | info              | The current applied tarif                                                     |
  | main#contract-offpeak-hours                       | info              | The OffPeakHour link to your contract                                         |
  | main#contract-status                              | info              | The current contract status                                                   |
  | main#contract-type                                | info              | The contract type                                                             |
  | main#contract-lastdistribution-tariff-changedate  | info              | The date of the last tariff change                                            |
  | main#contract-segment                             | info              | The customer segment for this contract                                        |
  | main#usage-point-id                               | info              | The distribution / usage point uniq indentifier                               |
  | main#usage-point-status                           | info              | The usage point current state                                                 |
  | main#usage-point-meter-type                       | info              | The usage point meter type                                                    |
  | main#usage-point-address-city                     | info              | The usage point City                                                          |
  | main#usage-point-address-country                  | info              | The usage point Country                                                       |
  | main#usage-point-address-insee-code               | info              | The usage point Insee Code                                                    |
  | main#usage-point-address-postal-code              | info              | The usage point Postal Code                                                   |
  | main#usage-point-address-street                   | info              | The usage point Address Street                                                |
  | main#contact-mail                                 | info              | The usage point Contact Mail                                                  |
  | main#contact-phone                                | info              | The usage point Contact Phone                                                 |

- The daily group will give consumtion information with day granularity

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | daily#yesterday                                   | consumption       | Yesterday energy usage                                                        |
  | daily#day-2                                       | consumption       | Day-2 energy usage                                                            |
  | daily#day-3                                       | consumption       | Day-3 energy usage                                                            |
  | daily#consumption                                 | consumption       | timeseries for energy usage  (up to three years will be store if available)   |
  | daily#maw-power                                   | power             | timeseries for max-power usage                                                |
  | daily#power                                       | power             | Yesterday's peak power usage                                                  |
  | daily#timestamp                                   | timestamp         | Timestamp of the power peak                                                   |
  | daily#power-2                                     | power             | Day-2's peak power usage                                                      |
  | daily#timestamp-2                                 | timestamp         | Timestamp Day-2's of the power peak                                           |
  | daily#power-3                                     | power             | Day-3's peak power usage                                                      |
  | daily#timestamp-3                                 | timestamp         | Timestamp Day-3's  of the power peak                                          |

- The weekly group will give consumtion information with week granularity

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | weekly#thisWeek                                   | consumption       | Current week energy usage                                                     |
  | weekly#lastWeek                                   | consumption       | Last week energy usage                                                        |
  | weekly#week-2                                     | consumption       | Week -2 energy usage                                                          |
  | weekly#consumption                                | consumption       | timeseries for weeks energy usage                                             |
  | weekly#max-power                                  | power             | timeseries for max-power weekly usage                                         |

- The monthly group will give consumtion information with month granularity

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | monthly#thisMonth                                 | consumption       | Current month energy usage                                                    |
  | monthly#lastMonth                                 | consumption       | Last month energy usage                                                       |
  | monthly#month-2                                   | consumption       | Month-2 energy usage                                                          |
  | monthly#consumption                               | consumption       | timeseries for months energy usage                                            |
  | monthly#max-power                                 | power             | timeseries for max-power monthly usage                                        |

- The yearly group will give consumtion information with year granularity

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | yearly#thisYear                                   | consumption       | Current year energy usage                                                     |
  | yearly#lastYear                                   | consumption       | Last year energy usage                                                        |
  | yearly#year-2                                     | consumption       | year-2 energy usage                                                           |
  | yearly#consumption                                | consumption       | timeseries for years energy usage                                             |
  | yearly#maxPower                                   | power             | timeseries for max-power yearly usage                                         |
 
- The load-curve group will give you access to load curve data with granularity as low as 30mn

  | Channel ID                                        | Item Type         | Description                                                                   |
  |---------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | load-curve#power                                  | power             | The load curve data                                                           |

### Full Example

#### Remote Enedis Web Connection

```java
Bridge linky:enedis:local "EnedisWebBridge" [ username="example@domaine.fr", password="******", internalAuthId="******" ]

Thing linky:linky:linkyremotexxxx "Linky Remote xxxx" (linky:enedis:local) [ prmId="xxxx" ]
```

```java
Number:Energy ConsoHier "Conso hier [%.0f %unit%]" <energy> { channel="linky:enedis:local:daily#yesterday" }
Number:Energy ConsoSemaineEnCours "Conso cette semaine [%.0f %unit%]" <energy> { channel="linky:enedis:local:weekly#thisWeek" }
Number:Energy ConsoSemaineDerniere "Conso semaine dernière [%.0f %unit%]" <energy> { channel="linky:enedis:local:weekly#lastWeek" }
Number:Energy ConsoMoisEnCours "Conso ce mois [%.0f %unit%]" <energy> { channel="linky:enedis:local:monthly#thisMonth" }
Number:Energy ConsoMoisDernier "Conso mois dernier [%.0f %unit%]" <energy> { channel="linky:enedis:local:monthly#lastMonth" }
Number:Energy ConsoAnneeEnCours "Conso cette année [%.0f %unit%]" <energy> { channel="linky:enedis:local:yearly#thisYear" }
Number:Energy ConsoAnneeDerniere "Conso année dernière [%.0f %unit%]" <energy> { channel="linky:enedis:local:yearly#lastYear" }
```

### Displaying Information Graph

Using the timeseries channel, you will be able to easily create a calendar graph to display the Tempo calendar.
To do this, you need to enable a timeseries persistence framework.
Graph definitions will look like this:

![TempoGraph](doc/GraphConso.png)

Sample code : 

```java
config:
  future: false
  label: Linky Melody Conso Journalière
  order: "110"
  period: 2W
  sidebar: true
slots:
  dataZoom:
    - component: oh-chart-datazoom
      config:
        type: inside
  grid:
    - component: oh-chart-grid
      config:
        containLabel: true
        includeLabels: true
        show: true
  legend:
    - component: oh-chart-legend
      config:
        bottom: 3
        type: scroll
  series:
    - component: oh-time-series
      config:
        areaStyle:
          opacity: 0.2
        gridIndex: 0
        item: Linky_Melody_Daily_Conso_Day
        label:
          formatter: =v=>Number.parseFloat(v.data[1]).toFixed(2) + " Kwh"
          position: inside
          show: true
        markLine:
          data:
            - type: average
        markPoint:
          data:
            - name: min
              type: min
            - name: max
              type: max
          label:
            backgroundColor: auto
        name: Consumption
        noBoundary: true
        noItemState: true
        service: influxdb
        type: bar
        xAxisIndex: 0
        yAxisIndex: 0
  tooltip:
    - component: oh-chart-tooltip
      config:
        confine: true
        smartFormatter: true
  xAxis:
    - component: oh-time-axis
      config:
        gridIndex: 0
        nameLocation: center
        splitNumber: 10
  yAxis:
    - component: oh-value-axis
      config:
        gridIndex: 0
        max: "150"
        min: "0"
        name: kWh
        nameLocation: center
```


## Getting Consumption Data Locally

You can also retrieve your consumption data locally, directly from your meters (Linky or even older blue meters).<br/>
More information about Teleinfo protocols can be found here:  [Teleinfo protocol](https://www.enedis.fr/sites/default/files/Enedis-NOI-CPT_54E.pdf) 

To achieve this, you need to connect the Teleinfo output to your OpenHAB server.<br/>
This can be done by plugging a Teleinfo modem into the I1 and I2 terminals of your electricity meter.
There are two main ways to do this:

- Direct connection: Using a Teleinfo-to-serial modem converter.
- Remote connection: Using an ERL dongle put into your counter.

The advantage of this method is that you get real-time information from your meter:

- Direct connection typically provides data with a granularity between 2 to 5 seconds.
- Remote connection typically provides data with a granularity of around 1 minute.

In comparison, remote connections using the Enedis API have a granularity of no less than 30 minutes, and data is refreshed only once per day. This means you can only view the data the day after it is produced!

With the Teleinfo protocol, you can read various electrical statistics from your electricity meter, such as instantaneous power consumption, current price period, and meter readings. 
These values can be used to:
- Send your meter reading to your electricity provider with a simple copy/paste.
- Improve your automation rules and minimize electricity costs.
- Check if your subscription plan is suitable for your needs.
- Monitor your electricity consumption.


The Teleinfo binding supports both single-phase and three-phase connections, ICC evolution, and the following pricing modes:
- HCHP mode
- Base mode
- Tempo mode
- EJP mode

### Type of Connection

#### Direction Connection

Direct connection can be achieved in several ways.

A few examples are provided below in the "Tested Hardware" section, but many other devices can also work.
Virtually, any hardware that exposes the Teleinfo frame as a serial transport on the OpenHAB server should be compatible.

You can even place the Teleinfo modem separately from your OpenHAB server by forwarding serial messages over a network using ser2net or similar technologies.
In this case, you need to define the serial port of your bridge as `rfc2217://ip:port.`<br/>
When using _ser2net_, make sure to use _telnet_ instead of _raw_ in the _ser2net_ configuration file.

#### Remote Connection

Remote connection can use different technologies to transmit the Teleinfo frame.
I have tested it using a D2L ERL, which uses Wi-Fi technology to send the frame over a TCP/IP port.
However, other ERLs use different radio technologies, such as:

- 433 MHz transmission
- LoRa or Sigfox (long-range, low-bandwidth networks)
- KNX technology
- Zigbee technology

The binding currently supports only Wi-Fi/D2L.
Support for 433 MHz transmission may be added in the future.
KNX and Zigbee are out of scope as they have their own bindings.

### Tic Mode

There are two different TIC modes, corresponding to two distinct frame formats:

- Historical TIC mode (older version)
  - Uses a serial transmission rate of 1200 baud.

- Standard TIC mode (newer version)
  - Uses a serial transmission rate of 9600 baud.
  - Provides more information from the meter.
  - Only available on Linky meters.
  - Offers a faster refresh rate.

The method for changing the TIC mode of a Linky meter is explained [here](https://forum.gce-electronics.com/t/comment-passer-un-cpt-linky-en-mode-standard/8206/7).


### Supported Things

Currently, there are two types of supported bridges:
- Serial Bridge: For direct connection using a Teleinfo modem.
- D2L Bridge: For remote connection over Wi-Fi.

### Bridge Configuration

#### Serial Bridge

      | Parameter                      | Sample         | Description                                                       |
      |--------------------------------|----------------|-------------------------------------------------------------------|
      | serialport                     | /dev/ttyUSB1   | The serial port where you connect the Teleinfo model              |
      | ticMode                        | Standard       | Standard or Historical : must match your meter configuration.     |
      | verifyChecksum                 | true           | If we check the checksum of the Teleinfo frame : default is true  |
      | autoRepairInvalidADPSgroupLine | true           | If we try to repair corrupted frame : default is true             |

Historical TIC mode is the only mode of all telemeters before Linky models and the default mode for Linky telemeters.


```java
Bridge linky:serial:local "SerialBridge" [ 	serialport="/dev/ttyUSB1",	ticMode="STANDARD",	verifyChecksum="true",	autoRepairInvalidADPSgroupLine="true"	]  
```

#### D2L Bridge

      | Parameter                      | Sample         | Description                                                       |
      |--------------------------------|----------------|-------------------------------------------------------------------|
      | listenningPort                 | 7845           | The tcp port we will listen for Teleinfo frame coming from D2L    |


The D2L bridge will open a TCP port, listen on it, and wait for Teleinfo frames.
If you have multiple meters, you can use a single port for all of them.
The bridge will decode the ID of the D2L device sending the frame and dispatch it to the corresponding thing.


```java
Bridge linky:d2l:local "D2lBridge" [ listenningPort="7845"]
```
### Thing Configuration  

There is only one thing type: the linky-local thing.
Channels will be updated upon receiving the first frame to accommodate your setup:
- Standard or Historical mode.
- Single-phase or Three-phase configuration.
- Tarif : Base, HP/HC, EJP or Tempo.
- Mode : Consummer or Producer.
- ICC Evolution.

You need to configure a few parameters according to your setup.
- PRM ID is mandatory.
- Other parameters are only required for D2L setup.

| Parameter      | Description                                                                                 |
|----------------|---------------------------------------------------------------------------------------------|
| prmId          | The prmId link to the linky Handler.                                                        |
| id             | The ID of the D2L module                                                                    |
| appKey         | The appKey use to decrypt the D2L traffic                                                   |
| ivKey          | The ivKey use to decrypt the D2L traffic                                                    |


```java
Thing linky:linky-local:linkylocalxx "Linky Local xxx" (linky:d2l:local)	[ prmId="2145499xxx", id="02180yyyy", appKey="myAppKey",	ivKey="myIvKey" ]  
Thing linky:linky-local:linkylocalxx "Linky Local xxx" (linky:serial:local)	[ prmId="2145499xxx"]  
```



### Discovery
This binding provides a discovery service only for things.
First, configure your bridge (D2L or Serial).
After a few seconds, your Inbox should be populated with your different meters.
For D2L-connected meters, you will need to enter the appKey and ivKey for decryption.
For direct serial-connected meters, no further action is required.

### Thing Channels

#### Historical TIC mode

##### Common Channels

| Channel                  | Type                      | Description                                              | Connection   | Mode  |
|--------------------------|---------------------------|----------------------------------------------------------|--------------|-------|
| historical-base#adco     | string                    | Subscribed electric current                              | All          | All   |
| historical-base#optarif  | string                    | Subscribed electric current                              | All          | All   |
| historical-base#isousc   | power                     | Subscribed electric current                              | All          | All   |
| historical-base#base     | consumption               | Total consumed energy                                    | All          | Base  |
| historical-base#ptec     | ptecType                  | Current pricing period                                   | All          | All   |
| historical-base#iinst    | current                   | Instantaneous electric current                           | Single-phase | All   |
| historical-base#adps     | current                   | Excess electric current warning                          | Single-phase | All   |
| historical-base#imax     | current                   | Maximum consumed electric current                        | Single-phase | All   |
| historical-base#papp     | power                     | Maximum consumed electric power on all phases            | All          | All   |
| historical-base#modetat  | info                      | Etat                                                     | All          | All   |

##### 3 Phase Channels

| Channel                  | Type                      | Description                                              | Connection   | Mode  |
|--------------------------|---------------------------|----------------------------------------------------------|--------------|-------|
| historical-3phase#iinst1 | current                   | Instantaneous electric current on phase 1                | Three-phase  | All   |
| historical-3phase#iinst2 | current                   | Instantaneous electric current on phase 2                | Three-phase  | All   |
| historical-3phase#iinst3 | current                   | Instantaneous electric current on phase 3                | Three-phase  | All   |
| historical-3phase#imax1  | current                   | Maximum consumed electric current on phase 1             | Three-phase  | All   |
| historical-3phase#imax2  | current                   | Maximum consumed electric current on phase 2             | Three-phase  | All   |
| historical-3phase#imax3  | current                   | Maximum consumed electric current on phase 3             | Three-phase  | All   |
| historical-3phase#adir1  | current                   | Excess electric current on phase 1 warning               | Three-phase  | All   |
| historical-3phase#adir2  | current                   | Excess electric current on phase 2 warning               | Three-phase  | All   |
| historical-3phase#adir3  | current                   | Excess electric current on phase 3 warning               | Three-phase  | All   |
| historical-3phase#ppot   | info                      | Electrical potential presence                            | Three-phase  | All   |

##### HpHc Channels

| Channel                  | Type                      | Description                                              | Connection   | Mode  |
|--------------------------|---------------------------|----------------------------------------------------------|--------------|-------|
| historical-hphc#hhphc    | hhphcType                 | Pricing schedule group                                   | All          | HCHP  |
| historical-hphc#hchc     | consumption               | Total consumed energy at low rate pricing                | All          | HCHP  |
| historical-hphc#hchp     | consumption               | Total consumed energy at high rate pricing               | All          | HCHP  |

##### EJP Channels

| Channel                  | Type                      | Description                                              | Connection   | Mode  |
|--------------------------|---------------------------|----------------------------------------------------------|--------------|-------|
| historical-ejp#ejphn     | consumption               | Total consumed energy at low rate pricing                | All          | EJP   |
| historical-ejp#ejphpm    | consumption               | Total consumed energy at high rate pricing               | All          | EJP   |
| historical-tempo#pejp    | time      	               | Prior notice to EJP start                                | All          | EJP   |

##### Tempo Channels

| Channel                  | Type                      | Description                                              | Connection   | Mode  |
|--------------------------|---------------------------|----------------------------------------------------------|--------------|-------|
| historical-tempo#bbrhcjb | consumption               | Total consumed energy at low rate pricing on blue days   | All          | Tempo |
| historical-tempo#bbrhpjb | consumption               | Total consumed energy at high rate pricing on blue days  | All          | Tempo |
| historical-tempo#bbrhcjw | consumption               | Total consumed energy at low rate pricing on white days  | All          | Tempo |
| historical-tempo#bbrhpjw | consumption               | Total consumed energy at high rate pricing on white days | All          | Tempo |
| historical-tempo#bbrhcjr | consumption               | Total consumed energy at low rate pricing on red days    | All          | Tempo |
| historical-tempo#bbrhpjr | consumption               | Total consumed energy at high rate pricing on red days   | All          | Tempo |
| historical-tempo#demain  | tomorrow-color            | Following day color                                      | All          | Tempo |

#### Standard TIC mode

##### Common Channels

| Channel                                  | Type                      | Description                                                                       |
|------------------------------------------|---------------------------|-----------------------------------------------------------------------------------|
| commonLSMGroupType#adsc                  | string                    | Second meter address                                                              |
| commonLSMGroupType#vtic                  | string                    | Vtic version                                                                      |
| commonLSMGroupType#prm                   | string                    | The prmId                                                                         |
| commonLSMGroupType#date                  | datetime                  | Date and Time                                                                     |
| commonLSMGroupType#ngtf                  | string                    | Provider schedule name                                                            |
| commonLSMGroupType#ltarf                 | string                    | Current pricing label                                                             |
| commonLSMGroupType#stge                  | string                    | Registre d'état                                                                   |
| commonLSMGroupType#east                  | energy                    | Total active energy withdrawn                                                     |
| commonLSMGroupType#easf_XX_              | energy                    | Active energy withdrawn from provider on index XX, XX in {01,...,10}              |
| commonLSMGroupType#easd_XX_              | energy                    | Active energy withdrawn from distributor on index XX, XX in {01,...,04}           |
| commonLSMGroupType#irms1                 | current                   | RMS Current on phase 1                                                            |
| commonLSMGroupType#urms1                 | potential                 | RMS Voltage on phase 1                                                            |
| commonLSMGroupType#pref                  | power                     | Reference apparent power                                                          |
| commonLSMGroupType#pcoup                 | power                     | Apparent power rupture capacity                                                   |
| commonLSMGroupType#sinsts                | power                     | Instantaneous withdrawn apparent power                                            |
| commonLSMGroupType#smaxsn                | power                     | Maximum withdrawn apparent power of the day                                       |
| commonLSMGroupType#smaxsnDate            | datetime                  | Timestamp of SMAXSN value                                                         |
| commonLSMGroupType#smaxsnMinus1          | power                     | Maximum withdrawn apparent power of the previous day                              |
| commonLSMGroupType#smaxsnMinus1Date      | datetime                  | Timestamp of SMAXSN-1 value                                                       |
| commonLSMGroupType#ccasn                 | power                     | Active charge point N                                                             |
| commonLSMGroupType#ccasnDate             | datetime                  | Timestamp of CCASN value                                                          |
| commonLSMGroupType#ccasnMinus1           | power                     | Active charge point N-1                                                           |
| commonLSMGroupType#ccasnMinus1Date       | datetime                  | Timestamp of CCASN-1 value                                                        |
| commonLSMGroupType#umoy1                 | potential                 | Mean Voltage on phase 1                                                           |
| commonLSMGroupType#umoy1Date             | datetime                  | Timestamp of UMOY1 value                                                          |
| commonLSMGroupType#dpm_X_                | string                    | Start of mobile peak period X, X in {1,2,3}                                       |
| commonLSMGroupType#dpm_X_Date            | datetime                  | Date of DPMX, X in {1,2,3}                                                        |
| commonLSMGroupType#fpm_X_                | string                    | End of mobile peak period X, X in {1,2,3}                                         |
| commonLSMGroupType#fpm_X_Date            | datetime                  | Date of FPMX, X in {1,2,3}                                                        |
| commonLSMGroupType#msg1                  | string                    | Short message                                                                     |
| commonLSMGroupType#msg2                  | string                    | Very short message                                                                |
| commonLSMGroupType#ntarf                 | string                    | Index of current pricing                                                          |
| commonLSMGroupType#njourf                | string                    | Number of current provider schedule                                               |
| commonLSMGroupType#njourfPlus1           | string                    | Number of next day provider schedule                                              |
| commonLSMGroupType#pjourfPlus1           | string                    | Profile of next day provider schedule                                             |
| commonLSMGroupType#ppointe               | string                    | Profile of next rush day                                                          |
| commonLSMGroupType#relaisX               | switch-rtpe               | state of relais X, X in {1,...,8}                                                 |


##### Three Phase only Channels

| Channel                                 | Type                      | Description                                                                       |
|-----------------------------------------|---------------------------|-----------------------------------------------------------------------------------|
| threePhasedLSMGroup#irmsX               | current                   | RMS Current on phase X, X in {2,3}                                                |
| threePhasedLSMGroup#urmsX               | potential                 | RMS Voltage on phase X, X in {2,3}                                                |
| threePhasedLSMGroup#umoyX               | potential                 | Mean Voltage on phase X, X in {2,3}                                               |
| threePhasedLSMGroup#sinstsX             | power                     | Instantaneous withdrawn apparent power on phase X, X in {1,2,3}                   |
| threePhasedLSMGroup#smaxsnX             | power                     | Maximum withdrawn apparent power of the day on phase X, X in {1,2,3}              |
| threePhasedLSMGroup#umoyXDate           | datetime                  | Timestamp of UMOYX value, X in {2,3}                                              |
| threePhasedLSMGroup#smaxsnXMinus1       | power                     | Maximum withdrawn apparent power on the previous day on phase X, X in {1,2,3}     |
| threePhasedLSMGroup#smaxsXDate          | datetime                  | Timestamp of SMAXSNX value, X in {1,2,3}                                          |
| threePhasedLSMGroup#smaxsnXMinus1Date   | datetime                  | Timestamp of SMAXSNX-1 value, X in {1,2,3}                                        |

##### Producer only Channels

| Channel                                 | Type                     | Description                                                                       |
|-----------------------------------------|--------------------------|-----------------------------------------------------------------------------------|
| producerLSMGroupType#eait               | energy                   | Total active energy injected                                                      |
| producerLSMGroupType#erqX               | energy                   | Total reactive energy on index X, X in {1,...,4}                                  |
| producerLSMGroupType#sinsti             | power                    | Instantaneous injected apparent power                                             |
| producerLSMGroupType#smaxin             | power                    | Maximum injected apparent power of the day                                        |
| producerLSMGroupType#smaxinMinus1       | power                    | Maximum injected apparent power of the previous day                               |
| producerLSMGroupType#ccain              | power                    | Injected active charge point N                                                    |
| producerLSMGroupType#ccainMinus1        | power                    | Injected active charge point N-1                                                  |
| producerLSMGroupType#smaxinDate         | datetime                 | Timestamp of SMAXIN value                                                         |
| producerLSMGroupType#smaxinMinus1Date   | datetime                 | Timestamp of SMAXIN-1 value                                                       |
| producerLSMGroupType#ccainDate          | datetime                 | Timestamp of CCAIN value                                                          |
| producerLSMGroupType#ccainMinus1Date    | datetime                 | Timestamp of CCAIN-1 value                                                        |

#### Calculated Channels

The binding also offer a number of "calculated" channels.

These channels can help decode existing data into more readable content, such as Relais, Stage State, PjourF, and PPointe advice.
Additionally, they can provide new values like irms1f, sactive, and sreactive.

- irms1f provides a more precise value of irms1, calculated as papp / urms1.
- cosphi is a specific channel designed to receive the cosphi value calculated by an external device.
- If cosphi is exposed, sactive and sreactive will provide the Active and Reactive power, respectively, derived from Apparent Power and Cosphi.

Note:<br/>
Cosphi, Active Power, and Reactive Power are not directly available on Linky meters.
Active power is particularly important as it is used to calculate consumption, which is what your supplier bills you for.


| Channel                              | Type                     | Description                                                                       |
|--------------------------------------|--------------------------|-----------------------------------------------------------------------------------|
| calc#irms1f                          | current                  | Floating value for Irms1                                                          |
| calc#power-factor-type               | power-factor             | Channel to feed external cosPhi calculation                                       |
| calc#sactive                         | power                    | Active power calculate from apparent power and Cosphi                             |
| calc#sreactive                       | power                    | Reactive power calculate from apparent power and Cosphi                           |
| calc#contact-sec                     | contact                  | Stge decode : contact Sec Value                                                   |
| calc#cutoff-type                     | cutoff                   | Stge decode : type of cutoff                                                      |
| calc#cache                           | contact                  | Stge decode : linky cache state                                                   |
| calc#over-voltage                    | over-voltage-state       | Stge decode : overvoltage state type                                              |
| calc#exceeding-power                 | exceeding-power-state    | Stge decode : exceding power state type                                           |
| calc#function                        | function                 | Stge decode : function type                                                       |
| calc#direction                       | direction                | Stge decode : direction type                                                      |
| calc#supplier-rate                   | numeric                  | Stge decode : supplier rate index                                                 |
| calc#distributor-rate                | numeric                  | Stge decode : distributor rate index                                              |
| calc#clock                           | contact                  | Stge decode : clock state                                                         |
| calc#plc                             | plc                      | Stge decode : PLC type                                                            |
| calc#outputcom                       | ouputcom-state           | Stge decode : Output com state type                                               |
| calc#plc-state                       | plc-state                | Stge decode : PLC state type                                                      |
| calc#plc-synchro                     | synchro-plc-state        | Stge decode : PLC Synchro state type                                              |
| calc#tempo-today                     | tempo                    | Stge decode : Today tempo color                                                   |
| calc#tempo-tomorrow                  | tempo                    | Stge decode : Tomorrow tempo color                                                |
| calc#advice-moving-tips              | moving-tips              | Stge decode : Advice of moving tips type                                          |
| calc#moving-tips                     | moving-tips              | Stge decode : Current moving tips type                                            |
| calc#pjourf1-plus1                   | string                   | Pjourf decode : Slot 1                                                            |
| calc#pjourf2-plus1                   | string                   | Pjourf decode : Slot 2                                                            |
| calc#pjourf3-plus1                   | string                   | Pjourf decode : Slot 3                                                            |
| calc#pjourf4-plus1                   | string                   | Pjourf decode : Slot 4                                                            |
| calc#pjourf5-plus1                   | string                   | Pjourf decode : Slot 5                                                            |
| calc#pjourf6-plus1                   | string                   | Pjourf decode : Slot 6                                                            |
| calc#pjourf7-plus1                   | string                   | Pjourf decode : Slot 7                                                            |
| calc#pjourf8-plus1                   | string                   | Pjourf decode : Slot 8                                                            |
| calc#ppointe1                        | string                   | PPointe decode : Slot 1                                                           |
| calc#ppointe2                        | string                   | PPointe decode : Slot 2                                                           |
| calc#ppointe3                        | string                   | PPointe decode : Slot 3                                                           |
| calc#ppointe4                        | string                   | PPointe decode : Slot 4                                                           |
| calc#ppointe5                        | string                   | PPointe decode : Slot 5                                                           |
| calc#ppointe6                        | string                   | PPointe decode : Slot 6                                                           |
| calc#ppointe7                        | string                   | PPointe decode : Slot 7                                                           |
| calc#ppointe8                        | string                   | PPointe decode : Slot 8                                                           |

How to feed cosphi ?

You will have to create a specific channel Cosphi.
First channel will be the one you get your cosphi from.
Second one, with profile="follow" will feed the cosphi to linky binding.


```java
Number											
	CompteurEDF_xxx_Cosphi
	"Linky Cosphi [%s]"			
	(gLinky)
  [ "Measurement" ]	     
	{ 
		channel="mqtt:topic:local:CompteurPi1:PFac_ComptGenerale",
		channel="linky:linky-local:linkylocalmelody:calc#cosphi"[profile="follow"]
  }
```

### Full Example

#### Direct Local Connection, Historical TicMode 

The following `things` file declare a serial USB controller on `/dev/ttyUSB1` with ticMode set to Historical and prmId = 2145499xxx :

```java
Bridge linky:serial:local "SerialBridge" [ serialport="/dev/ttyUSB1", ticMode="HISTORICAL" ] {
    Thing linky:linky-local:linkylocalxx "Linky Local xxx" [ prmId="2145499xxx"]  
}
```

This `items` file links some supported channels to items:

```java
Number:Power TLInfoEDF_PAPP "PAPP" <energy>               { channel="linky:linky-local:linkylocalxxx:historical-base#papp"   }
Number:ElectricCurrent TLInfoEDF_ISOUSC "ISOUSC" <energy> { channel="linky:linky-local:linkylocalxxx:historical-base#isousc" }
String TLInfoEDF_PTEC "PTEC" <energy>                     { channel="linky:linky-local:linkylocalxxx:historical-base#ptec"   }
Number:ElectricCurrent TLInfoEDF_IMAX "IMAX" <energy>     { channel="linky:linky-local:linkylocalxxx:historical-base#imax"   }
Number:ElectricCurrent TLInfoEDF_ADPS "ADPS" <energy>     { channel="linky:linky-local:linkylocalxxx:historical-base#adps"   }
Number:ElectricCurrent TLInfoEDF_IINST "IINST" <energy>   { channel="linky:linky-local:linkylocalxxx:historical-base#iinst"  }
Number:Energy TLInfoEDF_HCHC "HCHC" <energy>              { channel="linky:linky-local:linkylocalxxx:historical-hphc#hchc"   }
Number:Energy TLInfoEDF_HCHP "HCHP" <energy>              { channel="linky:linky-local:linkylocalxxx:historical-hphc#hchp"   }
String TLInfoEDF_HHPHC "HHPHC" <energy>                   { channel="linky:linky-local:linkylocalxxx:historical-hphc#hhphc"  }
```

#### D2L Connection, Standard Mode

The following `things` file declare a D2L controller, listenning on port 7845,  with ticMode set to Standard and prmId = 2145499xxx :

```java
Bridge linky:d2l:local "D2lBridge" [ listenningPort="7845" ] {
    Thing linky:linky-local:linkylocalxx "Linky Local xxx" [ prmId="2145499xxx"]  
}
```

This `items` file links some supported channels to items:

```java
Number:Power TLInfoEDF_SINSTS "SINSTS" <energy> ["Measurement","Power"]             { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#sinsts"    }
Number:ElectricCurrent TLInfoEDF_PREF "PREF" <energy> ["Measurement","Power"]       { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#pref"      }
String TLInfoEDF_LTARF "LTARF" <energy> ["Status"]                                  { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#ltarf"      }
Number:ElectricCurrent TLInfoEDF_SMAXSN "SMAXSN" <energy> ["Measurement","Energy"]  { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#smaxsn"     }
Number:ElectricCurrent TLInfoEDF_IRMS1 "IRMS1" <energy> ["Measurement","Current"]   { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#irms1"      }
Number:Energy TLInfoEDF_EASF01 "EASF01" <energy> ["Measurement","Energy"]           { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#easf01"     }
Number:Energy TLInfoEDF_EASF02 "EASF02" <energy> ["Measurement","Energy"]           { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#easf02"     }
String TLInfoEDF_NGTF "NGTF" <energy> ["Status"]                                    { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#ngtf"       }
DateTime TLInfoEDF_SMAXSN_DATE "SMAXSN_DATE" <energy> ["Measurement","Energy"]      { channel="linky:linky-local:linkylocalkerclae:commonLSMGroupType#smaxsnDate" }
```



### Tested Hardware

The Linky binding has been successfully validated with below hardware configuration:

| Wifi interface                      | Power Energy Meter model    | Mode(s)                   | TIC mode   |                                                                                     |
|-------------------------------------|-----------------------------|---------------------------|------------|-------------------------------------------------------------------------------------|
| D2L                                 | Linky                       | Single-phase TEMPO        | Standard   | [(more details)](https://eesmart.fr/modulesd2l/erl-wifi-compteur-linky/)            |


| Serial interface                    | Power Energy Meter model    | Mode(s)                   | TIC mode   |                                                                                     |
|-------------------------------------|-----------------------------|---------------------------|------------|-------------------------------------------------------------------------------------|
| GCE Electronics USB Teleinfo module | Actaris A14C5               | Single-phase HCHP & Base  | Historical | [(more details)](https://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html)   | 
| Cartelectronic USB Teleinfo modem   | Sagem S10C4                 | Single-phase HCHP         | Historical | [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html)                                                                                                                                            |
| GCE Electronics USB Teleinfo module | Linky                       | Single-phase HCHP         | Standard   | [(more details)](https://gce-electronics.com/fr/usb/655-module-teleinfo-usb.html)   |
| Cartelectronic USB Teleinfo modem   | Linky                       | Three-phase TEMPO         | Standard   | [(more details)](https://www.cartelectronic.fr/teleinfo-compteur-enedis/17-teleinfo-1-compteur-usb-rail-din-3760313520028.html)                                                                                                                                            | 

You can also build a Teleinfo modem by yourself (see [this example](http://bernard.lefrancois.free.fr)).

### Verify Communication

The good communication can be verified using software like picocom

picocom -b 9600 -d 7 -p e -f n /dev/ttyUSB1 (for Standard mode)
picocom -b 1200 -d 7 -p e -f n /dev/ttyUSB1 (for Historical mode)

After a few seconds, you should see Linky frame displayed in your terminal

```java
ADSC    81187xxxxxx    M
VTIC    02      J
DATE    H250314152111           ;
NGTF         TEMPO              F
LTARF       HP  BLEU            +
EAST    120684765       6
EASF01  083957312       H
EASF02  031765917       J
EASF03  001219877       G
EASF04  002681581       D
EASF05  000607543       ?
EASF06  000452535       ?
EASF07  000000000       (
EASF08  000000000       )
EASF09  000000000       *
EASF10  000000000       "
EASD01  076241272       ?
EASD02  033890466       H
EASD03  003663842       B
EASD04  006889185       P
IRMS1   006     4
URMS1   233     B
PREF    12      B
PCOUP   12      \
SINSTS  01389   [
SMAXSN  H250314052302   07720   8
SMAXSN-1        H250308233739   09340   (
CCASN   H250314150000   05090   >
CCASN-1 H250314143000   06668   *
UMOY1   H250314152000   231     +
STGE    013AC401        R
MSG1    PAS DE          MESSAGE                 <
PRM     2145499yyyyyyyy  4
RELAIS  000     B
NTARF   02      O
NJOURF  00      &
NJOURF+1        00      B
PJOURF+1        00004001 06004002 16004001 NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE NONUTILE      1
```




## Getting Electricity Pricing

These set of thing provides regulated electricity prices in France (Base, HPHC, Tempo)
This can be used to plan energy consumption, for example to calculate the cheapest period for running a dishwasher or charging an EV.

### Supported Things

The binding offers things for the three usual tariff classes (proposed by example by EDF).

- `base`: This is the basic subscription with a fixed kWh price.
- `hphc`: Alternative subscription with different price in a given hour set (low hours/high hours). Night price get a discount.
- `tempo`: Alternative suscription with different price in regards of day colors and day or night.
    Day colors can be Red, White or Blue. 
    Red day are the one where there is most energy demands in France, and are the most higher price.
    White day are intermediate pricing, for day where energy demands is not almost important as red day.
    Blue day are the one with the lower price.

    Blue day, and in some proportion White day get very interesting discount in regards of base tariff.
    But Red day, in counter part, have very high rate during the daylight.

### Thing Configuration

Things (`base`, `hphc` and `tempo`) only offers the configuration of the power output of the electrical delivery point (Linky terminal).

| Name                  | Type        | Description                                 | Default       | Required |
|-----------------------|-------------|---------------------------------------------|---------------|----------|
| puissance             | integer     | PDL power output (in kVA)                   | 6             | no       |

### Thing Channels

#### `base` Tariff Thing

All channels are read-only.

  | Channel      | Type               | Description                                                 | Advanced |
  |--------------|--------------------|-------------------------------------------------------------|----------|
  | fixed-ttc    | Number:Currency    | Yearly fixed price including taxes                          | No       |
  | variable-ttc | Number:EnergyPrice | Energy price in €/kWh including taxes                       | No       |
  | tariff-start | DateTime           | Beginning date for this tariff                              | Yes      |
  | fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes                          | Yes      |
  | variable-ht  | Number:EnergyPrice | Energy price in €/kWh excluding taxes                       | Yes      |

#### `hphc` Tariff Thing

All channels are read-only.

  | Channel      | Type               | Description                                                 | Advanced |
  |--------------|--------------------|-------------------------------------------------------------|----------|
  | fixed-ttc    | Number:Currency    | Yearly fixed price including taxes                          | No       |
  | hc-ttc       | Number:EnergyPrice | Low hours energy price in €/kWh including taxes             | No       |
  | hp-ttc       | Number:EnergyPrice | High hours energy price in €/kWh including taxes            | No       |
  | tariff-start | DateTime           | Beginning date for this tariff                              | Yes      |
  | fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes                          | Yes      |
  | hc-ht        | Number:EnergyPrice | Low hours energy price in €/kWh excluding taxes             | Yes      |
  | hp-ht        | Number:EnergyPrice | High hours energy price in €/kWh excluding taxes            | Yes      |

#### `tempo` Tariff Thing

  | Channel      | Type               | Description                                                 | Advanced |
  |--------------|--------------------|-------------------------------------------------------------|----------|
  | fixed-ttc    | Number:Currency    | Yearly fixed price including taxes                          | No       |
  | blue-hc-ttc  | Number:EnergyPrice | Low hours blue day energy price in €/kWh including taxes    | No       |
  | blue-hp-ttc  | Number:EnergyPrice | High hours blue day energy price in €/kWh including taxes   | No       |
  | white-hc-ttc | Number:EnergyPrice | Low hours white day energy price in €/kWh including taxes   | No       |
  | white-hp-ttc | Number:EnergyPrice | High hours white day energy price in €/kWh including taxes  | No       |
  | red-hc-ttc   | Number:EnergyPrice | Low hours red day energy price in €/kWh including taxes     | No       |
  | red-hp-ttc   | Number:EnergyPrice | High hours red day energy price in €/kWh including taxes    | No       |
  | tariff-start | DateTime           | Beginning date for this tariff                              | Yes      |
  | fixed-ht     | Number:Currency    | Yearly fixed price excluding taxes                          | Yes      |
  | blue-hc-ht   | Number:EnergyPrice | Low hours blue day energy price in €/kWh excluding taxes    | Yes      |
  | blue-hp-ht   | Number:EnergyPrice | High hours blue day energy price in €/kWh excluding taxes   | Yes      |
  | white-hc-ht  | Number:EnergyPrice | Low hours white day energy price in €/kWh excluding taxes   | Yes      |
  | white-hp-ht  | Number:EnergyPrice | High hours white day energy price in €/kWh excluding taxes  | Yes      |
  | red-hc-ht    | Number:EnergyPrice | Low hours red day energy price in €/kWh excluding taxes     | Yes      |
  | red-hp-ht    | Number:EnergyPrice | High hours red day energy price in €/kWh excluding taxes    | Yes      |


### Full Example

#### Thing Configuration
```java
Thing linky:base:local "Tarification Actuelle Base" [puissance=9]
Thing linky:hphc:local "Tarification Actuelle HP/HC" [puissance=9]
Thing linky:tempo:local "Tarification Actuelle Tempo" [puissance=9]
```

#### Item Configuration
```java
DateTime Tarif_Start { channel="linky:hphc:local:tariff-start" }
Number:Currency Abonnement_Annuel {channel="linky:hphc:local:fixed-ttc"}
Number:EnergyPrice Prix_Heure_Pleine {channel="linky:hphc:local:hp-ttc"}
Number:EnergyPrice Prix_Heure_Creuse {channel="linky:hphc:local:hc-ttc"}
```


## Getting Tempo Calendar Information	

### Thing Channels 

- The tempo group will give information about the tempo day color link to a tempo contract

  | Channel ID                                                     | Item Type         | Description                                                                   |
  |----------------------------------------------------------------|-------------------|-------------------------------------------------------------------------------|
  | linky-tempo-calendar#tempo-info-today                          | tempo-value       | The tempo color for the current day                                           |
  | linky-tempo-calendar#tempo-info-tomorrow                       | tempo-value       | The tempo color for the tomorrow                                              |
  | linky-tempo-calendar#tempo-info-timeseries                     | tempo-value       | A timeseries channel that will expose full tempo information for one year     |

### Displaying Tempo Graph

Using the timeseries channel, you will be able to esealy create a calendar graph to show the tempo calendar.
You will need for this to enable a timeseries persistence framework.
Graph definitions will look like this

The resulting graph will look like this:

![TempoGraph](doc/TempoGraph.png)


Sample code:

```java
config:
  chartType: month
  future: false
  label: Tempo
  period: M
  sidebar: true
slots:
  calendar:
    - component: oh-calendar-axis
      config:
        cellSize: 10
        dayLabel:
          firstDay: 1
          fontSize: 16
          margin: 20
        left: center
        monthLabel:
          color: "#c0c0ff"
          fontSize: 30
          margin: 20
        orient: vertical
        top: middle
        yearLabel:
          color: "#c0c0ff"
          fontSize: 30
          margin: 50
  dataZoom:
    - component: oh-chart-datazoom
      config:
        orient: horizontal
        show: true
        type: slider
  grid: []
  legend:
    - component: oh-chart-legend
      config:
        show: false
  series:
    - component: oh-calendar-series
      config:
        aggregationFunction: average
        calendarIndex: 0
        coordinateSystem: calendar
        item: Linky_Melody_Tempo
        label:
          formatter: =v=> JSON.stringify(v.data[0]).substring(1,11)
          show: true
          smartFormatter: false
        name: Series 1
        service: inmemory
        type: heatmap
  title:
    - component: oh-chart-title
      config:
        show: true
        text: Calendrier Tempo
  toolbox:
    - component: oh-chart-toolbox
      config:
        presetFeatures:
          - saveAsImage
          - restore
          - dataView
          - dataZoom
          - magicType
        show: true
  tooltip:
    - component: oh-chart-tooltip
      config:
        formatter: "{c}"
        show: true
  visualMap:
    - component: oh-chart-visualmap
      config:
        bottom: 0
        calculable: true
        inRange:
          color:
            - "#0000ff"
            - "#ffffff"
            - "#ff0000"
        left: center
        max: 2
        min: 0
        orient: horizontal
        presetPalette: ""
        show: false
        type: continuous
  xAxis: []
  yAxis: []

```


## Console Commands

The binding provides one specific command you can use in the console.
Enter the command `openhab:linky` to get the usage.

```shell
openhab:linky <thingUID> report <start day> <end day> [<separator>] - report daily consumptions between two dates
```

The command `report` reports in the console the daily consumptions between two dates.
If no dates are provided, the last 7 are considered by default.
Start and end day are formatted yyyy-mm-dd.

Here is an example of command you can run: `openhab:linky linky:linky:local report 2020-11-15 2020-12-15`.

## Docker Specificities

In case you are running openHAB inside Docker, the binding will work only if you set the environment variable `CRYPTO_POLICY` to the value "unlimited" as documented [here](https://github.com/openhab/openhab-docker#java-cryptographic-strength-policy).


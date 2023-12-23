# BoschSpexor Binding

In short what is spexor: 
spexor is a smart IoT device that enables you to observe your environment by measuring this different kind of embedded sensors. 
It is a mobile device and can be used in Europe on your travels but also at home, in your garden shelter or wherever you want to use it. 
It only needs a WiFi connection or mobile network to connect. 
If you would like to get more details, we recommend visiting our Website https://www.spexor-bosch.com.

With the new add-on for openHAB we would like to enable your smart home or maybe your smart mobile environment to get connected to your other systems and enable new possibilities.
Getting an indication of your air quality and visualizing this via your smart light bulbs or just getting a voice reminder if it got to a certain level could be an option. 
You can also enable the burglary on defined rules as you like and play sounds or do other stuff if the spexor has observed an intrusion.

## Supported Things

This plugin supports the first generation of Bosch spexor

## Discovery

The spexor discovery service will automatically find owned things.
To enable this, you have to visit the servlet page http://<<yourOPENHAB>>:<yourOPENHABport>>/spexor it will guide you through the setup.

Just create the Bosch spexor Bridge as a Thing


## Binding Configuration

No additional configuration needed. 

## Thing Configuration

The spexor thing can be configured by setting up a refresh interval.
The spexor contains 2 properties

| property                  | type   | description                                                              | editable      |
|---------------------------|--------|--------------------------------------------------------------------------|---------------|
| ID                        | ID     | identifies the thing                                                     | no            |
| spexor refresh interval   | number | definition how often a spexor should check its state with the spexor API | yes           |
                                       

## Channels

Definition and explanation of the channels is provided in the UI.
To change the observation of Burglary or other (Observation Type Burglary) you can change the state (BurglaryState f.e.) by selecting the values *deactivated* or *activated*. 
A drop-down menu should be available via the UI.

Section **spexor Status**

| item-type                 | description                                                                                                                                               |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| State of Charge           | Battery state of the device in percentage.                                                                                                                |
| Last Connected            | Timestamp when the spexor was connected to the backend systems - format: YYYY-MM-DDTHH:mm:ss.S+0200                                                       |
| Connection State          | Information about the connection state. Which type of communication channels was used like WiFi or Mobile Network                                         | 
| Powered                   | Information about the charge port state. Boolean value                                                                                                    |
| Energy Mode               | Used energy saving mode of the spexor. Please see documentation at https://developer.bosch.com/products-and-services/apis/spexor-api/documentation        |
| Installed Firmware Version| Currently installed software version number on the spexor                                                                                                 | 
| Available Firmware Version| Shows the available firmware version that could be installed on the spexor                                                                                |
| Firmware State            | Indicates the current state of installation. Please see documentation at https://developer.bosch.com/products-and-services/apis/spexor-api/documentation  |

Section **spexor Profile**

| item-type                 | description                                                                                                                                               |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Profile Name              | Name of the current used profile of the spexor.                                                                                                           |
| Profile Type              | Type of the profile. Please see documentation at https://developer.bosch.com/products-and-services/apis/spexor-api/documentation                          |

Section **spexor observation information**

| item-type                 | description                                                                                                                                               |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Observation Type Burglary | Status of the Observation Type Burglary. It can be requested to be changed by selecting the state "deactivated" or "activated". States are documented here: https://developer.bosch.com/products-and-services/apis/spexor-api/documentation      |
| Observation Type Fire     | Status of the Observation Type Fire. It can be requested to be changed by selecting the state "deactivated" or "activated". Fire is only available if purchased / subscribed.                                                                                 |

Section **spexor Sensors**

| item-type                     | description                                                                                                                                               |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sensor Type AirQuality        | Measured value of the Air Quality. Value Range is between 0 and 500++. Low values indicate good air quality.                                              |
| Sensor Type AirQualityLevel   | Measured value of the Air Quality. Interpreted values into categories.                                                                                    |
| Sensor Type Temperature       | Measured temperature in ° Celsius                                                                                                                         |
| Sensor Type Pressure          | Measured pressure in Pascal                                                                                                                               |
| Sensor Type Acceleration      | Measured acceleration in mG                                                                                                                               |
| Sensor Type Light             | Measured light indication in Light Index                                                                                                                  |
| Sensor Type Humidity          | Measured humidity in % r.H.                                                                                                                               |
| Sensor Type Microphone        | Measured volume in XXX which is measured by the microphone                                                                                                |
| Sensor Type PassiveInfrared   | Measured movement via the PIR Sensor. Its based on an event counter. As higher the value as more events did happen.                                       | 

There are also additional channels upcoming. 
Dependent on your use and purchased features within the spexor. 
The channels will be named dependent on the available Observation Types defined in the official Bosch documentation https://developer.bosch.com/products-and-services/apis/spexor-api/documentation/spexor-device

If you have subscribed to Fire it will automatically appear as a new channel. Same for any sensor values that are unlocked / available on your spexor. 
The implementation is dynamically updating your model as soon as your feature is available. 
If any channel or sensor value will not be available any more it won't get updated and also would disappear.

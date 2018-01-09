# Amazon Echo Control Binding

This binding let control openHAB Amazon Echo devices (Alexa).
It provide features to control and view the current state of:

- volume
- pause/continue/next track/previous track
- connect/disconnect bluetooth devices
- start playing radio

Some ideas what you can do in your home by using rules and other openHAB controlled devices:

- Automatic turn on your amplifier and connect echo with bluetooth if the echo playes music
- If the amplifier was turned of, the echo stop playing and disconnect the bluetooth
- The echo starts playing radio if the light was turned on
- The echo starts playing radio at specified time 

## Note ##

This binding uses the same API as the Web-Browser-Based Alexa site (alexa.amazon.de). In other words, it simulates a user which is using the web page.
Unfortunately, the binding can get broken if Amazon change the website.

The binding is tested with amazon.de and amazon.co.uk accounts, but should also work with all others. 

## Warning ##

For the connection to the Amazon server, your password of the Amazon account is required, this will be stored in your openHAB thing device configuration. So you should be sure, that nobody other has access to your configuration! 

## What else you should know ##

All the display options are updated by polling the amazon server. The polling time can be configured, but a minimum of 10 seconds is required. The default is 60 seconds, which means the it can take up to 60 seconds to see the correct state. I do not know, if there is a limit implemented in the amazon server if the polling is too fast and maybe amazon will lock your account. 60 seconds seems to be safe.

## Supported Things

| Thing type id            | Name                  |
|--------------------------|-----------------------|
| account                  | Amazon Account        |
| echo                     | Amazon Echo Device    |


## Discovery

The first 'Amazon Account' thing will be automatically discovered. After configuration of the thing with the account data, a 'Amazon Echo' thing will be discovered for each registered device.

## Binding Configuration

The binding does not have any configuration. The configuration of your amazon account habe to be done in the 'Amazon Account' device.

## Thing Configuration

The Amazon Account device need the following configurations:

| Config name              | Description                 |
|--------------------------|-----------------------|
| amazonSite               | The amazon site where the echos are registered. e.g. amazon.de      |
| email                    | Email of your amazon account    |
| password                 | Password of your amazon account    |
| pollingIntervalInSeconds | Polling interval for the device state in seconds. Default 60, minimum 10    |

The Amazon Echo device need the following configurations:

| Config name              | Description                 |
|--------------------------|-----------------------|
| serialNumber             | Serial number of the amazon echo in the Alexa app  |

You will find the serial number in the alexa app.

## Channels

| Channel Type ID     | Item Type | Access Mode | Description                                                                                                                                                                
|---------------------|-----------|-------------|-------------------------------------------------------------------------------------------------------
| player              | Player    | R/W         | Control the music player e.g. pause/continue/next track/previous track                                                                                                
| volume              | Dimmer    | R/W         | Control the volume                                                                                            
| shuffle             | Switch    | R/W         | Shuffle play if applicable, e.g. playing a playlist     
| imageUrl            | String    | R           | Url of the album image or radio station logo     
| title               | String    | R           | Title of the current media     
| subtitle1           | String    | R           | Subtitle of the current media     
| subtitle2           | String    | R           | Additional subtitle of the current media     
| providerDisplayName | String    | R           | Name of the music provider   
| bluetoothId         | String    | R/W         | Bluetooth device id. Used to connect to a specific device or disconnect if a empty string was provided
| bluetooth           | Switch    | R/W         | Connect/Disconnect to the last used bluetooth device (works after a bluetooth connection was established after the openhab start) 
| bluetoothDeviceName | String    | R           | User friendly name of the connected bluetooth device
| radioStationId      | String    | R/W         | Start playing of a radio station by specifying its id od stops playing if a empty string was provided
| radio               | Switch    | R/W         | Start playing of the last used radio station works after the radio station started after the openhab start)

## Full Example

### amzonechocontrol.things

```php
Bridge amazonechocontrol:account:account1 [amazonSite="amazon.de", email="myaccountemail@myprovider.com", password="secure", pollingIntervalInSeconds=60]
{
    Thing echo echo1 "Alexa" @ "Living Room" [serialNumber="SERIAL_NUMBER"]
}
```
You will find the serial number in the Alexa app.

### amzonechocontrol.items:

```
Group Alexa_Living_Room <player>

Player Echo_Living_Room_Player               "Player"                           (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:player"}
Dimmer Echo_Living_Room_Volume               "Volume [%.0f %%]" <soundvolume>   (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:volume"}
Switch Echo_Living_Room_Shuffle              "Shuffle"                          (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:shuffle"}
String Echo_Living_Room_ImageUrl             "Image URL"                        (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:imageUrl"}
String Echo_Living_Room_Title                "Title"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:title"}
String Echo_Living_Room_Subtitle1            "Subtitle 1"                       (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle1"}
String Echo_Living_Room_Subtitle2            "Subtitle 2"                       (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:subtitle2"}
String Echo_Living_Room_ProviderDisplayName  "Provider"                         (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:providerDisplayName"}
String Echo_Living_Room_BluetoothId          "Bluetooth Id"     <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothId"}
Switch Echo_Living_Room_Bluetooth            "Bluetooth"        <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetooth"}
String Echo_Living_Room_BluetoothDeviceName  "Bluetooth Device" <bluetooth>     (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:bluetoothDeviceName"}
String Echo_Living_Room_RadioStationId       "Radio Station Id"                 (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radioStationId"}
Switch Echo_Living_Room_Radio                "Radio"                            (Alexa_Living_Room) {channel="amazonechocontrol:echo:account1:echo1:radio"}

```

### amzonechocontrol.sitemap:

```
sitemap amzonechocontrol label="Echo Devices"
{
        Frame label="Alexa" {
            Default item=Echo_Living_Room_Player
            Slider  item=Echo_Living_Room_Volume
            Switch  item=Echo_Living_Room_Shuffle
            Text    item=Echo_Living_Room_Title
            Text    item=Echo_Living_Room_Subtitle1     
            Text    item=Echo_Living_Room_Subtitle2
            Text    item=Echo_Living_Room_ProviderDisplayName
            Text    item=Echo_Living_Room_BluetoothId
            Switch  item=Echo_Living_Room_Bluetooth
            Text    item=Echo_Living_Room_BluetoothDeviceName
            Text    item=Echo_Living_Room_RadioStationId
            Switch  item=Echo_Living_Room_Radio          
        }
}
```

## Trademark Disclaimer

All Amazon Echo, Alexa and other products and Amazon and other companies are trademarks™ or registered® trademarks of their respective holders. Use of them does not imply any affiliation with or endorsement by them. 



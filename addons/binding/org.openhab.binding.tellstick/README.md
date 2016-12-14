#Tellstick Binding

This is an openHAB binding for Tellstick devices produced by Telldus, a Swedish company based in Lund.

The original Tellstick focused on controlling 433 MHz devices like switches, dimmers and reading sensors from different brands. <br>
Many of the supported devices are cheaper and "low-end" and support have been made by reverse engineer the transmission protocols. <br>
All of these 433 MHz devices is one-way, so some versions of the Tellstick monitoring the air to keep the state of all devices. 
  
The latest versions have also implemented Z-Wave as transmission protocol which open up for more robust transmission due two-ways communication. 
 
## Supported Things

This binding implements two different API:  
**1)** *Telldus Core* which is a local only interface supported by USB based device. <br>
**2)** *Telldus Live* which is a REST based cloud service maintained by Telldus. <br>
3) (According to [Telldus](http://developer.telldus.com/blog/2016/01/21/local-api-for-tellstick-znet-lite-beta) are they working with a local REST based API for the new Z-Wave devices. This is currently **NOT** supported by this binding.)

Depending on your Tellstick model different API methods is available: 

<table>
<tr><td><b>Model</b></td> <td><b>Telldus Core</b></td> <td><b>Telldus Live</b></td> <td><b>Verified working with openHAB</b></td></tr>
<tr><td>Tellstick Basic</td><td>X</td><td>X</td></tr>
<tr><td>Tellstick Duo</td><td>X</td><td>X</td><td>X</td></tr>
<tr><td>Tellstick Net</td><td></td><td>X</td></tr>
<tr><td>Tellstick ZNet Lite</td><td></td><td>X</td></tr>
<tr><td>Tellstick ZNet Pro</td><td></td><td>X</td><td>X</td></tr>
</table>


This binding supports the following thing types:

* Telldus Core Bridge
* Telldus Live Bridge
* Dimmable Device
* Switchable Device
* Sensors

## Binding Configuration

---

#####For Telldus Core only#####

*First of all you need to make sure that your JVM is matching your installed Telldus Center. This normally means openHab must run on a 32bit JVM for windows and a 64bit JVM for linux. For windows the binding is hardcoded to look for Telldus Center in Programs Files ("C:/Program Files/Telldus/;C:/Program Files (x86)/Telldus/"). If you have trouble getting the telldus core library to work you can modify the library path using*

## Discovery

Both Telldus Core and Live devices will be discovered once the bridges are configured.

## Thing Configuration

```xtend
Bridge tellstick:telldus-core:1 "Tellstick Duo" []

Bridge tellstick:telldus-live:2 "Tellstick ZWave" [publicKey="XXX", privateKey="YYYY", token= "ZZZZ", tokenSecret="UUUU"]
```

## Channels

There are two levels of discovery. When you add this binding it will try to discover the Telldus Core Bridge (this only works if you have Telldus installed and right 32/64 bit version of java).
If you want to use the Telldus Live bridge, then you manually need to add this thing.
When you add either of those all devices and sensors will be discovered and reported in the inbox.

## Thing Configuration

#####Telldus Core Bridge#####

- **libraryPath:** The path to tellduscore.dll/so, 
- **resendInterval:** The interval between each transmission of command, default 100ms.

#####Telldus Live Bridge#####

To configure Telldus Live you have request OAuth tokens from Telldus. Goto this page
http://api.telldus.com/keys/index and request your keys and update the config.
- **privateKey:** Private key
- **publicKey:** Public key
- **token:** Token
- **tokenSecret:** Token secret
- **refreshInterval:** How often we should contact Telldus.Live to check for updates

The devices and sensors should not be configured by hand, let the discovery/inbox configure these.

## Channels

Actuators (dimmer/switch) support the following channels:
| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|----------------------------------------------- |
| dimmer          | Number       | This channel indicates the current dim level |
| state           | Switch       | This channel indicates whether a device is turned on or off. |
| timestamp       | DateTime     | This channel reports the last time this device state changed. |

Sensors support the following channels:
| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|----------------------------------------------- |
| humidity        | Number       | This channel reports the current humidity in percentage |
| temperature      | Number       | This channel reports the current temperature in celsius |
| timestamp       | DateTime     | This channel reports the last time this sensor was updates. |

## Full Example

```xtend
Bridge tellstick:telldus-core:1 "Tellstick Duo" []
Bridge tellstick:telldus-live:2 "Tellstick ZWave" [refresh="10000", publicKey="XXXXXXXX", privateKey="YYYYYY", token= "ZZZZZZZZ", tokenSecret="UUUUUUUUUU"]
```

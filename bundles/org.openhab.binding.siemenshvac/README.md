# SiemensHvac Binding

This binding is to support Siemens Hvac controller ecosystem, and the Web Gateway interface OZW672.
A typical system is composed of:

                                                    
<=== Ethernet ===>   | OZW672 | <====== ¨BSB/LPB BUS ======> | Hvac Controler (RVS41.813/327) | ====== | Internal device in your system : sensors, boiler, external pac unit, ... |

There's a lot of different Hvac controler depending on model in lot of different PAC constructor.
Mine is a Atlantic Hybrid duo whith a Siemens RVS41.813/327 inside.

Siemens have a complete set of controler reference under the name "Siemens Albatros".
Here some picture of such device.
You can also find this device in other type of heating system : boiler or solar based.

![](doc/Albatros.jpg)

You will find some information about the OZW672.01 gateway on siemens web site : 
[OZW 672 Page]
([https://hit.sbt.siemens.com/RWD/app.aspx?rc=FR&lang=fr&module=Catalog&action=ShowProduct&key=BPZ:OZW672.01)

With this binding, you will be able :
- To consult the different parameters of your system like temperature, current heating mode, water temperature, and many more.
- Modify the functionning mode of your device : temperature set point, heating mode, and others.



## Supported Things

Support many different things as the thing type is handle by autodiscovery.

Mainly, it will first discover the gateway.
Currently test and support is the OZW672.x series.
No test done with OZW772.x series, but it should work as well.

After, it will discover thing inside your PAC, mainly main controller of type RVS...
Only test in real condition with RVS41.813/327 but should work with all other type as the access interface is standard.


## Discovery

Discovery of Gateway can be done using Upnp.
Just switch off/on your gateway to make it annonce itself on the network.
The gateway should appears in the Inbox a few minutes after.
Be aware what you will have to modifity the password in Gateway parameters just after the discovering to make it work properly.
Be also aware that first initialization is a little long because we need to read all the metadata from the device.

Discovery of Hvac device have to be done through the Scan button inside the binding.
Go to the thing page, click on the "+" button, select the siemensHvac binding, and then click Scan.
Your device should appears on the page after a few seconds.


## Binding Configuration

There is no particular configuration to be done.
The only revelant parameters is on the OZW672 thing for the user and password to use to connect to the gateway.
IP should have be discovered automatically via UPNP.


## Thing Configuration



## Channels

Channels are autodiscovered, you will find them on the RVS things.
They are organized the same way as the LCD screen of your PAC device, by top level menu functionnality, and sub-functionnalities.
Each channel are strongly typed, so for exemple, for heating mode, openhab will provide you with a list of choice supported by the device.



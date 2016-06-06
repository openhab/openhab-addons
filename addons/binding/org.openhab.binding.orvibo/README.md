# Orvibo Binding

This binding integrates Orvibo devices that communicate using UDP. Primarily this was designed for S20 Wifi Sockets but other products using the same protocol may be implemented in future.

## Supported Things

* S20 Wi-Fi Smart Socket
* AllOne Wi-Fi Smart Remote

## Discovery

This binding can automatically discover devices that have already been added to the Wifi network.  Please see the check your instruction manual or the help guide in the app for instructions on how to add your device to your Wifi network.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

### S20: 

This is optional, it is recommended to let the binding discover and add Orvibo devices.
 
To manually configure an S20 Thing you must specify its deviceId (MAC address). 
 
In the thing file, this looks e.g. like

```
Thing orvibo:s20:mysocket [ deviceId="AABBCCDDEEFF"]
```

### AllOne:

Manual Thing definition for AllOne devices is still optional, it is recommended to let the binding discover and add Orvibo devices.

**However the AllOne Thing requires you to set the root folder configuration property.**  If you do no set the root folder the Thing will not change to "ONLINE".

To manually configure an AllOne Thing you must specify its deviceId (MAC address) and rootFolder (for saving command data). 
 
In the thing file, this looks e.g. like
```
Thing orvibo:allone:myallone [ deviceId="FFEEDDCCBBAA", rootFolder="/home/pi"]
```

## Channels

### S20:
|Channel | Data Type |Description | Example  |
|------- | -------- |-------- | ---- |
|power	 | Switch 	|Current power state of switch | orvibo:s20:mysocket:power |

### AllOne:
|Channel | Data Type | Description | Example  |
|------- | -------- | -------- | ---- |
|learn	 | String | The file to save the next learn response | orvibo:allone:myallone:learn |
|emit	 | String | The file to emit | orvibo:allone:myallone:emit |

## Items:

```
Switch MySwitch              "Switch state [%s]"	{channel="orvibo:s20:mysocket:power"}
String LearnString           "Learn file [%s]"		{channel="orvibo:allone:myallone:learn"}
String EmitString            "Emit file [%s]"	  	{channel="orvibo:allone:myallone:emit"}
```

## Example Sitemap

Using the above things channels and items 
Sitemap:

```
sitemap demo label="Main Menu" {
        Frame  {
                Switch item=MySwitch
				Text item=LearnString
				Text item=EmitString
        }
}
```
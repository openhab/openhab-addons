# iCloud Binding

The iCloud Binding helps to integrate apple device information into openHAB.

## Supported Things

The following devices are known to work with this binding:
* iPhone 6s
* iPhone 5c
* iPhone 7
* iPad Air 2/2017/Pro

But other devices should work as well. Please provide feedback if you have tested another device type. We will add it to the list.

## Discovery

For each iCloud account to be included in openHAB a binding (bridge) needs to be configured with your apple id, password & refresh rate. 

The devices registered to this account will then be automatically discovered.


## Binding Configuration

No textual configuration required/supported.

## Thing Configuration

Devices are automatically discovered once the binding (bridge) is configured.

## Channels

The following channels are available (if supported by the device):
* Battery Status
* Battery Level
* Find My Phone
* Location
* Location Accuracy
* Last Update
* Address Street
* Address City
* Address Country
* Formatted Address

## Full Example

Only configuration via paperUI is tested and supported:

1.  Select the binding:  
![Select binding](./doc/Config_1.png "Step 1")
2.  Select the bridge:   
![Select bridge](./doc/Config_2.png "Step 2") 
3.  Configure your account and the desired refresh rate. 
![Configure](./doc/Config_3.png "Step 3") 



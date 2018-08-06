# Energenie Mi|Home Binding

Energenie Mi|Home Binding is the house home automation solution of Energenie.  
The system consists of Internet connected gateway and devices (called subdevices) that are communicating with the gateway using the Openthings protocol.  
The subdevices are paired to the gateway.  

In order to use the Mi|Home solution and the binding, you should first create an account in the [Mi|Home portal](https://mihome4u.co.uk/) or through the Mi|Home app from [Google Play](https://play.google.com/store/apps/details?id=energenie.mihome) or [App Store](https://itunes.apple.com/cy/app/mi-home/id982173821?mt=8).  
The username and the password will be used in Openhab to access your devices.

## Supported Things

Currently the binding supports the following thing types:

- Mi|Home Gateway 
- Mi|Home House Monitor 
- Mi|Home Motion Sensor 
- Mi|Home Open Sensor 

## Binding Configuration

Required configuration parameters:

- *username* - User's email address in the Mi|Home portal
- *password* - User's password in the Mi|Home portal

## Discovery

All subdevices should be paired before they can be used, including The Home Gateway.  
Please do this through the [Mi|Home portal](https://mihome4u.co.uk/) or through the Mi|Home app from [Google Play](https://play.google.com/store/apps/details?id=energenie.mihome) or [App Store](https://itunes.apple.com/cy/app/mi-home/id982173821?mt=8).    
Follow the steps that are explained in details for the pairing.  
When you finish them you should see your Home Gateway with all your paired devices.  
After setting the credentials in Openhab (the ones you used for your registration in the Mi|Home portal), you can begin the discovery.  
Discovery results will be created first for your Home Gateway and after adding it, for each device that is already paired and doesn't have a Thing representation.  

## Gateway (Bridge) Configuration

Optional configuration parameters:

- *updateInterval* - Update interval to poll data from a device in seconds. If not set, the default value is 30 seconds;

## Subdevices configuration

Optional configuration parameters:

- *updateInterval* - Update interval to poll data from a device in seconds. If not set, the default value is 10 seconds;

## Channels

The channels supported from the different devices are listed below:

## Channels for Mi|Home Gateway (Bridge):

| - | Channel Type ID | Item Type  | Description      |    
| - | --------------- | ---------- | ---------------- |    
| - | lastSeen        | DateTime   | Time when the device was last seen from the Mi|Home server |    

## Channels for Mi|Home House Monitor:

| - | Channel Type ID  | Item Type  |   Description     |    
| - | ---------------- | ---------- | ----------------- |    
| - | voltage          | Number     | Voltage           |    
| - | real_power       | Number     | Real power        |  
| - | today_consumption| Number     | The consumption in Wh for the day|    

## Channels for Mi|Home Motion Sensor:

| - | Channel Type ID | Item Type  | Description      |    
| - | --------------- | ---------- | ---------------- |    
| - | state           | Switch     | Set to ON when movement starts and OFF when movement stops |    

## Channels for Mi|Home Open Sensor:

| - | Channel Type ID | Item Type  | Description      |    
| - | --------------- | ---------- | ---------------- |    
| - | state           | Contact    | Set to Open or Close |    

### Full Example

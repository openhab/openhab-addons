# NeoHub Binding

The NeoHub binding allows you to connect openHab via TCP/IP to Heatmiser's NeoHub and integrate your NeoStat smart thermostats and NeoPlug smart plugs onto the bus.

## Binding Configuration

The binding polls each thermostat or plug that is configured in your items, at the pollInterval rate as set in the configuration. 

| Property            | Default | Required | Description
|---------------------|---------|----------|------------------------------------------
| pollInterval        | 60      |   No     | Refresh interval in seconds
| hostName            |         |   Yes    | The IP address of the NeoHub
| portNumber          | 4242    |   No     | The port number for the NeoHub interface

## Item Configuration (NeoStat)

The following properties, and their associated item types are shown below. The RO and RW in the description column indicate which properties are read only (RO) or read/write (RW).

| Property            | Data Type | Description                  
|---------------------|-----------|----------------------------------------------------------------
| RoomTemperature     | Number    | RO Current room temperature
| SetTemperature      | Number    | RW Target room temperature
| FloorTemperature    | Number    | RO Current floor temperature
| DeviceName          | String    | RO The device (room) name
| Heating             | Switch    | RO Indicates if the thermostat is calling for heat
| Away      		  | Switch    | RW Indicates and controls if the thermostat is in AWAY mode
| Standby             | Switch    | RW Indicates and controls if the thermostat is in STANDBY mode

## Item Configuration (NeoPlug)

| Property            | Data Type | Description
|---------------------|-----------|---------------------------------------------------------------
| Switch              | Switch    | RW Indicates and controls if the plug is turned on

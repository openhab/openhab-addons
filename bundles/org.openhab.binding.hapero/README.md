# hapero Binding

This Binding is intended for [Spanner Re²/Hapero](https://www.holz-kraft.com/) Central Heating Systems
It collects data from the FTP upload feature of the furnace control system. (The "Upload.hld" file).
The file needs to be either on a path visible to this openHAB instance or on an FTP Server that can be accessed from this openHAB instance.

It is not possible to control the furnace through the binding as this is not supported by the control system.

FTP upload is only supported for "myTouch" control panels. (Check if the FTP configuration option is present in the internet settings of the control panel)

## Supported Things

- `haperoBridge`: The bridge receives updates of the "Upload.hld" file delivered by the furnace control system and distributes it to the Things
- `furnace`: Provides data from the furnace itself `furnace`
- `buffer`: Provides data from the installed buffer tank(s) `buffer`
- `boiler`: Provides data from the installed hot water boiler tank(s) `boiler`
- `heatingCircuit`: Provides data from the installed heating circuit(s) `heatingCircuit`

## Discovery

The Bridge will automatically discover the furnace and all Buffer/Boiler Tanks and Heating Circuits as configured in the furnace control system.
Only one furnace can be present in the system
Buffer Tanks are numbered from 1..4
Heating Circuits are numbered from 1..6

## Thing Configuration

### `haperoBridge` Thing Configuration

| Name            | Type    | Description                              | Default | Required | Advanced |
|-----------------|---------|------------------------------------------|---------|----------|----------|
| refreshTimeout  | integer | Timeout for updates from the device      | 300     | yes      | yes      |
| accessMode      | option  | "FTP" or "File system"                   | FTP     | yes      | no       |
| ftpServer       | text    | Network address of the FTP Server        | N/A     | no       | no       |
| ftpPath         | text    | Path to the "Upload.hld" on the Server   | N/A     | no       | no       |
| port            | integer | Port address of the FTP Server           | 21      | no       | no       |
| userName        | text    | Username to login on the server          | N/A     | no       | no       |
| password        | text    | Password to login on the server          | N/A     | no       | no       |
| fileStoragePath | text    | Path to the "Upload.hld" on local system | N/A     | no       | no       |

### `furnace` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| deviceID        | text    | ID of the furnace (always "SI")       | SI      | yes      | no       |

### `buffer` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| deviceID        | text    | ID of the buffer ("PUx")              | N/A     | yes      | no       |

### `boiler` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| deviceID        | text    | ID of the boiler ("WWx")              | N/A     | yes      | no       |

### `heatingCircuit` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| deviceID        | text    | ID of the heating circuit ("HKx")     | N/A     | yes      | no       |

## Channels

| Channel                  | Type               | Read/Write | Description                                                 |
|--------------------------|--------------------|------------|-------------------------------------------------------------|
| temperatureTop           | Number:Temperature | R          | Temperature on top of the storage tank                      |
| temperatureBottom        | Number:Temperature | R          | Temperature on the bottom of the storage tank               |
| onTemperature            | Number:Temperature | R          | Charging of the tank is requested at/below this temperature |
| offTemperature           | Number:Temperature | R          | Target temperature for charging of the tank                 |
| pump                     | Switch             | R          | Operation status of the pump                                |
| switchValve              | Contact            | R          | Current status of the switch valve                          |
| charging                 | Number:Temperature | R          | Charging of the tank is requested                           |
| flowTemperature          | Number:Temperature | R          | Current flow temperature of the heating circuit             |
| flowTemperatureSet       | Number:Temperature | R          | Target flow temperature of the heating circuit              |
| circuitMode              | Number             | R          | Current Mode of the heating circuit                         |
| circuitSubMode           | Number             | R          | Current submode of the heating circuit                      |
| circuitFault             | Number             | R          | Fault code of the heating circuit                           |
| roomTemperature          | Number:Temperature | R          | Current room temperature for this heating circuit           |
| roomTemperatureSet       | Number:Temperature | R          | Target room temperature for this heating circuit            |
| combustionTemp           | Number:Temperature | R          | Temperature in the combustion chamber                       |
| pelletChannelTemp        | Number:Temperature | R          | Temperature in the pellet feed channel                      |
| boilerTemp               | Number:Temperature | R          | Temperature in the boiler                                   |
| boilerSetTemp            | Number:Temperature | R          | Target temperature of the boiler                            |
| outsideTemp              | Number:Temperature | R          | Outside temperature                                         |
| furnaceStatus            | String             | R          | Operating status of the furnace                             |
| burnerStatus             | String             | R          | Operating status of the burner                              |
| materialStatus           | String             | R          | Operating status of the material feed                       |
| airStatus                | String             | R          | Operating status of the air supply                          |
| grateStatus              | String             | R          | Operating status of the burner grate drive                  |
| errorStatus              | Number             | R          | Last error code of the furnace controller                   |
| multifunctionMotorMode   | String             | R          | Operating mode of the multi function motor                  |
| multifunctionMotorStatus | String             | R          | Operating status of the multi function motor                |
| powerChannel             | Number             | R          | Power level of the furnace                                  |
| airFlow                  | Number             | R          | Combustion air flow                                         |
| airFlowSet               | Number             | R          | Target Air flow                                             |
| airPower                 | Number             | R          | Air Power level of the furnace                              |
| airDrive                 | Number             | R          | Drive Value of the blower motor                             |
| airO2                    | Number             | R          | Remaining O2 in combustion                                  |

## Full Example

### Thing Configuration

Minimal Thing configuration for File access:

```java
Hapero:haperoBridge:home [ accessMode="file", fileStoragePath="/<mappedFTPFolder>" ]
```

Minimal Thing configuration for FTP access:

```java
Hapero:haperoBridge:home [ accessMode="ftp", ftpServer="<server ip>", ftpPath="<pathOnServer>", userName="...", password="..." ]
```

Once a connection to an account is established, connected Things are discovered automatically.

Alternatively, you can manually configure Things:

```java
Bridge Hapero:haperoBridge:home [ accessMode="file", fileStoragePath="/<mappedFTPFolder>" ]
{
  Thing furnace myFurnace [ deviceId="SI" ]
  Thing buffer myBuffer [ deviceId="PU1" ]
  Thing boiler myBoiler [ deviceId="WW1" ]
  Thing heatingCircuit myHeatingCircuit1 [ deviceId="HK1" ]
  Thing heatingCircuit myHeatingCircuit2 [ deviceId="HK2" ]
}
```

### Item Configuration

In the items file, you can link items to channels of your Things:

```java
Number:Temperature Combustion_Temperature "Combustion Chamber [%.1f °C]" {channel="Hapero:haperoBridge:home:myFurnace:temperatures#combustionTemp"}
Number:Temperature Buffer_Top_Temperature "Buffer Top [%.1f °C]" {channel="Hapero:haperoBridge:home:myBuffer:temperatureTop"}
```

## Warning!

The Upload.hld file contains the VNC Password and serial number of your heating control system. With these, anyone can log into your control system!
Make sure you configure your heating control system and openHAB so that this file is never visible to anyone outside your home network!

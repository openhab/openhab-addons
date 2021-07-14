# ekey Binding

This binding connects to [ekey](https://ekey.net/) converter UDP (CV-LAN) using the RARE/MULTI/HOME protocols.

## Supported Things

This binding only supports one thing type:

| Thing       | Thing Type | Description                                 |
|-------------|------------|---------------------------------------------|
| cvlan | Thing      | Represents a single ekey converter UDP |

## Thing Configuration

The binding uses the following configuration parameters.

| Parameter | Description                                                    |
|-----------|----------------------------------------------------------------|
| ipAddress | IPv4 address of the eKey udp converter.  A static IP address is recommended.|
| port      | The port as configured during the UDP Converter configuration.  e.g. 56000 (Binding default)     |
| protocol  | Can be RARE, MULTI or HOME depending on what the system supports. Binding defaults to RARE  |
| delimiter | The delimiter is also defined on the ekey UDP converter - use the ekey configuration software to determine which delimiter is used or to change it.  Binding default is `_` (underscore)  |


## Channels

| Channel ID      | Item Type          | Description                                            | Possible Values                                         |
|-----------------|--------------------|--------------------------------------------------------|---------------------------------------------------------|
| action          | Number             | This indicates whether access was granted (value=0) or denied (value=-1). According to the ekey documentation there are six more values possible as you can see in the .map file below.  | 0,-1                                                  |
| fingerId        | Number             | This indicates the finger that was used by a person. | 0-9,-1                                  |
| inputId         | Number             | This indicates which of the four digital inputs was triggered. Value is number of Input. "-1" indicates that no input was triggered. | 0-4,-1                                               |
| keyId           | Number             | This indicates which of the four keys was used. See ekey documentation on "keys". | 0-4,-1                               |
| relayId         | Number             | This indicates which relay has been switched. | 0-3,-1                               |
| terminalId      | Number | This provides the serial number of the packet source. The source can be a fingerprint terminal or the controller (in case of digital inputs). The Serial number has a length of 13. When using RARE mode, only the trailing 8 digits can be returned. |                              |
| terminalName    | String |  This returns the 4-character-long name that was specified on the controller for the specific terminals. |                                                         |
| userId | Number | This indicates which user has been detected on the terminal. The value is the numerical order of the user as it was specified on the controller. | 0-99,-                                                        |
| userName        | String             | This returns the ten-character-long name of the person that has been recognized on the terminal. The name that is returned must have been previously specified on the controller. |                                                   |
| userStatus      | Number             | This indicates the status of the user (-1=undefined, 1=enabled, 0= disabled) | 0,1,-1                                          |

## Examples
tbd.

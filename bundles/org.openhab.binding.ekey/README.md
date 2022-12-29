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

| Channel ID | Item Type | Protocol | Description                                                                                                                                                                                                                                           | Possible Values                      |
|------------|-----------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| action     | Number    | R/M/H    | This indicates whether access was granted (value=0) or denied (value=-1).                                                                                                                                                                             | 0,-1 (136 and 137 with RARE protocol |
| fingerId   | Number    | R/M/H    | This indicates the finger that was used by a person.                                                                                                                                                                                                  | 0-9,-1                               |
| fsName     | String    | M        | This returns the 4-character-long name that was specified on the controller for the specific terminals.                                                                                                                                               |                                      |
| fsSerial     | Number    | R/M/H        | This returns the serial number for the specific terminal.                                     |
| inputId    | Number    | M        | This indicates which of the four digital inputs was triggered. Value is number of Input. "-1" indicates that no input was triggered.                                                                                                                  | 0-4,-1                               |
| keyId      | Number    | M        | This indicates which of the four keys was used. See ekey documentation on "keys".                                                                                                                                                                     | 0-4,-1                               |
| relayId    | Number    | R/H      | This indicates which relay has been switched.                                                                                                                                                                                                         | 0-3,-1                               |
| termId     | Number    | R        | This provides the serial number of the packet source. The source can be a fingerprint terminal or the controller (in case of digital inputs). The Serial number has a length of 13. When using RARE mode, only the trailing 8 digits can be returned. |                                      |
| userId     | Number    | R/M/H    | This indicates which user has been detected on the terminal. The value is the numerical order of the user as it was specified on the controller.                                                                                                      | 0-99,-1                              |
| userName   | String    | M        | This returns the ten-character-long name of the person that has been recognized on the terminal. The name that is returned must have been previously specified on the controller.                                                                     |                                      |
| userStatus | Number    | M        | This indicates the status of the user (-1=undefined, 1=enabled, 0= disabled)                                                                                                                                                                          | 0,1,-1                               |

## Examples

example.things

```java
Thing ekey:cvlan:de3b8db06e "Ekey Udp Converter" @ "Control Panel" [ ipAddress="xxx.xxx.xxx.xxx", port="56000", protocol="RARE", delimiter="_" ]
```

rare.items

```java
Number Action "Last action [MAP(ekey_action.map):%d]"                          { channel="ekey:cvlan:de3b8db06e:action" }
Number FingerID "User used finger [MAP(ekey_finger.map):%d]"                   { channel="ekey:cvlan:de3b8db06e:fingerId" }
Number RelayID "Last relay that has been swiched [%d]"                         { channel="ekey:cvlan:de3b8db06e:relayId" }
Number Serialnumber "Serialnumber [%d]"                                        { channel="ekey:cvlan:de3b8db06e:fsSerial" }
Number TerminalID "Last used terminal [MAP(ekey_terminal.map):%d]"             { channel="ekey:cvlan:de3b8db06e:termId" }
Number UserID "Last user that accessed the house was [MAP(ekey_names.map):%d]" { channel="ekey:cvlan:de3b8db06e:userId" }
```

multi.items

```java
Number Action "Last action [MAP(ekey_action.map):%d]"                          { channel="ekey:cvlan:de3b8db06e:action" }
Number FingerID "User used finger [MAP(ekey_finger.map):%d]"                   { channel="ekey:cvlan:de3b8db06e:fingerId" }
String FsName "Name of Scanner [%s]                                            { channel="ekey:cvlan:de3b8db06e:fsName" }
Number FsSerial "Serialnumber [%d]"                                            { channel="ekey:cvlan:de3b8db06e:fsSerial" }
Number InputID "Last input that has been triggered [%d]"                       { channel="ekey:cvlan:de3b8db06e:inputId" }
Number KeyID  "Last key that has been used [%d]"                               { channel="ekey:cvlan:de3b8db06e:keyId" }
Number UserID "Last user that accessed the house was [MAP(ekey_names.map):%d]" { channel="ekey:cvlan:de3b8db06e:userId" }
String UserName " Name of Last user that accessed the house was : [%d]"        { channel="ekey:cvlan:de3b8db06e:userName" }
Number UserStatus "Last user that accessed the house was [MAP(ekey_names.map):%d]" { channel="ekey:cvlan:de3b8db06e:userStatus" }
```

home.items

```java
Number Action "Last action [MAP(ekey_action.map):%d]"                          { channel="ekey:cvlan:de3b8db06e:action" }
Number FingerID "User used finger [MAP(ekey_finger.map):%d]"                   { channel="ekey:cvlan:de3b8db06e:fingerId" }
Number RelayID "Last relay that has been swiched [%d]"                         { channel="ekey:cvlan:de3b8db06e:relayId" }
Number Serialnumber "Serialnumber [%d]"                                        { channel="ekey:cvlan:de3b8db06e:fsSerial" }
Number UserID "Last user that accessed the house was [MAP(ekey_names.map):%d]" { channel="ekey:cvlan:de3b8db06e:userId" }
```

transform/ekey_finger.map [This is just an example, as there is no strict rule what finger belongs to what number]

```text
0=leftlittle
1=leftring
2=leftmiddle
3=leftindex
4=leftthumb
5=rightthumb
6=rightindex
7=rightmiddle
8=rightring
9=rightlittle
-1=unknown
```

transform/ekey_names.map [NO spaces allowed]

```text
-1=Unspecified
1=JohnDoe
2=JaneDoe
```

transform/ekey_terminal.map

```text
80156839130911=Front
80156839130914=Back
```

transform/ekey_multi_action.map

```text
0=granted
-1=rejected
1=timeoutA
2=timeoutB
3=inactive
4=alwaysuser
5=notcoupled
6=digitalinput
```

transform/ekey_rare_action.map

```text
136=granted
137=rejected
```

# CP750 Binding

This binding is intended to connect to a _Dolby Digital Cinema Processor CP750_.

It uses ASCII commands send over a TCP connection on port 61408. Please note that the CP750 only accepts up to 20 simultaneous connection and will discard the oldest connection if a 21st connection is established. So be sure to grateful shutdown each connection using the client's close() method, or it's AutoCloseable functionality.

This binding wraps the CP750 Java library from https://github.com/Cybso/cp750-java/.

This project is NOT affiliated with, funded, or in any way associated with Dolby Laboratories, Inc.

Currently, only _Fader_, _Mute_ and _Input Mode_ channels are implemented.

## Supported Things

- `cp750` 

## Thing Configuration

Normally, only the hostname or IP address must be configured.

### `sample` Thing Configuration

| Name              | Type    | Description                                       | Default | Required | Advanced |
|-------------------|---------|---------------------------------------------------|---------|----------|----------|
| hostname          | text    | Hostname or IP address of the device              | N/A     | yes      | no       |
| port              | integer | TCP port if different from 61408                  | 61408   | no       | no       |
| refreshInterval   | integer | Interval the device is polled in sec.             | 5       | no       | no       |
| reconnectInterval | integer | Interval a new connection is tried after IO error | 10      | no       | no       |

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                                                                                                     |
|---------|--------|------------|-----------------------------------------------------------------------------------------------------------------|
| version | Switch | R          | Version as reported by the device                                                                               |
| fader   | Dimmer | RW         | Fader value (0 to 100)                                                                                          | 
| mute    | Switch | RW         | Mute (ON or OFF)                                                                                                |
| input   | String | RW         | Input channel as string (one of "analog", "dig-1", "dig-2", "dig-3", "dig-4", "non-sync" or "mic")              |
| analog  | Switch | RW         | Is ON if input mode is 'analog'. When an ON command is retrieved, input mode will be changed to this channel.   |
| dig1    | Switch | RW         | Is ON if input mode is 'dig-1'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig2    | Switch | RW         | Is ON if input mode is 'dig-2'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig3    | Switch | RW         | Is ON if input mode is 'dig-3'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig4    | Switch | RW         | Is ON if input mode is 'dig-4'. When an ON command is retrieved, input mode will be changed to this channel.    |
| nonsync | Switch | RW         | Is ON if input mode is 'non-sync'. When an ON command is retrieved, input mode will be changed to this channel. |
| mic     | Switch | RW         | Is ON if input mode is 'mic'. When an ON command is retrieved, input mode will be changed to this channel.      |

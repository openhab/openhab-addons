# UnifiedRemote Binding

This binding integrates the [Unified Remote Server](https://www.samsung.com).

<b>Known Limitations: It needs the web interface to be enabled on the server settings to work.</b>

## Discovery

Discovery works on the default discovery UDP port 9511.

## Thing Configuration

The Unified Remote Server Thing requires the host name, and mac address to be correctly configured in order to work correctly.
Other properties like tcpPort and udpPort are not used in the initial implementation.

E.g.

```
Thing unifiedremote:server:xx-xx-xx-xx-xx-xx [ host="192.168.1.10", macAddress="xx-xx-xx-xx-xx-xx", friendlyName="test" ]
```


## Channels

Unified Remote Server support the following channels:

| Channel Type ID  | Item Type | Description                                                                                             |
|------------------|-----------|---------------------------------------------------------------------------------------------------------|
| relmtech_basic__input-left-channel     | String    | Mouse Left Click       |
| relmtech_basic__input-right-channel    | String    | Mouse Left Click       |
| relmtech_basic__input-delta-channel    | String    | Mouse Move (string format 0,x,y) (example 0,10,10) |
| unified_navigation-toggle-channel      | String    | Use Navigation Keys (SPACE, RETURN, UP...) |
| unified_power-lock-channel             | String    | Lock Unified Server Host |
| unified_power-sleep-channel            | String    | Sleep Unified Server Host |
| unified_power-shutdown-channel         | String    | Shutdown Unified Server Host |
| unified_power-restart-channel          | String    | Restart Unified Server Host |
| unified_power-logoff-channel           | String    | Log off Unified Server Host |
| unified_media-play_pause-channel       | String    | Media Play/Pause |
| unified_media-next-channel             | String    | Media Next |
| unified_media-previous-channel         | String    | Media Previous |
| unified_media-stop-channel             | String    | Media Stop |
| unified_media-volume_mute-channel      | String    | Volume Mute |
| unified_media-volume_up-channel        | String    | Volume Up by One |
| unified_media-volume_down-channel      | String    | Volume Down by One |
| unified_monitor-brightness_down-channel| String    | Monitor Brightness Down by One |
| unified_monitor-brightness_up-channel  | String    | Monitor Brightness Up by One |
| unified_monitor-turn_off-channel       | String    | Monitor Turn Off |
| unified_monitor-turn_on-channel        | String    | Monitor Turn On |



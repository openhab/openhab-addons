# Gardena Binding

This is the binding for [Gardena Smart Home](http://www.gardena.com/de/rasenpflege/smartsystem/).
This binding allows you to integrate, view and control Gardena Smart Home devices in the openHAB environment.

## Supported Things

Devices connected to Gardena Smart Home, currently:

| Thing type | Name |
|------------|------|
| bridge     | smart Home Gateway | 
| mower      | smart Sileno(+) Mower |
| watering_computer | smart Water Control |
| sensor | smart Sensor |

The schedules are not yet integrated!

## Discovery

An account must be specified, all things for an account are discovered automatically.

## Account Configuration

There are several settings for an account:

| Name | Required | Description |
| --- | --- | --- |
| **email** | yes | The email address for logging into the Gardena Smart Home |
| **password** | yes | The password for logging into the Gardena Smart Home |
| **sessionTimeout** | no | The timeout in minutes for a session to Gardena Smart Home (default = 30) |
| **connectionTimeout** | no | The timeout in seconds for connections to Gardena Smart Home (default = 10) |
| **refresh** | no | The interval in seconds for refreshing the data from Gardena Smart Home (default = 60) |

## Example

### Things

Minimal Thing configuration:

```
Bridge gardena:account:home [ email="...", password="..." ]
```

Configuration with refresh:

```
Bridge gardena:account:home [ email="...", password="...", refresh=30 ]
```

Configuration of multiple bridges:

```
Bridge gardena:account:home1 [ email="...", password="..." ]
Bridge gardena:account:home2 [ email="...", password="..." ]
```

Once a connection to an account is established, connected Things are discovered automatically.

Alternatively, you can manually configure a Thing:

```
Bridge gardena:account:home [ email="...", password="..." ]
{
  Thing mower myMower [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
}
```

## Items

In the items file, you can link items to channels of your Things:

```
Number Battery_Level "Battery [%d %%]" {channel="gardena:mower:home:myMower:battery#level"}
```

### Debugging and Tracing

If you want to see what's going on in the binding, switch the loglevel to TRACE in the Karaf console

```
log:set TRACE org.openhab.binding.gardena
```

Set the logging back to normal

```
log:set INFO org.openhab.binding.gardena
```

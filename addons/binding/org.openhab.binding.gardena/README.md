# Gardena Binding

This is the binding for [Gardena Smart Home](http://www.gardena.com/de/rasenpflege/smartsystem/).
This binding allows you to integrate, view and control all Gardena Smart Home devices in the openHAB environment.

## Supported Things

All devices connected to Gardena Smart Home, currently:

| Thing type | Name |
|------------|------|
| bridge     | smart Home Gateway | 
| mower      | smart Sileno(+) Mower |
| watering_computer | smart Water Control |
| sensor | smart Sensor |

The schedules are not yet integrated!

## Discovery

A bridge must be specified, all things for a bridge are discovered automatically.

## Bridge Configuration

There are several settings for a bridge:

| **email** | required | The email address for logging into the Gardena Smart Home |
| **password** | required | The password for logging into the Gardena Smart Home |
| **sessionTimeout** | optional | The timeout in minutes for a session to Gardena Smart Home (default = 30) |
| **connectionTimeout** | optional | The timeout in seconds for connections to Gardena Smart Home (default = 10) |
| **refresh** | optional | The interval in seconds for refreshing the data from Gardena Smart Home (default = 60) |

## Example

### Things

Minimal Thing configuration:

```
Bridge gardena:bridge:home [ email="...", password="..." ]
```

Configuration with refresh:

```
Bridge gardena:bridge:home [ email="...", password="...", refresh=30 ]
```

Configuration of multiple bridges:

```
Bridge gardena:bridge:home1 [ email="...", password="..." ]
Bridge gardena:bridge:home2 [ email="...", password="..." ]
```

Once a bridge connection is established, connected Things are discovered automatically.

Alternatively, you can manually configure a Thing:

```
Bridge gardena:bridge:home [ email="...", password="..." ]
{
  Thing mower myMower [ deviceId="c81ad682-6e45-42ce-bed1-6b4eff5620c8" ]
}
```

## Items

In the items file, you can link items to channels of your Things:

```
Number Battery_Level "Battery [%d %%]" {channel="gardena:mower:home:myMower:battery#level"}
```

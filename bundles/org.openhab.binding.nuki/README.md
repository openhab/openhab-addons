# Nuki Binding

This is the binding for the [Nuki Smart Lock](https://nuki.io).  
This binding allows you to integrate, view, control and configure the Nuki Bridge, Nuki Smart Lock and Nuki Opener.

## Prerequisites

1. At least one Nuki Smart Lock or Nuki Opener which is paired via Bluetooth with a Nuki Bridge. For this go and get either:
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and a [Nuki Bridge](https://nuki.io/en/bridge/) or
    * the [Nuki Combo](https://nuki.io/en/shop/nuki-combo/) or
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and the Nuki [Nuki Software Bridge](https://play.google.com/store/apps/details?id=io.nuki.bridge)
2. The Bridge HTTP-API has to be enabled during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).

It is absolutely recommended to configure static IP addresses for both, the openHAB server and the Nuki Bridge!  

### Nuki Bridge Callback

The Nuki Binding will manage the required callback from the Nuki Bridge to the openHAB server if *manageCallbacks* is set to `true`.
If *manageCallbacks* is not set it will default to `true`.  

If you want to manage the callbacks from the Nuki Bridge to the openHAB server by yourself, you need to set *manageCallbacks* to `false`.
Then add the callback on the Nuki Bridge via Bridge API Endpoint */callback/add* in the format `http://<openHAB_IP>:<openHAB_PORT>/nuki/bcb`.  
The Sheet [NukiBridgeAPI](https://docs.google.com/spreadsheets/d/1SGKWhqwqRyOGbv4NEq-8PAPjBORRixvEjRuzO-nVabQ) is a helpfull tool for listing, adding and removing callbacks.  

## Supported Bridges

This binding supports just one bridge type: The Nuki Bridge. Create one `bridge` per Nuki Bridge available in your home automation environment.  

The following configuration options are available:  

| Parameter       | Description                                                                                                                                                                                        | Comment      |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| ip              | The IP address of the Nuki Bridge. Look it up on your router. It is recommended to set a static IP address lease for the Nuki Bridge (and for your openHAB server too) on your router.             | Required     |
| port            | The Port which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).                                                                 | Default 8080 |
| apiToken        | The API Token which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).                                                            | Required     |
| manageCallbacks | Let the Nuki Binding manage the callbacks on the Nuki Bridge. It will add the required callback on the Nuki Bridge. If there are already 3 callbacks, it **will delete** the callback with ID `0`. | Default true |

### Bridge discovery

Bridges on local network can be discovered automatically if both Nuki Bridge and openHAB have working internet connection. You can check whether discovery
is working by checking [discovery API endpoint](https://api.nuki.io/discover/bridges). To discover bridges do the following:
* In openHAB UI add new thing, select Nuki Binding and start scan
* Within 30s press button on Nuki Bridge you want to discover
* Bridge should appear in inbox

Pressing bridge button is required for binding to obtain valid API token. If the button isn't pressed during discovery, bridge will
be created but token must be set manually for binding to work.


## Supported Things

This binding supports 2 things - Nuki Smart Lock and Nuki Opener. Both devices can be added using discovery after bridge they are 
connected to is configured and online.

### Nuki Smart Lock

The following configuration options are available:

| Parameter | Description                                                                                                                                                                                               | Comment       |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| unlatch   | If set to `true` the Nuki Smart Lock will unlock the door but then also automatically pull the latch of the door lock. Usually, if the door hinges are correctly adjusted, the door will then swing open. | Default false |

#### Supported Channels

- **lock** (Switch)  
    Use this channel with a Switch Item to lock and unlock the door.

- **lockState** (Number)  
    Use this channel if you want to execute other supported lock actions or to display the current lock state.  
    Supported actions are:

  | Action | Name                     |
  |--------|--------------------------|
  | 1      | Unlock                   |
  | 2      | Lock                     |
  | 3      | Unlatch                  |
  | 4      | Lock 'n' Go              |
  | 5      | Lock 'n' Go with Unlatch |

  Supported lock states are:
  
  | State  | Name                     |
  |--------|--------------------------|
  | 0      | Uncalibared              |
  | 1      | Locked                   |
  | 2      | Unlocking                |
  | 3      | Unlocked                 |
  | 4      | Locking                  |
  | 5      | Unlatched                |
  | 6      | Unlatched (Lock 'n' Go)  |
  | 7      | Unlatching               |
  | 254    | Motor blocked            |
  | 255    | Undefined                |

  Unfortunately the Nuki Bridge is not reporting any transition states (e.g. for Lock 'n' Go).

- **lowBattery** (Switch)  
    Use this channel to receive a low battery warning.
  
  **keypadLowBattery** (Switch)
    Use this channel to receive a low battery warning for keypad paired with smart lock.
  
  **batteryLevel** (Number)
    Use this channel to monitor current battery level of smart lock.
  
  **batteryCharging** (Switch)
    Use this channel to monitor charging of smart lock.

- **doorsensorState** (Number)  
    Use this channel if you want to display the current door state provided by the door sensor.
  
  | Action | Name                     |
  |--------|--------------------------|
  | 0      | Unavailable              |
  | 1      | Deactivated              |
  | 2      | Closed                   |
  | 3      | Open                     |
  | 4      | Unknown                  |
  | 5      | Calibrating              |

### Nuki Opener

Nuki Opener has no configuration properties.

#### Supported channels

- **openerState** (Number)
  Use this channel if you want to execute supported Opener actions or monitor current Opener state.
  Supported actions are:
  
  | Action | Name                       |
  |--------|----------------------------|
  | 1      | Activate ring to open      |
  | 2      | Deactivate ring to open    |
  | 3      | Electric strike actuation  |
  | 4      | Activate continuous mode   |
  | 5      | Deactivate continuous mode |

  Supported opener states are:

  | State  | Name                |
  |--------|---------------------|
  | 0      | Untrained           |
  | 1      | Online              |
  | 3      | Ring to open active |
  | 5      | Open                |
  | 7      | Opening             |
  | 253    | Boot run            |
  | 255    | Undefined           |

- **openerMode** (Number)
  Use this channel to monitor opener modes. Supported values are:
  
  | Mode   | Name            |
  |--------|-----------------|
  | 2      | Door mode       |
  | 3      | Continuous mode |

- **openerLowBattery** (Switch)
  Use this channel to receive a low battery warning.

- **ringActionState** (Switch)
  Use this channel to receive notification when doorbell rings.
  
- **ringActionTimestamp** (DateTime)
  Use this channel to get timestamp of last time doorbell was rung.

## Full Example

A manual setup through files could look like this:

### things/nuki.things

```
Bridge nuki:bridge:NB1 [ ip="192.168.0.50", port=8080, apiToken="myS3cr3t!", manageCallbacks=true ] {
    Thing smartlock SL1 [ nukiId="12AB89EF", unlatch=false ]
}
```

### items/nuki.items

```
Switch Frontdoor_Lock		"Frontdoor (Unlock / Lock)"	<nukiwhite>		{ channel="nuki:smartlock:NB1:SL1:lock" }
Number Frontdoor_LockState	"Frontdoor (Lock State)"	<nukisl>		{ channel="nuki:smartlock:NB1:SL1:lockState" }
Switch Frontdoor_LowBattery	"Frontdoor Low Battery"		<nukibattery>		{ channel="nuki:smartlock:NB1:SL1:lowBattery" }
Number Frontdoor_DoorState	"Frontdoor (Door State)"	<door>			{ channel="nuki:smartlock:NB1:SL1:doorsensorState" }
```

### sitemaps/nuki.sitemap

```
sitemap nuki label="Nuki Smart Lock" {
	Frame label="Channel Lock" {
		Switch item=Frontdoor_Lock
	}
	Frame label="Channel State used for lock actions" {
		Switch item=Frontdoor_LockState mappings=[2="Unlock", 7="Unlatch", 1002="LnGo", 1007="LnGoU", 4="Lock"]
	}
	Frame label="Channel State" {
		Text item=Frontdoor_LockState label="Lock State [MAP(nukilockstates.map):%s]"
	}
	Frame label="Channel Low Battery" {
		Text item=Frontdoor_LowBattery	label="Low Battery [%s]"
	}
	Frame label="Channel Door State" {
		Text item=Frontdoor_DoorState label="Door State [MAP(nukidoorsensorstates.map):%s]"
	}
}
```

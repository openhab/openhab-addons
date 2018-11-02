# Nuki Binding

This is the binding for the [Nuki Smart Lock](https://nuki.io).  
This binding allows you to integrate, view, control and configure the Nuki Bridge and Nuki Smart Locks.  

## Prerequisites

1. At least one Nuki Smart Lock which is paired via Bluetooth with a Nuki Bridge. For this go and get either:
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and a [Nuki Bridge](https://nuki.io/en/bridge/) or
    * the [Nuki Combo](https://nuki.io/en/shop/nuki-combo/) or
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and the Nuki [Nuki Software Bridge](https://play.google.com/store/apps/details?id=io.nuki.bridge)
2. The Bridge HTTP-API has to be enabled during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/). Note down the IP, Port and API token.

It is absolutely recommended to configure static IP addresses for both, the openHAB server and the Nuki Bridge!  

You can configure the Nuki Binding either by using Paper UI or manually through text files.  

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

## Supported Things

This binding support just one thing type: The Nuki Smart Lock. Create one `smartlock` per Nuki Smart Lock available in you home automation environment.

The following configuration options are available:

| Parameter | Description                                                                                                                                                                                               | Comment       |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| nukiId    | The `Nuki-ID` of the Nuki Smart Lock. It is a 8-digit hexadecimal string. Look it up on the sticker on the back of the Nuki Smart Lock (remove mounting plate).                                           | Required      |
| unlatch   | If set to `true` the Nuki Smart Lock will unlock the door but then also automatically pull the latch of the door lock. Usually, if the door hinges are correctly adjusted, the door will then swing open. | Default false |

## Supported Channels

- **lock** (Switch)  
    Use this channel with a Switch Item to lock and unlock the door.

- **lockState** (Number)  
    Use this channel if you want to execute other supported lock actions or to display the current lock state.  
    Supported Lock Actions are: `2` (Unlock), `7` (Unlatch), `1002` (Lock 'n' Go), `1007` (Lock 'n' Go with Unlatch) and `4` (Lock).  
    Supported Lock States are : `1` (Locked), `2` (Unlocking), `3` (Unlocked), `4` (Locking), `7` (Unlatching), `1002` (Unlocking initiated through Lock 'n' Go) and `1007` (Unlatching initiated through Lock 'n' Go with Unlatch).  
    Unfortunately the Nuki Bridge is not reporting any transition states (e.g. for Lock 'n' Go).

- **lowBattery** (Switch)  
    Use this channel to receive a low battery warning.

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
Switch Frontdoor_Lock		"Frontdoor (Unlock / Lock)"		<nukiwhite>		{ channel="nuki:smartlock:NB1:SL1:lock" }
Number Frontdoor_State		"Frontdoor (State)"				<nukisl>		{ channel="nuki:smartlock:NB1:SL1:lockState" }
Switch Frontdoor_LowBattery	"Frontdoor Low Battery"			<nukibattery>	{ channel="nuki:smartlock:NB1:SL1:lowBattery" }
```

### sitemaps/nuki.sitemap

```
sitemap nuki label="Nuki Smart Lock" {
	Frame label="Channel Lock" {
		Switch item=Frontdoor_Lock
	}
	Frame label="Channel State used for lock actions" {
		Switch item=Frontdoor_State mappings=[2="Unlock", 7="Unlatch", 1002="LnGo", 1007="LnGoU", 4="Lock"]
	}
	Frame label="Channel State" {
		Text item=Frontdoor_State label="Lock State [MAP(nukilockstates.map):%s]"
	}
	Frame label="Channel Low Battery" {
		Text item=Frontdoor_LowBattery	label="Low Battery [%s]"
	}
}
```

# Nuki Binding



This is the binding for the [Nuki Smart Lock](https://nuki.io).  
This binding allows you to integrate, view, control and configure the Nuki Bridge and Nuki Smart Locks in the openHAB environment.  



## Prerequisites

1. At least one Nuki Smart Lock which is paired via Bluetooth with a Nuki Bridge. For this go and get either:
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and a [Nuki Bridge](https://nuki.io/en/bridge/) or
    * the [Nuki Combo](https://nuki.io/en/shop/nuki-combo/) or
    * a [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and the Nuki [Nuki Software Bridge](https://play.google.com/store/apps/details?id=io.nuki.bridge)
2. The Bridge HTTP-API has to be enabled during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/). Note down the IP, Port and API token.
3. A registered callback URL on the Nuki Bridge which points to http://< openHAB IP >/nuki/bcb



## Discovery

There is no automatic discovery of Nuki Bridge and Nuki Smart Lock yet implemented.



## Supported Bridges

This binding was tested with the [Nuki Bridge](https://nuki.io/en/bridge/).  
It should also work with the [Nuki Software Bridge](https://play.google.com/store/apps/details?id=io.nuki.bridge) - Feedback is really appreciated!  



## Supported Things

Nuki Smart Lock(s) which is/are paired via Bluetooth with the Nuki Bridge.



## Supported Channels

- **unlock** (Switch)  
    Use this channel with a Switch Item to unlock and lock the door. Configure "Unlatch" to true if your Nuki Smart Lock is mounted on a door lock with a knob on the outside.

- **lockAction** (Number)  
    Use this channel if you want to execute other supported lock actions or to display the current lock state.  
    Supported Lock Actions are: 2 (Unlock), 7 (Unlatch), 1002 (Lock 'n' Go), 1007 (Lock 'n' Go with Unlatch) and 4 (Lock).  
    Supported Lock States are : 1 (Locked), 2 (Unlocking), 3 (Unlocked), 4 (Locking), 7 (Unlatching), 1002 (Unlocking initiated through Lock 'n' Go) and 1007 (Unlatching initiated through Lock 'n' Go with Unlatch).  
    Unfortunately the Nuki Bridge is not reporting any transition states (e.g. for Lock 'n' Go).

- **lowBattery** (Switch)  
    The Eclipse Smart Home system.low-battery channel.

Please see also below the Manual configuration examples!



## Configuration

It is absolutely recommended to configure static IP addresses for both, the openHAB server and the Nuki Bridge!  
You can configure the Nuki Binding either by using PaperUI or manually through text files.



### Nuki Bridge Configuration

**Please note:** At the moment you still need to manually configure a callback URL on the Nuki Bridge which points to your openHAB server.  
See [Bridge HTTP-API](https://nuki.io/en/api/), Section */callback* and */callback/add*.  

For example, if your openHAB server's IP address is 192.168.0.100, Port 8080 and your Nuki Bridge's IP address is 192.168.0.50, Port 8080, add a callback URI on the Nuki Bridge via the [Bridge HTTP-API](https://nuki.io/en/api/) to **http://192.168.0.100:8080/nuki/bcb** like this:

```
http://192.168.0.50:8080/callback/add?token=1a2b3c4d5e&url=http%3A%2F%2F192.168.0.100%3A8080%2Fnuki%2Fbcb
```

The callback URL has to be */nuki/bcb*.



### Bridge Configuration (in openHAB PaperUI)

There are three configuration parameters for a bridge:

- **IP Address** (required)  
    The IP address of the Nuki Bridge. Look it up on your router. It is recommended to set a static IP address lease for the Nuki Bridge (and for your openHAB server too) on your router.

- **Port** (required)  
    The Port which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).

- **API Token** (required)  
    The API Token which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).



### Thing Configuration (in openHAB PaperUI)

There are two configuration parameters for a thing:  

- **Nuki ID** (required)  
    The ID of the Nuki Smart Lock. Get it through the [Bridge HTTP-API](https://nuki.io/en/api/), Endpoint */list*.

- **Unlatch**  
    If switched to On (or set to true) the Nuki Smart Lock will unlock the door but then also automatically pull the latch of the door lock. Usually, if the door hinges are correctly adjusted, the door will then swing open.



### Manual configuration of Bridge and Thing (.things file)

The syntax for a bridge is:

```
Bridge nuki:bridge:<UNIQUENAME> [ <CONFIGURATION_PARAMETERS> ]
```

The syntax for a thing is:

```
Thing smartlock <UNIQUENAME> "<DISPLAYNAME>" @ "<LOCATION>" [ <CONFIGURATION_PARAMETER> ]
```

So, to manually configure a Nuki Bridge and a Nuki Smart Lock in your .things file you can do the following:

```
Bridge nuki:bridge:NB1 [ ip="192.168.0.50", port=8080, apiToken="1a2b3c4d5e" ] {
    Thing smartlock SL1 [ nukiId="123456789", unlatch=false ]
}
```



### Items Configuration (.items file)

Following the Manual configuration example from above you can do the following:

```
Switch Frontdoor_Unlock		"Frontdoor (Unlock / Lock)"		<nukiwhite>		{ channel="nuki:smartlock:NB1:SL1:unlock" }

Number Frontdoor_Action		"Frontdoor (Action)"			<nukisl>		{ channel="nuki:smartlock:NB1:SL1:lockAction" }

Switch Frontdoor_LowBattery	"Frontdoor Low Battery"			<nukibattery>	{ channel="nuki:smartlock:NB1:SL1:lowBattery" }
```

### Sitemap Configuration (.sitemap file)

Following the Manual configuration example from above you can do the following:

```
sitemap nuki label="Nuki Smart Lock" {
	Frame label="Channel Unlock" {
		Switch item=Frontdoor_Unlock
	}
	Frame label="Channel Action" {
		Switch item=Frontdoor_Action mappings=[2="Unlock", 7="Unlatch", 1002="LnGo", 1007="LnGoU", 4="Lock"]
	}
	Frame label="Channel Action used to display the lock state" {
		Text item=Frontdoor_Action		label="Lock State [MAP(nukilockstates.map):%s]"
	}
	Frame label="Channel Low Battery" {
		Text item=Frontdoor_LowBattery	label="Low Battery [%s]"
	}
}
```

### Transform Configuration (nukilockstates.map file)

Following the Manual configuration example from above you can do the following:

```
1=Locked
2=Unlocking
3=Unlocked
4=Locking
7=Unlatching
1002=Unlocking (Lock 'n' Go)
1007=Unlatching (Lock 'n' Go)
```



## Troubleshooting, Debugging and Tracing

Switch the loglevel to at least *DEBUG* in the Karaf console to see what's going on.  
To see also the request/response data and more, use loglevel *TRACE*.  
See also [Logging in openHAB](http://docs.openhab.org/administration/logging.html) for general information.  

If you open an issue, please post a full startup TRACE log.  

```
stop org.openhab.binding.nuki
log:set TRACE org.openhab.binding.nuki
start org.openhab.binding.nuki
```

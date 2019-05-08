# NEEO Binding

This binding will discovery and control a NEEO Brain/Remote combination.
NEEO is a smart home solution that includes an IP based remote.
More information can be found at [NEEO](neeo.com) or in the forums at [NEEO Planet](https://planet.neeo.com).
**This binding was not developed by NEEO** - so please don't ask questions on the NEEO forums.

Discovery occurs in three steps:

1. Discover your NEEO Brain.
2. Once you have added a NEEO Brain, each Room will be discovered (which will include all Recipes and Scenarios).
3. Once you have added a NEEO Room, each Device in the Room will be discovered (which will include all Macros for the Device).

The Recipes/Scenarios can then be started or stopped from openHAB or from the remote.
If a Recipe/Scenario is started on the Brain, the status of the Recipe/Scenario will change in openHAB as well.
Likewise, starting a Recipe/Scenario in openHAB will change the status on the Brain/remote.

This binding has been designed to compliment the NEEO Transport (which will expose openHAB Devices to the Brain[s] and expose each Brains Device to the other Brains).

The Room/Scenario/Recipe/Device information is read at startup time.
If you make changes to any Room/Scenario/Recipe/Device, you'll need to delete the item in question and re-discover the item (see discovery section below).

Since this binding allows you to trigger actions on NEEO Devices, this allows you to use the NEEO Brain as a IR solution to openHAB.
In other words, if the NEEO Brain supports a Device over IR - then openHAB can use the NEEO Brain to control that Device regardless if there is an openHAB binding for it or not.

## openHAB Primary Address

This binding will use the primary address defined in openHAB to register itself with the NEEO Brain (allowing the NEEO Brain to forward events back to the binding).
If you change the primary address option, this binding will de-register the old address and re-register the new address with the NEEO Brain.

## Definitions

A NEEO Scenario is a package of Recipes making up the Scenario.
A Scenario is generally related to the buttons on the NEEO remote screen and are named "Watch a TV" or "Watch a Movie", etc.
The Scenario will have one or more Recipes - most commonly two Recipes (a launch and poweroff Recipe).

A NEEO Recipes is a sequence of steps that accomplish a single task (launching a Scenario or turning off a Scenario).
You can view/modify Recipes using the NEEO app.

You can run a Scenario by sending ON to the Scenario status channel and end the Scenario by sending OFF to the Scenario status channel.
Likewise, you can send ON to any "launch" type Recipe status channel to start the Scenario and send ON to the "poweroff" type Recipe status channel to end the Scenario.
Sending OFF to any Recipe status channel does nothing.

A NEEO Device is simply a collection of Macros that the Device supports.

A NEEO Macro is an action that can be performed on the Device.
Actions can be triggered by sending ON to the channel

## Supported Things

* Bridge: NEEO Brain.
This bridge represents a physical NEEO Brain and will contain one to many Rooms within it.

* Bridge: NEEO Room.
Represents a Room on the NEEO Brain.  Only rooms that have atleast one device or one recipe (custom if no devices) will be shown unless the brain configuration option "discoverEmptyRooms" is set to true.

* Thing: NEEO Device.

Represents a Device within the NEEO Room.

## Discover

NEEO Brains will be automatically discovered if MDNS/bonjour/zeroconf is installed on the local machine:

1. On Windows - installing iTunes will install bonjour.
2. On Linux - please install zeroconf (see vendor documentation on how to do that).
3. On Mac - should already be installed.

When you add the NEEO Brain, the Rooms on the Brain will then be auto discovered and placed in the inbox.
When you add a Room, all Devices should be auto discovered and placed in the inbox.
If you remove any discovered thing either from the inbox or from the added things, simply re-trigger a NEEO binding scan to rediscover it.

If you have the Brain both wired and wireless, the Brain will NOT be discovered twice (only once) and which interface is discovered depends on the timing of the beacon discover message (first one wins).
If you discovered the wired first but want to use the wireless (or in the reverse), add the Brain and then modify it's configuration to the IP address you want to use.

If the Brain is not discovered, here is list of the most common issues:

1.  You can generally trigger discovery by starting up the NEEO APP on your mobile device, press MENU->NEEO Brain->Change Brain.
This will generally send out the necessary MDNS broadcast messages to discovery the Brain.
2.  You didn't wait long enough.
I've noticed that it will take up to 5 minutes for the discovery to find the Brain.
3.  Local firewall is blocking the MDNS broadcast messages.
Modify the firewall to allow MDNS packets - typically port 5353 and/or IP address 224.0.0.251
4.  The Brain is on a different subnet.
Unless you have special routing rules, having the Brain on a different subnet than the openHAB instance will prevent discovery.
Either add routing rules or move one of them to the same subnet.
5.  Bug in the MDNS library.
Occasionally a broadcast will be missed and a simple openHAB restart will fix the issue.
6.  Brain isn't reachable.

Ping the Brain's address from the openHAB machine and see if it responds.

If none of the above work, there are a few more things you can try:

1.  Use your local dns-sd command to see if you find the instance ("dns-sd -B _neeo._tcp").
2.  Manually configure the Brain and specify it's IP address.
3.  Look in the issues forum on the NEEO SDK Github - specifically the [Brain Discovery not working](https://github.com/NEEOInc/neeo-sdk/issues/36).

## Forward Actions

The NEEO Brain has the option to forward all actions performed on it to a specific address.
The forward actions will be a JSON string representation:

```
{ "action": "xxx", "actionparameter": "xxx", "recipe": "xxx", "device": "xxx", "room": "xxx" }
```

All parameters are optional (based on what action has been taken) with atleast one of them filled in.
If the Recipe "Watch TV" is launched, the forward action would be:

```
{ "action": "launch", "recipe": "Watch TV" }
```

The NEEO Brain bridge will register itself as the destination for actions and has a trigger channel defined to accept the results of any forward action.
An example rule might look like (for a Brain with an ID of d487672e):

```
rule "NEEO"
    when
        Channel 'neeo:Brain:d487672e:forwardActions' triggered
    then
        logInfo("neeo", "action received")
        
        var data = receivedEvent.getEvent()

        logInfo("neeo", "data: {}", data)

        var String recipe = transform("JSONPATH", "$.recipe", data);
        var String action = transform("JSONPATH", "$.action", data);
        var String device = transform("JSONPATH", "$.device", data);
        var String room = transform("JSONPATH", "$.room", data);
        var String actionparameter = transform("JSONPATH", "$.actionparameter", data);
        
        logInfo("neeo", "action: {}, recipe: {}, device: {}, room: {}, actionparameter: {}", action, recipe, device, room, actionparameter)
end   
```

Since the NEEO Brain ONLY allows a single forward actions URL, the NEEO Brain Bridge can be configured to:

1. Whether to register for forward actions or not.
2. If forward actions has been registered, forward the action on to other URLs for processing.

This will allow you to use other devices that want to consume the forward actions (in addition to openHAB).

## Thing Configuration

The following are the configurations available to each of the bridges/things:

### NEEO Brain

| Name                 | Type    | Required | Default | Description                                                                                                    |
|----------------------|---------|----------|---------|----------------------------------------------------------------------------------------------------------------|
| ipAddress            | string  | Yes      | (None)  | IP Address or host name of the NEEO Brain                                                                      |
| enableForwardActions | boolean | No       | true    | Whether to enable registration of forward actions or not                                                       |
| forwardChain         | string  | No       | blank   | Comma delimited list of other IP addresses to forward actions to                                               |
| discoverEmptyRooms   | boolean | No       | false   | Whether to discover Rooms with no Devices in them                                                              |
| checkStatusInterval  | number  | No       | 10      | The interval (in seconds) to check the status of the Brain. Specify <=0 to disable                             |



### NEEO Room

| Name                 | Type    | Required | Default | Description                                                                                                    |
|----------------------|---------|----------|---------|----------------------------------------------------------------------------------------------------------------|
| roomKey              | string  | Yes      | (None)  | The unique key identifying the Room on the NEEO Brain                                                          |
| refreshPolling       | number  | No       | 120     | The interval (in seconds) to refresh active Scenarios.  Specify <=0 to disable                                 |
| excludeThings        | boolean | No       | true    | Exclude devices that are openHAB things (exposed by the NEEO Transport)                                        |

### NEEO Device

| Name                 | Type    | Required | Default | Description                                                                                                    |
|----------------------|---------|----------|---------|----------------------------------------------------------------------------------------------------------------|
| deviceKey            | string  | Yes      | (None)  | The unique key identifying the Device on the NEEO Brain                                                        |


## Channels

### NEEO Brain

The NEEO Brain has the following channels:
| Channel Type ID    | Read/Write | Item Type    | Description                                                                                |
|--------------------|------------|--------------|--------------------------------------------------------------------------------------------|
| forwardActions     | R          | Trigger      | The forward actions channel                                                                |


The following properties are available at the time of this writing:

| Name          | Description                                                                                                         |
|---------------|---------------------------------------------------------------------------------------------------------------------|
| AirKey        | Unknown (hints at a future airplay feature)                                                                         |
| Version       | The software (not firmware) version of the NEEO Brain                                                               |
| Is Configured | Whether the Brain has gone through it's initial setup (true) or not (false)                                         |
| Label         | Internal label assigned to the Brain                                                                                |
| Last Change   | The time (in milliseconds) that the Brain was last updated (Recipe/Devices/etc change - again not firmware)         |
| Key           | The unique identifier of the Brain                                                                                  |
| Name          | Internal name of the Brain                                                                                          |


### NEEO Room

The NEEO Room is dynamically generated from the Brain.
Each Room will dynamically generate the following channel groups:

1) Each Room will have exactly one "room-state" representing the current state of the Room.
2) Each Room will have zero or more "room-recipe-xxx" (where xxx is the Recipe key) groups representing each Recipe in the Room.
3) Each Room will have zero or more "room-scenario-xxx" (where xxx is the Scenario key) groups representing each Scenario in the Room.

#### Room State Group

The following channels will be in the Room state group:

| Channel Type ID    | Read/Write | Item Type    | Description                                                                                |
|--------------------|------------|--------------|--------------------------------------------------------------------------------------------|
| currentStep*       | R          | trigger      | Displays the current step being executed                                                   |

Current Step will ONLY be triggered if openHAB started the corresponding recipe (or scenario).  
If the NEEO Remote or NEEO App starts the recipe or scenario, the currentStep will never be triggered. 

The current step is ONLY communicated from the Brain to the device that started the Recipe/Scenario.
If the remote started the Recipe/Scenario, it will show the current step but openHAB will not be notified.
Likewise if openHAB starts the Recipe/scenario, the remote will not be notified of the current step (although it will know the Recipe/Scenario became active).

#### Room Recipe Group

Each Room Recipe group will have the following channels:

| Channel Type ID    | Read/Write | Item Type    | Description                                                                           |
|--------------------|------------|--------------|---------------------------------------------------------------------------------------|
| name               | R          | String       | The name of the Recipe                                                                |
| type*              | R          | String       | The type of Recipe                                                                    |
| enabled            | R          | Switch       | Whether the Recipe is enabled or not                                                  |
| status             | RW         | Switch       | Whether the Recipe is currently running (you can start/stop Recipes with this switch) |

The list of types is unknown at this time and the only ones I know of are "launch" and "poweroff".

Simply view the Recipe channel prior to using the type in a rule.

#### Room Scenario Group

Each Scenario group will have the following channels:

| Channel Type ID    | Read/Write | Item Type    | Description                                                                              |
|--------------------|------------|--------------|----------------------------------------------------------------------------------------- |
| name               | R          | String       | The name of the Scenario                                                                 |
| configured         | R          | Switch       | Whether the Scenario is configured (or waiting additional input)                         |
| status             | RW         | Switch       | Whether the Scenario is currently running (you can start/stop Scenarios with this switch |

### NEEO Device

The NEEO Device is dynamically generated from the Brain.
Each Device will have a single group (Macros) and that group will contain one or more channels defined by the Macro key (as defined by the NEEO Brain):

| Channel Type ID    | Read/Write | Item Type    | Description                                                                              |
|--------------------|------------|--------------|----------------------------------------------------------------------------------------- |
| (macro key)        | RW         | Switch       | Send ON to trigger Macro, resets to false afterwards                                     |

## Full Example

.things

```
neeo:brain:home                     [ ipAddress="192.168.1.24" ]
neeo:room:attic   (neeo:brain:home) [ roomKey="6277847230179180544" ]
neeo:device:tv    (neeo:room:attic) [ deviceKey="6343464057630097408" ]
```

.items

```
String Attic_RecipeName          "Recipe Name [%s]"     { channel="neeo:room-6277847230179180544:attic:room:recipe#name-6277847545657950208" }
Switch Attic_RecipeEnabled       "Recipe Enabled"       { channel="neeo:room-6277847230179180544:attic:room:recipe#enabled-6277847545657950208" }
Switch Attic_RecipeStatus        "Running"              { channel="neeo:room-6277847230179180544:attic:room:recipe#status-6277847545657950208" }
String Attic_ScenarioName        "Scenario Name [%s]"   { channel="neeo:room-6277847230179180544:attic:room:scenario#name-6277847545657950208" }
Switch Attic_ScenarioStatus      "Running"              { channel="neeo:room-6277847230179180544:attic:room:scenario#status-6277847545657950208" }
Switch Attic_TvInput1            "Input1"               { channel="neeo:device:tv:macros#status-6343464057651068928" }
```

.sitemap

```
sitemap demo label="NEEO" {
    Frame label="Attic" {
        Text item=Attic_RecipeName
        Switch item=Attic_RecipeStatus
        Switch item=Attic_TvInput1
    }
}
```

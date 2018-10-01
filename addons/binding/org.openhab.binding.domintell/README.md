# Domintell Binding

This binding integrates [Domintell](https://www.domintell.com/en/) home automation system and openHAB using Light protocol provided by DETH02 ethernet communication module. 

## Supported Things

### DBIR01(BIR)

Output module with 8 bipolar relays. It supports 8 Switch type channels.

### DMR01(DMR)

Output module with 5 monopolar relays. It supports 5 Switch type channels.

### DISM08(IS8)/DISM04(IS4)

Input module with 8/4 free of potential contacts. 
They support 8/4 Contact type channels (OPEN/CLOSE) and one command channel with the following string commands:

* **ShortPush-[1..8]** to simulate short push events
* **LongPush-[1..8]** to simulate long push events
  
### DTEM01(TE1)/DTEM02(TE2)

Thermostat modules. 
They support thermostat channel groups with the following channels:

* **heating**
  * **presetValue** - Preset temperature (RW)
  * **currentValue** - Measured temperature (RO)
  * **presetValue** - Profile temperature configured in Domintell system (RO)
  * **mode** - Temperature mode [AUTO|ABSENCE|COMFORT|FROST] (RW)
* **cooling**
  * **presetValue** - Preset temperature (RW)
  * **currentValue** - Measured temperature (RO)
  * **presetValue** - Profile temperature configured in Domintell system (RO)
  * **mode** - Regulation mode [OFF|HEATING|COOLING|AUTO] (RW)

### DDIM01(DIM) Dimmer

It supports 8 system.brightness channels.

### DOUT10V02(D10)

0/1-10V dimmer module with single system.brightness channel support.

### Push buttons

Listed push buttons support predefined number of Contact type channels:

* **DPBL0x(B8x)** - push button where x:1, 2, 4, 6
* **DPBR02(BRx)** - push button where x:2, 4, 6
* **DPB0x(BUx)** - push button where x:1, 2, 4, 6

### User Defined Variables 

Domintell allows definition of boolean and numeric user variables. 
All variables are identified by **VAR** module type and sequence number. 
In order to control their configuration the binding collects all variables under **Domintell Variables** thing where each channel represents a single variable.

## Configuration

### Discovery

Binding supports full auto-discovery feature. 
The bridge discovery is initiated after bundle start and adds a bridge to the inbox.
After configuring the host/port for bridge the binding will try to connect to Domintell DETH02 module to discover the Domintell full installation at every inbox search.
All supported modules listed above will be added to the inbox when the first status message arrives from the module.

### Bridge

The only required configuration for this binding are the bridge level **host** and **port** parameters. 
For this the Domintell DETH02 should use:

* Static IP address
* Session timeout should be set at least to 2 min
* Exclusive session should be false
* No NTP is needed
* Clear the password

### Modules

In a Domintell installation each module has it's type and unique serial number:

|Parameter|Type|Description|
|---|---|---|
|Module Type|String|Length is always 3 characters. Check module list above for available values.|
|Serial Number| Decimal | Unique serial number of the Domintell module| 

### Contact Channels

#### Inverting

Contact type channels support value inverting to cover NO and NC contacts.

#### Value Reset

If this value is larger than 0 the binding resets the contact state to its base (OPEN state) and triggers a value update from Domintell. 
This can be used to reset the expiration timer of an output control (e.g. a light) that is turned on on CLOSED events.

## Full Example

### .things
    
    Bridge domintell:bridge:DETH02 "Domintell Bridge" \[address="10.200.0.6", port=17481\] {
        //Thermostats
        Thing moduleTEx 1 "Thermostate AB9/2745" [moduleType="TE1", serialNumber="2745"]   //groundfloor
        Thing moduleTEx 2 "Thermostate C8A/3258" [moduleType="TE2", serialNumber="3258"]   //first floor
    
        //Relays
        Thing moduleBIR 3 "Relay 2C37/11319" [moduleType="BIR", serialNumber="11319"]

        //Contacts
        Thing moduleIS8 4 "Input module 36D3/14035" [moduleType="IS8", serialNumber="14035"]
    }

### .items 

    //temperature
    Number domGroundFloorTemp "Ground floor temperature" {item="domintell:moduleTEx:DETH02:1:heating#currentValue"} 
    
    //relays
    Switch lPantry "Pantry light" {item="domintell:moduleBIR:DETH02:3:4"}
    Switch lKitchen "Kitchen light" {item="domintell:moduleBIR:DETH02:3:8"}

    //contacts
    Contact msKitchen "Kitchen motion" {item="domintell:moduleIS8:DETH02:4:5"}
    Contact msSmallBathroom "Small bathroom motion" {item="domintell:moduleIS8:DETH02:4:6"}

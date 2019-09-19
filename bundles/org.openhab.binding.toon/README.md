# Toon Binding

The Toon bindings shows among others current room temperature, setpoint, energy and gas usage information.
It can control the setpoint and current program.
Connected smart plugs can also be controlled.

## Supported Things

### Toon Webaccount

Before the binding can be used, a Toon Webaccount must be added.
This needs to be done manually. Select `Toon Webaccount`, and enter your username and password.
Afterwards start discovery so your display and connected plugs are discovered.

### Toon display unit

The display unit holds all channels that represent current room temperature, setpoint, setpoint mode, gas and energy meter readings.

### Toon plug

A Toon plug represents a connected wall plug that can be controlled via Toon.

## Discovery

Once the binding is authorized, and the `Toon Webaccount` is added, you can start the discovery.
This will find your Toon Display and put it in the Inbox.

Currently only the display and plugs are discovered. So no Alarms or Hue lights will be discovered.

## Binding Configuration



## Thing Configuration

demo.things

```
toon:toonapi:toontest [ username="xxxx", password="yyyy" ]
```

## Items

demo.items:

```
Group Toon
// Display items
Number ToonTemp         (Toon) {channel="toon:main:toontest:zzzz:Temperature"}
Number ToonSetpoint     (Toon) {channel="toon:main:toontest:zzzz:Setpoint"}
Number ToonSetpointMode (Toon) {channel="toon:main:toontest:zzzz:SetpointMode"}

Number Gas              (Toon) {channel="toon:main:toontest:zzzz:GasMeterReading"}
Number Power            (Toon) {channel="toon:main:toontest:zzzz:PowerMeterReading"}
Number PowerLow         (Toon) {channel="toon:main:toontest:zzzz:PowerMeterReadingLow"}
Number PowerConsumption (Toon) {channel="toon:main:toontest:zzzz:PowerConsumption"}

Number Modulation       (Toon) {channel="toon:main:toontest:zzzz:ModulationLevel"}
Switch Heater           (Toon) {channel="toon:main:toontest:zzzz:Heating"}
Switch Tapwater         (Toon) {channel="toon:main:toontest:zzzz:Tapwater"}
Switch PreHeat          (Toon) {channel="toon:main:toontest:zzzz:Preheat"}

// Plug items
Switch Plug             (Toon) {channel="toon:plug:toontest:pppp:switch_binary"}
Number PlugConsumption  (Toon) {channel="toon:plug:toontest:pppp:PowerConsumption"}
```

Replace zzzz and pppp with the discovered values.


## Sitemaps

demo.sitemaps

```
    Frame {
        Group item=Toon
        Setpoint item=ToonSetpoint minValue=16 maxValue=28 step=0.5
        Selection item=ToonSetpointMode label="Toon Program Selection" mappings=[0=Comfort, 1=Active, 2=Sleep, 3=Away]
    }
```

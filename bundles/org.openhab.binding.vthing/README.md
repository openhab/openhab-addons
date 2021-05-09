# vthing Binding

This binding provides virtual things, a thing without a corresponding hardware device.
A virtual thing is useful to learn and play with OpenHAB.

## Supported Things

Currently a virtual lamp is supported.

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| state    | Switch | on off switch                |
| color    | Color  | color                        |
| ontime   | String | on time                      |

## Full Example

### demo.things:

```
Thing vthing:vlamp:vlamp1
```

### demo.items:

```
Switch vlamp1_onoff  {channel="vthing:vlamp:vlamp1:state"}
Color  vlamp1_color  {channel="vthing:vlamp:vlamp1:color"}
String vlamp1_ontime {channel="vthing:vlamp:vlamp1:ontime"}
```

## Webview

The color of the virtual lamp can be visualized with a web view 

```
Webview url="/vlamp/lampcolor.html?thingUID=vthing:vlamp:vlamp1"
```

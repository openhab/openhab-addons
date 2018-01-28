# Brumberg vitaLED Binding

This binding integrates Brumberg vitaLED via the Brumberg vitaLED LAN master that communicates via DMX with the Brumberg vitaLED bulbs. The LAN master supports 8 zones. Each zone can consists of 6 vitaLED bulbs.  

## Supported Things

This binding supports the Brumberg vitaLED LAN master (Brumberg Article number 18308000).

## Discovery

There is no auto-discovery feature available for this binding.

## Thing Configuration

The Brumberg vitaLED thing requires the IP address and the port to access it on. Optional you can set a refresh interval in seconds. The refresh interval states how often a refresh shall occur in seconds, that reads the current vitaLED setting of each zone from the Brumberg vitaLED LAN master.

```text
vitaled:vitaled:demo [ ipAddress="192.168.0.40", port=80, refreshInterval=60 ]
```

## Channels

The Brumberg vitaLED supports the following channels:

| Channel Type ID                | Item Type | Description                                                                                                      |
|--------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| zone1#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |
| zone1#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone1#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone1#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone1#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone1#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone1#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone1#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone1#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone1#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone1#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone1#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone1#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone1#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone1#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone1#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone1#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone2#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone2#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone2#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone2#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone2#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone2#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone2#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone2#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone2#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone2#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone2#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone2#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone2#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone2#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone2#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone2#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone2#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone3#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone3#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone3#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone3#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone3#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone3#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone3#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone3#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone3#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone3#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone3#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone3#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone3#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone3#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone3#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone3#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone3#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone4#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone4#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone4#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone4#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone4#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone4#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone4#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone4#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone4#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone4#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone4#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone4#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone4#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone4#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone4#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone4#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone4#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone5#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone5#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone5#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone5#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone5#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone5#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone5#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone5#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone5#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone5#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone5#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone5#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone5#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone5#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone5#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone5#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone5#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone6#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone6#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone6#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone6#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone6#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone6#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone6#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone6#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone6#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone6#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone6#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone6#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone6#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone6#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone6#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone6#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone6#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone7#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone7#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone7#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone7#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone7#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone7#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone7#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone7#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone7#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone7#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone7#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone7#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone7#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone7#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone7#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone7#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone7#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |
| zone8#achromaticLight          | Number    | Achromatic Light supports values between 2000 and 10000 K in 50 K steps.                                         |                                                                                                                                    
| zone8#intensity                | Number    | The light intensity between 0 and 100 %.                                                                         |
| zone8#red                      | Number    | The red light value between 0 and 100 %.                                                                         |
| zone8#green                    | Number    | The green light value between 0 and 100 %.                                                                       |
| zone8#blue                     | Number    | The blue light value between 0 and 100 %.                                                                        |
| zone8#white                    | Number    | The white light value between 0 and 100 %.                                                                       |
| zone8#colourSaturation         | Number    | The colour saturation between 0 and 100 %.                                                                       |
| zone8#speed                    | Number    | The speed between 0 and 100 %.                                                                                   |
| zone8#colourGradientIntensity  | Number    | The light intensity for the colour gradient between 0 and 100 %.                                                 |
| zone8#xCoord                   | Number    | The x-coordinate in the colour triangle.                                                                         |
| zone8#yCoord                   | Number    | The y-coordinate in the colour triangle.                                                                         |
| zone8#scene1                   | Switch    | Activates scene 1. Only ON makes sense.                                                                          |
| zone8#scene2                   | Switch    | Activates scene 2. Only ON makes sense.                                                                          |
| zone8#scene3                   | Switch    | Activates scene 3. Only ON makes sense.                                                                          |
| zone8#scene4                   | Switch    | Activates scene 4. Only ON makes sense.                                                                          |
| zone8#scene5                   | Switch    | Activates scene 5. Only ON makes sense.                                                                          |
| zone8#scene6                   | Switch    | Activates scene 6. Only ON makes sense.                                                                          |


## Full Example

The following example show the configuration for zone 1.

## Item Configuration

demo.items

```java
// vitaLED
Number zone1_achromaticLight { channel = "vitaled:vitaled:demo:zone1#achromaticLight" }
Number zone1_intensity { channel = "vitaled:vitaled:demo:zone1#intensity" }
Number zone1_red { channel = "vitaled:vitaled:demo:zone1#red" }
Number zone1_green { channel = "vitaled:vitaled:demo:zone1#green" }
Number zone1_blue { channel = "vitaled:vitaled:demo:zone1#blue" }
Number zone1_white { channel = "vitaled:vitaled:demo:zone1#white" }
Number zone1_colour_saturation { channel = "vitaled:vitaled:demo:zone1#colourSaturation" }
Number zone1_speed { channel = "vitaled:vitaled:demo:zone1#speed" }
Number zone1_colourGradientIntensity { channel = "vitaled:vitaled:demo:zone1#colourGradientIntensity" }
Number zone1_xCoord { channel = "vitaled:vitaled:demo:zone1#xCoord" }
Number zone1_yCoord { channel = "vitaled:vitaled:demo:zone1#yCoord" }
Switch zone1_scene1 { channel = "vitaled:vitaled:demo:zone1#scene1" }
Switch zone1_scene2 { channel = "vitaled:vitaled:demo:zone1#scene2" }
Switch zone1_scene3 { channel = "vitaled:vitaled:demo:zone1#scene3" }
Switch zone1_scene4 { channel = "vitaled:vitaled:demo:zone1#scene4" }
Switch zone1_scene5 { channel = "vitaled:vitaled:demo:zone1#scene5" }
Switch zone1_scene6 { channel = "vitaled:vitaled:demo:zone1#scene6" }
```

## Sitemap Configuration

demo.sitemap

```perl
sitemap demo label="Brumberg vitaLED"
{
        // intensity is used for all modes except for colour gradients
        Slider item=zone1_intensity label="Intensity" icon="none"
        // achromatic light
        Setpoint item=zone1_achromaticLight label="Achromtaic Light" icon="none" minValue=2000 maxValue=10000 step=50        
        // chromatic light
        Slider item=zone1_red label="Red" icon="none"
        Slider item=zone1_green label="Green" icon="none"
        Slider item=zone1_blue label="Blue" icon="none"
        Slider item=zone1_white label="White" icon="none"
        // colour gradients
        Slider item=zone1_colour_saturation label="Colour saturation" icon="none"
        Slider item=zone1_speed label="Speed" icon="none"
        Slider item=zone1_colourGradientIntensity label="Colour gradient intensity" icon="none"
        // scenes
        Switch item=zone1_scene1 label="Scene 1" icon="none" mappings=[ON="activate"]
        Switch item=zone1_scene2 label="Scene 2" icon="none" mappings=[ON="activate"]
        Switch item=zone1_scene3 label="Scene 3" icon="none" mappings=[ON="activate"]
        Switch item=zone1_scene4 label="Scene 4" icon="none" mappings=[ON="activate"]
        Switch item=zone1_scene5 label="Scene 5" icon="none" mappings=[ON="activate"]
        Switch item=zone1_scene6 label="Scene 6" icon="none" mappings=[ON="activate"]
}
```

## Current limitations

The binding has in the current version the following limitations.
 
Only none is supported as authentication yet. Do not use DHCP client, use instead a fix IP address. 
Groups are also not supported, but this can be handled via openHAB rule. Colour management and vital gradient are not yet supported.

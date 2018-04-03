---
layout: documentation
---

{% include base.html %}

# Bose SoundTouch10 Binding

The binding integrates Bose SoundTouch10 devices. It is explicitly created for the Bose SoundTouch10 device. Other SoundTouch devices may work too, but are not completely supported by this binding.

The binding communicates via the REST API of the devices and receives message via websockets.

You can control the power, volume, bass, presets (including bluetooth and aux). It is also possible to see the name of the current preset.

| Configuration Parameter | Type    | Description  | 
|-------------|--------|-----------------------------|
| deviceUrl | text | url for communication with Bose SoundTouch10 speaker e.g. http://192.168.0.2:8090 | 


## Supported Things

The binding currently supports only the Bose SoundTouch10 speaker.

## Discovery

Bose SoundTouch10 speaker are discovered using MDNS, no authentication is required.

## Thing Configuration

Bose SoundTouch10 speakers can be added manually. The only configuration parameter is the `ipAddress:port`.

## Channels

| Channel Type ID | Item Type    | Description  | 
|-------------|--------|-----------------------------|
| power | Switch | Turn speaker on and off |
| volume | Dimmer | control the volume of the speaker |
| bass | Number | control the bass of the speaker |
| source | String | source (preset, bluetooth, aux) to be played |
| now_playing | String | name of the source |
| control | Player | Player control; currently only supports play/pause |

## Full Example


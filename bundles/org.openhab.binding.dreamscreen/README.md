# DreamScreen Binding

This binding integrates the DreamScreen TV light system

## Supported Things

| Thing UID  | Description                 |
|------------|-----------------------------|
| hd         | DreamScreen HD Device       |
| 4k         | DreamScreen 4k Device       |
| sidekick   | DreamScreen Sidekick Device |

Note: Sidekick support is primarily and not verified.

## Discovery

DreamScren devices are discovered automatically on the local network (The control protocol is UDP-based and runs on port 8888).
Configure and test your DreamScreen using the manufacture's App before setting up the openHAB integration.

## Channels

Various function of the DreamScreen could be controlled and status is reported.

| Channel  | Type   | Description                                            |
|----------|--------|--------------------------------------------------------|
| power    | Switch | Switch power of the device on/off                      |
| input    | String | HD + 4k: Select input channel: hdmi1, hdmi2 or jdmi3   |
| mode     | String | Switch the display mode: video, music or ambient       |
| scene    | String | Switch the ambient display scene: one of color, random, fireside, twinkle, ocean, rainbow, july_4th,  holiday, pop, forest |
| color    | Color  | Select the active color to use with the Color scene    |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._


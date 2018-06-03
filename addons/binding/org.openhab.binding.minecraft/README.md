# Minecraft Binding

This binding integrates Minecraft with openHAB through the [spigot plugin](https://github.com/ibaton/bukkit-openhab-plugin/releases/download/1.5/OHMinecraft.jar) ([sources](https://github.com/ibaton/bukkit-openhab-plugin/tree/master)).

The binding allows reading of server and player data.
It furthermore keeps track of redstone power going below signs and links them to Switch items.

## Youtube Videos

[![Minecraft Binding 1](http://img.youtube.com/vi/TdvkTorzkXU/0.jpg)](https://youtu.be/TdvkTorzkXU)

[![Minecraft Binding 2](http://img.youtube.com/vi/zAyNWmr7byE/0.jpg)](https://youtu.be/zAyNWmr7byE)

## Discovery

The Minecraft binding automatically finds all Minecraft servers running [this plugin](https://github.com/ibaton/bukkit-openhab-plugin/releases/download/1.9/OHMinecraft.jar) on the local network.
Servers can be added manually if they are not found automatically.

## Channels

Depending on the thing type, different channels are provided:

### Server

| Channel Type ID | Item Type | Description                             |
|-----------------|-----------|-----------------------------------------|
| name            | String    | Name of Minecraft server                |
| online          | Switch    | Online status                           |
| bukkitVersion   | String    | The bukkit version running on server    |
| version         | String    | The Minecraft version running on server |
| players         | Number    | The number of players on server         |
| maxPlayers      | Number    | The maximum number of players on server |


### Player

| Channel Type ID            | Item Type | Description                                                |
|----------------------------|-----------|------------------------------------------------------------|
| playerName                 | String    | The name of the player                                     |
| playerOnline               | Switch    | Is the player connected to the server                      |
| playerLevel                | Number    | The current level of the player                            |
| playerTotalExperience      | Number    | The total experience of the player                         |
| playerExperiencePercentage | Number    | The percentage of the experience bar filled for next level |
| playerHealth               | Number    | The health of the player                                   |
| playerWalkSpeed            | Number    | The speed of the player                                    |
| playerLocation             | Location  | The player location                                        |
| playerGameMode             | Number    | The players game mode                                      |


### Sign

| Channel Type ID | Item Type | Description                                  |
|-----------------|-----------|----------------------------------------------|
| signActive      | Switch    | Does the sign have powered redstone below it |


Active switch (Controllable from openHAB)
<a href="https://drive.google.com/uc?export=view&id=0B3UO0c11-Q6hMkNZSjJidGk4b28"><img src="https://drive.google.com/uc?export=view&id=0B3UO0c11-Q6hMkNZSjJidGk4b28" style="width: 500px; max-width: 100%; height: auto" title="Click for the larger version." /></a>

Passive sensor
<a href="https://drive.google.com/uc?export=view&id=0B3UO0c11-Q6hUG1wd3h0MDUzUzQ"><img src="https://drive.google.com/uc?export=view&id=0B3UO0c11-Q6hUG1wd3h0MDUzUzQ" style="width: 500px; max-width: 100%; height: auto" title="Click for the larger version." /></a>

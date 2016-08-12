# Minecraft-Binding

Binding integrating Minecraft with openHAB through the spigot plugin 
[here](https://github.com/ibaton/bukkit-openhab-plugin/releases/download/1.5/OHMinecraft.jar),
[source](https://github.com/ibaton/bukkit-openhab-plugin/tree/master).
The binding allows reading of server and player data. The binding also keeps track of redstone power going below signs and links them to Switch item.

## Youtube Video

https://www.youtube.com/watch?v=TdvkTorzkXU&feature=youtu.be

## Discovery

The Minecraft binding automatically finds all Minecraft servers running [this plugin](https://github.com/ibaton/bukkit-openhab-plugin/releases/download/1.5/OHMinecraft.jar) on the local network. Servers can be added manually if it isn't found automatically. 

## Channels

Depending on the thing it supports different Channels

### Server

| Channel Type ID | Item Type    | Description  |
|------------------|------------------------|--------------|
| name | String | Name of Minecraft server |
| online | Switch | Servers online status |
| bukkitVersion | String | The bukkit version running on server |
| version | String | The Minecraft version running on server |
| players | Number | The number of players on server |
| maxPlayers | Number | The maximum number of players on server |

### Player

| Channel Type ID | Item Type    | Description  |
|------------------|------------------------|--------------|
| playerName | String | The name of the player |
| playerOnline | Switch | Is player connected to the server |
| playerLevel | Number | The current level of the player |
| playerTotalExperience | Number | The total experience of player |
| playerExperiencePercentage | Number | The percentage of the experience bar filled for next level |
| playerHealth | Number | The health of the player |
| playerWalkSpeed | Number | The speed of the player |
| playerLocation | Location | The player location |

### Sign

| Channel Type ID | Item Type    | Description  |
|------------------|------------------------|--------------|
| signActive | Switch | Does sign have powered redstone below it |

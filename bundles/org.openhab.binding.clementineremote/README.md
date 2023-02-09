# Clementine Remote Binding

<img style="float: right; margin: 0 0 5px 5px" src="doc/screenshot.png" alt="Screenshot" />
This binding brings the benefits of over-the-network control of the [Clementine-Player] to openHAB!

It allows control of playback, position and volume and allows display of various title information:

* Artist
* Album
* Title
* current position within track
* Album cover

## Discovery

This thing does not provide auto-discovery.
You will have to manually configure clementine to allow remote connections.

Detailed instructions how to configure Clementine's remote functions are provided here:

https://github.com/clementine-player/Android-Remote/wiki/How-to-use-the-Android-Remote

And you will have to manually configure the Thing instances to talk to your player:

## Thing Configuration

Configuring the connection to Clementine is quite easy:

Normally you only need to set the hostname your Clementine player is running at.
If you set a password in the Clementine configuration, you will have to provide it to the configuration, too.

| Name     | Type    | Description                                                       | Default | Required | Advanced |
|----------|---------|-------------------------------------------------------------------|---------|----------|----------|
| hostname | text    | Hostname or IP address of the device                              | N/A     | yes      | no       |
| password | text    | Password to access the device                                     | N/A     | no       | no       |
| port     | integer | Port at which Clementine is listening for remote control commands | 5500    | yes      | no       |

## Channels

The binding provides the following channels:

| Channel          | Type        | Read/Write  | Description                                  |
|------------------|-------------|-------------|----------------------------------------------|
| album            | String      | read-only   | Album the currently playing song belongs to  |
| artist           | String      | read-only   | Artist of the currently playing title        |
| cover            | Image       | read-only   | Cover of the current album                   |
| playback-control | String      | read/write  | Common control of playback and position      |
| position         | Number:Time | read-only   | Current position within the playing track    |
| state            | String      | read-only   | Current state of Clementine player           |
| title            | String      | read-only   | Name of the currently playing track          |
| track            | String      | read-only   | Number of the title within the current album |
| volume-control   | Dommer      | read/write  | Playback volume                              |



## Full Example

### Thing

#### YAML
```yaml
UID: clementineremote:clementine:myplayer
label: "Music Player"
thingTypeUID: clementineremote:clementine
configuration:
  port: 5500
  hostname: 192.168.1.158
  authCode: 123456
```

#### demo.things:

```
Thing clementineremote:clementine:myplayer "Music Player" @ "Living Room" [ hostname="192.168.1.158", authCode="12345", port="5500" ]
```

### Widget

You may find a nice widget to use with this plugin at [Widget.md]

[Clementine-Player]: https://www.clementine-player.org/
[Links]: doc/useful%20links.md
[Widget.md]: Widget.md
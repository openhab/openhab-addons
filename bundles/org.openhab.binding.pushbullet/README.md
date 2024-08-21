# Pushbullet Binding

The Pushbullet binding allows you to notify iOS, Android & Windows 10 Phone & Desktop devices of a message using the Pushbullet API web service.

## Supported Things

This binding supports a generic "bot" which is a representation of the client.

## Discovery

This binding provides no discovery.
The desired bots must be configured manually or via a things file.

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Thing Configuration

### Bot (`bot`)

The bot thing is used to send messages to other recipients.
It has the following parameters:

| Config     |  Description                                                     | Required | Advanced |
|------------|------------------------------------------------------------------|----------|----------|
| token      | Pushbullet [API token](#obtaining-an-api-key) to send to devices | Yes      | False    |
| name       | Explicit Name, for later use when the bot can receive messages   | No       | True     |
| apiUrlBase | Address of own Pushbullet server, for testing purposes           | No       | True     |

```java
Thing pushbullet:bot:r2d2 "R2D2" @ "Somewhere" [ token = "verysecretwonttellyou" ]

```

## Channels

| Channel ID | Channel Description                             | Supported item type  | Advanced |
|------------|-------------------------------------------------|----------------------|----------|
| recipient  | for later use when the bot can receive messages | String               | False    |
| title      | for later use when the bot can receive messages | String               | False    |
| message    | for later use when the bot can receive messages | String               | False    |

## Rule Action

This binding includes rule actions for sending notes.
Two different actions available:

- `sendPushbulletNote(String recipient, String messsage)`
- `sendPushbulletNote(String recipient, String title, String messsage)`

Since there is a separate rule action instance for each `bot` thing, this needs to be retrieved through `getActions(scope, thingUID)`.
The first parameter always has to be `pushbullet` and the second is the full Thing UID of the bot that should be used.
Once this action instance is retrieved, you can invoke the action method on it.

The recipient can either be an email address, a channel tag or `null`.
If it is not specified or properly formatted, the note will be broadcast to all of the user account's devices.

Examples:

```java
val actions = getActions("pushbullet", "pushbullet:bot:r2d2")
val result = actions.sendPushbulletNote("someone@example.com", "R2D2 talks here...", "This is the pushed note.")
```

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

pushbullet.things:

```java
Thing pushbullet:bot:r2d2 "R2D2" @ "Somewhere" [ token = "verysecretwonttellyou" ]

```

pushbullet.items

```java
Switch Pushbullet_R2D2_Button  "Pushbullet Action bot R2D2"
```

pushbullet.sitemap

```java
sitemap pushbullet label="Pushbullet"
{
    Switch item=Pushbullet_R2D2_Button
}
```

pushbullet.rules

```java
rule "Pushbullet R2D2 changed"
when
    Item Pushbullet_R2D2_Button changed
then
    logInfo(filename, "Button R2D2 changed - OH2...")

    if (Pushbullet_R2D2_Button.state == ON)
    {
        sendCommand(Pushbullet_R2D2_Button, OFF)

        val actions = getActions("pushbullet", "pushbullet:bot:r2d2")
        logInfo(filename, "Actions for 'R2D2' are: " + actions)

        if (actions != null)
        {
            val result = actions.sendPushbulletNote("someone@example.com", "Title R2D2 OH2", "This has been sent by the new R2D2 bot")
            logInfo(filename, "Result of send action is: " + result)
        }
    }
end
```

## Creating an account for your bot(s)

The pushbullet accounts are bound to either Google or Facebook accounts.

- Create a bot account with either Facebook or Google
- Go to "<https://www.pushbullet.com/>"
- Chose to either "Sign up with Google" or "Sign up with Facebook".
- Complete the signup process as guided by the pushbullet web site.
- Continue with "Obtaining an API key".

## Obtaining an API key

The API keys are bound to the pushbullet account.

- Go to the pushbullet site.
- Log in with either your personal account or the one you created for your bot.
- Go to "<https://www.pushbullet.com/#settings/account>"
- Click on "Create Access Token".
- Copy the token created on the site.

You must at least provide an API token (Private or Alias Key from Pushbullet.com) and a message in some manner before a message can be pushed.
All other parameters are optional.
If you use an alias key, the parameters (device, icon, sound, vibration) are overwritten by the alias setting on pushbullet.

## Rate limits

As of 2019, free accounts have a limit of 100 pushes per month.
This action does not evaluate the rate limiting headers though.

## Translation

This project is being translated on transifex.
If you want to help, please join the project at the URL:

- <https://www.transifex.com/hakan42/openhab-binding-pushbullet/dashboard/>

## Libraries

This action has been written without using libraries as jpushbullet or jpushbullet2.
Both of those libraries use various libraries themselves which makes integrating them into openHAB a challenge.

## pushbullet API

- <https://docs.pushbullet.com/>
- <https://docs.pushbullet.com/#push-limit>

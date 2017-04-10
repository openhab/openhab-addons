# MailServer Binding

This binding allows OpenHab to become a simple SMTP server.

## Supported Things

Currently, the binding supports a single type of Thing, the ```smtpserver``` Thing.

## Binding Configuration

The binding requires no special configuration.

## Thing Configuration

The `smtpserver` Thing supports multiple different channels:

For each desired server a separate Thing has to be defined.

```
mailserver:smtpserver:<customservername> [ port=<PortToListenOn> ]
```

## Channels

All Things support the following channels.

| Channel Type ID      | Item Type    | Description  |
|----------------------|--------------|------------------- |
| receivedmessagecount | String       | The count of messages the server has received since being started |
| messagebody          | String       | The raw body of the last message received |
| openhabvalue         | String       | The data in between the <openhabvalue></openhabvalue> xml tags |
| openhabcommand       | String       | The data in between the <openhabcommand></openhabcommand> xml tags |

## Full Example

**demo.things**

```
mailserver:smtpserver:<customservername> [ port=<PortToListenOn> ]
mailserver:smtpserver:server [ port=25 ]
```

**demo.items**

```
Switch <custom item name>          "<visible text>"      { channel="mailserver:smtpserver:<customservername>:<channel>" }
Switch smtp_command                "Command"             { channel="mailserver:smtpserver:server:openhabcommand" }
```
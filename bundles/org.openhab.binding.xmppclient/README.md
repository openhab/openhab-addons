# XMPPClient Binding

XMPPClient binding provides support for sending and receiving XMPP (Jabber) messages.

## Supported Things

xmppBridge - Basic XMPP (Jabber) client thing, that can send and recieve messages.

## Thing Configuration

**xmppBridge** parameters:

| Name   |      Label      |  Description |
|----------|-------------|------|
| host | Server Hostname/IP | The IP/Hostname of the XMPP server |
| port | XMPP server Port | The typical port is 5222 |
| username | Username | The XMPP username |
| password | Password | The XMPP password |


## Channels

**publishTrigger** parameters:

| Name   |      Label      |  Description |
|----------|-------------|------|
| payload | Payload condition | An optional condition on the value |
| separator | Separator character | The trigger channel payload usually only contains the received text. If you define a separator character, for example '#', the sender UID and received text will be in the trigger channel payload. For example: pavel@example.com#My Message Text |

## Example Rules

Send message:

```
rule "Leak detected"
when
    Item Xi_Leak changed
then
    if(Xi_Leak.state == ON) {
        val actions = getActions("xmpp","xmppclient:xmppBridge:xmpp")
        actions.publishXMPP("pavel@example.com","Warning! Leak detected!")
    }
end
```

Receive and process message:

```
rule "Turn off all lights without separator"
when
    Channel "xmppclient:xmppBridge:xmpp:xmpp_command" triggered
then
    var actionName = receivedEvent.getEvent()
    if(actionName.toLowerCase() == "turn off lights") {
        Group_Light_Home_All.sendCommand(OFF)
    }
end

rule "Turn off all lights with separator and reply"
when
    Channel "xmppclient:xmppBridge:xmpp:xmpp_command" triggered
then
    var actionName = receivedEvent.getEvent().split("#")
    if(actionName.get(1).toLowerCase() == "turn off lights") {
        Group_Light_Home_All.sendCommand(OFF)

        val actions = getActions("xmpp","xmppclient:xmppBridge:xmpp")
        actions.publishXMPP(actionName.get(0),"All lights was turned off")
    }
end
```
# XMPPClient Binding

The XMPPClient binding provides support for sending and receiving XMPP (Jabber) messages.

## Supported Things

- xmppBridge — XMPP (Jabber) client bridge that can send and receive messages. You can add publish trigger channels to it.

## Thing Configuration

Sample configurations:

```java
Bridge xmppclient:xmppBridge:xmpp "XMPP Client" [ username="openhab", domain="example.com", password="********" ] {
    Channels:
        Trigger String : xmpp_command [ separator="##" ]
}
```

```java
Bridge xmppclient:xmppBridge:xmpp "XMPP Client" [ host="xmpp.example.com", port=7222, username="openhab", domain="example.com", password="********" ] {
    Channels:
        Trigger String : xmpp_command [ separator="##" ]
}
```

**xmppBridge** parameters:

| Name         | Label              | Description                                                        | Required | Default value         |
|--------------|--------------------|--------------------------------------------------------------------|----------|-----------------------|
| username     | Username           | The XMPP username (left part of JID)                               | true     | -                     |
| domain       | Domain             | The XMPP domain name (right part of JID)                           | true     | -                     |
| password     | Password           | The XMPP user password                                             | true     | -                     |
| host         | Server Hostname/IP | The IP address or hostname of the XMPP server                      | false    | as "domain" parameter |
| port         | XMPP Server Port   | Port for the XMPP server                                           | false    | 5222                  |
| securityMode | Security Mode      | Sets the TLS security mode: `required`, `ifpossible` or `disabled` | false    | `required`            |

## Channels

You can add `publishTrigger` channels to the bridge to react to incoming messages.

**publishTrigger** parameters:

| Name      | Label               | Description                                                                                                                                                                                                                           | Required |
|-----------|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| payload   | Payload condition   | An optional condition on the value                                                                                                                                                                                                    | false    |
| separator | Separator character | The trigger payload usually contains only the received text. If you define a separator character (e.g., `#`), the sender UID and received text will both be included in the payload. For example: `pavel@example.com#My Message Text` | false    |

## Example Rules

Send message:

```java
rule "Leak detected"
when
    Item Xi_Leak changed
then
    if(Xi_Leak.state == ON) {
        val actions = getActions("xmppclient","xmppclient:xmppBridge:xmpp")
        actions.publishXMPP("pavel@example.com","Warning! Leak detected!")
    }
end
```

Receive and process message:

```java
rule "Turn off all lights without separator"
when
    Channel "xmppclient:xmppBridge:xmpp:xmpp_command" triggered
then
    var actionName = receivedEvent
    if(actionName.toLowerCase() == "turn off lights") {
        Group_Light_Home_All.sendCommand(OFF)
    }
end

rule "Turn off all lights with separator and reply"
when
    Channel "xmppclient:xmppBridge:xmpp:xmpp_command" triggered
then
    var actionName = receivedEvent.split("##")
    if(actionName.get(1).toLowerCase() == "turn off lights") {
        Group_Light_Home_All.sendCommand(OFF)

        val actions = getActions("xmppclient","xmppclient:xmppBridge:xmpp")
        actions.publishXMPP(actionName.get(0),"All lights were turned off")
    }
end
```

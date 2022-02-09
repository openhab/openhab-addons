# SMSModem Binding

This binding connects to a USB serial GSM modem (or a network exposed one, see ser2net) and allows openHAB to send and receive SMS through it.

Serial modem should all use the same communication protocol (AT message) and therefore this binding _should_ be compatible with every dongle. However, ther is a gap between theory and reality and success may vary. The protocol stack is based on the no longer supported smslib project (more precisely a v4 fork), and all modems supported by this library should be OK. The following devices have been reported functional :

-  Huawei E180

## Supported Things

Two things are supported by this binding :

- A *smsmodembridge*, representing the dongle
- A *smsconversation*, representing a conversation between one distant msisdn and the msisdn on the sim card in the dongle.

## Discovery

There is no discovery process for *smsmodembridge* thing.
A *smsconversation* thing will be discovered and added to the inbox everytime the modem should receive a SMS by a new sender.

## Thing Configuration

The *smsmodembridge* thing requires at least two parameters to work properly (serialPortOrIP, baudOrNetworkPort).
Depending on the nature of the connection (direct serial modem, or serial over network), this two field will be used differently :

| field | direct serial modem   | serial over network                  |
|-------|--------------------------|-----------------------------------|
|serialPortOrIP| The serial port to access (eg. /dev/tty/USBx) | IP address of the computer hosting the ser2net service|
|baudOrNetworkPort| Baud rate                                 | The network port of the ser2net service |

The other parameters are optional :

| field | description     |
|-------|--------------------------|-----------------------------------|
|simPin          |If your sim card is protected, fill this field with the PIN code|
|pollingInterval| Delay between two checks for new message|
|delayBetweenSend|Delay to wait between two messages sent (could be necessary for slow modem)|

```
Bridge smsmodem:smsmodembridge:adonglename [ serialPortOrIP="/dev/ttyUSB0", baudOrNetworkPort="19200", enableAutoDiscovery="true" ]
```

The *smsconversation* thing is just a shortcut to address/receive messages with a specific msisdn. It is not mandatory to use the binding, as you can use action and trigger channel to send/receive a message once the smsmodem bridge is configured.

| field | description               |
|-------|--------------------------|
| recipient | The msisdn of the phone you want to discuss with.|
| deliveryReport | If enabled, ask the network for a delivery report (default false)|

```
Thing smsmodem:smsconversation:aconversationname [ recipient="XXXXXXXXXXX", deliveryReport="true" ]
```

## Channels

The *smsconversation* supports the following channels :
| channel  | type   | description                  |
|----------|--------|------------------------------|
| receive | String| The last message received |
| send | String| A message to send |
|deliverystatus| String| Delivery status (either UNKNOWN, QUEUED, SENT, PENDING, DELIVERED, EXPIRED, or FAILED). Several status are only possible if the delivery report parameter is enabled|

## Trigger channels

The *smsmodembridge* has the following trigger channel :
| Channel ID          | event                      |  
|---------------------|----------------------------|
|receivetrigger| The msisdn and message received (concatened with the '\|' character as a separator)|


## Rule action

This binding includes a rule action to send SMS.

```
(Rule DSL)
val smsAction = getActions("smsmodem","smsmodem:smsmodembridge:<uid>")
```

```
(javascript JSR)
var smsAction = actions.get("smsmodem","smsmodem:smsmodembridge:<uid>");
```

Where uid is the Bridge UID of the *smsconversation* thing.

Once this action instance is retrieved, you can invoke the 'send' method on it:

```
smsAction.send("1234567890", "Hello world!")
```

## Full Example

### Send SMS

`sms.rules` for DSL :

```java
rule "Alarm by SMS"
when
   Item Alarm changed
then
   val smsAction = getActions("smsmodem","smsmodem:smsmodembridge:dongleuid")
   smsAction.send("33123456789", "Alert !")
end
```

### Receive and forward SMS

`sms.py` with the python helper library :

```python
@rule("smscommand.receive", description="Receive SMS and resend it")
@when("Channel smsmodem:smsmodembridge:dongleuid:receivetrigger triggered")
def smscommand(event):
    sender_and_message = event.event.split("|")
    sender = sender_and_message[0]
    content = sender_and_message[1]
    actions.get("smsmodem", "smsmodem:smsmodembridge:dongleuid").send("336123456789", sender + "send the following message:" + content)
```

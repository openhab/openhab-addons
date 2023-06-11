# SMSModem Binding

This binding connects to a USB serial GSM modem (or a network exposed one, see ser2net) and allows openHAB to send and receive SMS through it.

Serial modem should all use the same communication protocol (AT message) and therefore this binding _should_ be compatible with every dongle.
However, there is a gap between theory and reality and success may vary.
The protocol stack is based on the no longer supported smslib project (more precisely a v4 fork), and all modems supported by this library should be OK.

The following devices have been reported functional :

- Huawei E180

## Supported Things

Two things are supported by this binding :

- A _smsmodembridge_, representing the dongle connected on the local computer
- A _smsmodemremotebridge_, representing the dongle exposed over the network (with ser2net or other similar software)
- A _smsconversation_, representing a conversation between one distant msisdn and the msisdn on the sim card in the dongle.

## Discovery

There is no discovery process for _smsmodembridge_ or _smsmodemremotebridge_ thing.
A _smsconversation_ thing will be discovered and added to the inbox everytime the modem should receive a SMS by a new sender.

## Thing Configuration

The _smsmodembridge_ or _smsmodemremotebridge_ things requires at least two parameters to work properly.

For local _smsmodembridge_:

| Parameter Name | type    | direct serial modem                           |
| -------------- | ------- | --------------------------------------------- |
| serialPort     | text    | The serial port to access (eg. /dev/tty/USBx) |
| baud           | integer | Baud rate                                     |

For remote _smsmodemremotebridge_:

| Parameter Name | type    | serial over network                                    |
| -------------- | ------- | ------------------------------------------------------ |
| ip             | text    | IP address of the computer hosting the ser2net service |
| networkPort    | integer | The network port of the ser2net service                |

The other parameters are optional :

| Parameter Name   | type    | description                                                                                  |
| ---------------- | ------- | -------------------------------------------------------------------------------------------- |
| simPin           | text    | If your sim card is protected, fill this field with the PIN code                             |
| pollingInterval  | integer | Delay between two checks for new message (in seconds)                                        |
| delayBetweenSend | integer | Delay to wait between two messages post (in milliseconds, could be necessary for slow modem) |

The _smsconversation_ thing is just a shortcut to address/receive messages with a specific msisdn. It is not mandatory to use the binding, as you can use action and trigger channel to send/receive a message once the smsmodem bridge is configured.

| Parameter Name | type    | description                                                                                                                                                                                  |
| -------------- | ------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| recipient      | text    | The msisdn of the phone you want to discuss with.                                                                                                                                            |
| deliveryReport | boolean | If enabled, ask the network for a delivery report (default false)                                                                                                                            |
| encoding       | text    | The encoding to use when sending the message (either Enc7, Enc8, EncUcs2, EncCustom, default is Enc7). EncUcs2 is good for non latin character, but SMS character size limit is then reduced |

## Channels

The _smsconversation_ supports the following channels :
| channel        | type   | description                                                                                                                                                          |
| -------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| receive        | String | The last message received                                                                                                                                            |
| send           | String | A message to send                                                                                                                                                    |
| deliverystatus | String | Delivery status (either UNKNOWN, QUEUED, SENT, PENDING, DELIVERED, EXPIRED, or FAILED). Several status are only possible if the delivery report parameter is enabled |

## Trigger channels

The _smsmodembridge_ and _smsmodemremotebridge_ has the following trigger channel :
| Channel ID     | event                                                                               |
| -------------- | ----------------------------------------------------------------------------------- |
| receivetrigger | The msisdn and message received (concatened with the '\|' character as a separator) |

## Rule action

This binding includes a rule action to send SMS.

```javascript
(Rule DSL)
val smsAction = getActions("smsmodem","smsmodem:smsmodembridge:<uid>")
```

```javascript
(javascript JSR)
var smsAction = actions.get("smsmodem","smsmodem:smsmodembridge:<uid>");
```

Where uid is the Bridge UID of the _smsmodembridge_ thing.

Once this action instance is retrieved, you can invoke the 'send' method on it:

```java
smsAction.sendSMS("1234567890", "Hello world!")
```

Or with a special encoding:

```java
smsAction.sendSMS("1234567890", "Hello world!", "EncUcs2")
```

## Full Example

### Thing configuration

things/smsmodem.things:

```java
Bridge smsmodem:smsmodembridge:adonglename "USB 3G Dongle " [ serialPort="/dev/ttyUSB0", baud="19200" ] {
    Thing smsconversation aconversationname [ recipient="XXXXXXXXXXX", deliveryReport="true" ]
}
```

### Send SMS

`sms.rules` for DSL:

```java
rule "Alarm by SMS"
when
   Item Alarm changed
then
   val smsAction = getActions("smsmodem","smsmodem:smsmodembridge:dongleuid")
   smsAction.sendSMS("33123456789", "Alert !")
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
    actions.get("smsmodem", "smsmodem:smsmodembridge:dongleuid").send("336123456789", sender + " just send the following message: " + content)
```

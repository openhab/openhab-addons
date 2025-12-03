# Prowl Binding

This binding integrates the [Prowl](https://www.prowlapp.com) iOS push service.
It was written from scratch and is not based on the original 1.x Prowl binding.
Its only purpose is to send push messages to iOS devices.

## Binding Configuration

The binding does not require any manual configuration at the binding level.

## Thing Configuration

This binding has a single Thing called Broker.
To use it, add a broker instance and configure the API key (generate it on the Prowl website).
You can also set the application property, which identifies the originator of these push messages.
If you want a specific refresh interval for the remaining free push-messages channel, set the refresh property.
Be aware: every check consumes one of the free push messages you can send per hour.

## Channels

The broker Thing has one channel exposing the number of free push messages remaining this hour.

| channel    | type   | description                                            |
|------------|--------|--------------------------------------------------------|
| remaining  | Number | The number of free push messages remaining this hour   |

## Example

### Things

```java
Thing prowl:broker:mybroker "Prowl Broker" [ apiKey="0000000000000000000000000000000000000000" ]
```

### Rules

Once you’ve created the broker Thing with a valid API key, you can use the Prowl service in rules.
First, create an instance of the broker before any call or at the top of the rules file (replace mybroker with your Thing ID).
Then call the pushNotification method, which requires two parameters: event and description.
An optional third parameter, priority, represents the message priority (very low) -2, -1, 0, 1, 2 (emergency). The default priority is 0.

```java
val prowl = getActions("prowl","prowl:broker:mybroker")
prowl.pushNotification("Event", "This is the description of the event")
prowl.pushNotification("Emergency Event", "This is the description of the event", 2)
```

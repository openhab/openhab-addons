# Prowl Binding

This is the binding for the [Prowl](https://www.prowlapp.com) iOS push service.  
It has been written from scratch and therefore it is not based on the original 1.x Prowl binding.  
It has no other purpose than sending push messages to iOS devices.

## Binding Configuration

The binding does not require any manual configuration on the binding level.

## Thing Configuration

This binding has only one thing called _Broker_. If you want to use this binding, just add a broker instance and configure the API key, which you can generate on the Prowl website.  
You can also modify the _application_ property, which identifies the originator of these push messages.  
If you want to have specific refresh time for the remaining free push messages channel, you can edit the _refresh_ property.
Anyway beware - every check consumes one free push message you can send in an hour.

## Channels

The broker thing has only one channel keeping the number of free push messages, which can be sent.

| channel    | type   | description                                            |
|------------|--------|--------------------------------------------------------|
| remaining  | Number | This channel provides the number of free push messages |

## Example

_*.things_

```
Thing prowl:broker:mybroker "Prowl Broker" [ apiKey="0000000000000000000000000000000000000000" ]
```

_*.rules_

Once you have created the broker thing with a valid API key, you can use the Prowl service in your rules.  
First you need to create an instance of the broker just before any call or on the top rules level. (replace the _mybroker_ with the right name of your instance).
Then you can call method _pushNotification_, which requires two parameters - _event_ and _description_. 
There is also an optional third parameter _priority_ which represents the message priority (very low) -2,-1,0,1,2 (emergency). The default priority is 0. 

```
val prowl = getActions("prowl","prowl:broker:mybroker")  
prowl.pushNotification("Event", "This is the description of the event")
prowl.pushNotification("Emergency Event", "This is the description of the event", 2)
```

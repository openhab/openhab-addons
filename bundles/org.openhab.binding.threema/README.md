# Threema Binding

This is the binding allows to send text messages via the Threema.Gateway (https://gateway.threema.ch).

There are two main modes of operation:

**1. Basic mode (server-based encryption)**
- The server handles all encryption for you.
- The server needs to know the private key associated with your Threema API identity.
- Incoming messages and delivery receipts are not supported.

**2. End-to-end encrypted mode (yet not supported by this binding)**
- The server doesn't know your private key.
- Incoming messages and delivery receipts are supported.
- You need to run software on your side to encrypt each message before it can be sent, and to decrypt any incoming messages or delivery receipts.

## Supported Things

### Basic Message Transmitter (basic)
This thing can be used to send messages to any Threema user.

## Discovery

Auto discovery is not supported.

## Thing Configuration

### Basic Message Transmitter

| Parameter    | Required | Description |
|--------------|----------|-------------|
| gatewayId    | yes      | The senders Threema ID. Threema Gateway lets you request your personal, custom ID. |
| secret       | yes      | The private key associated with the senders Threema ID. |
| recipientIds | no       | A list of Threema IDs of recipients to send messages to. |

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| credits  | Number | The remaining credits to send messages. (read only) |

## Full Example

### threema.thing
```
Thing threema:basic:example "Threema" [ gatewayId="*THREEMA", secret="0123456789ABCDEF", recipientIds="*THREEM1, *THREEM2"]
```

### DSL script
```
val threema = getActions("threema","threema:basic:example");
threema.sendTextMessageSimple("Hello World");
```

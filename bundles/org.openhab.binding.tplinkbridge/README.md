# TP-Link Bridge Binding

This binding provides one TP-Link account that Kasa and Tapo devices share.
You enter your TP-Link credentials once, on a `tplinkbridge:account` bridge, and Things of other TP-Link bindings use them to log in to their devices.

The bridge only stores the credentials.
It doesn't contact the TP-Link cloud or any device itself, so wrong credentials show up as login errors on the device Things, not on the bridge.

## Supported Things

| Thing type | Description                                                         |
|------------|---------------------------------------------------------------------|
| `account`  | Bridge holding the e-mail address and password of a TP-Link account |

## Discovery

There is no discovery.
Add the bridge manually.

## Thing Configuration

### `account` Bridge Configuration

| Name       | Type | Description                            | Default | Required | Advanced |
|------------|------|----------------------------------------|---------|----------|----------|
| `username` | text | E-mail address of your TP-Link account | N/A     | yes      | no       |
| `password` | text | Password of your TP-Link account       | N/A     | yes      | no       |

The bridge is ONLINE when both values are set.

## Channels

The bridge has no channels.

## Full Example

### Thing Configuration

```java
Bridge tplinkbridge:account:home "TP-Link Account" [ username="user@example.com", password="secret" ]
```

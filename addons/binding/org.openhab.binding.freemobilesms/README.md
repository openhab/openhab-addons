# FreeMobile SMS Binding

The FreeMobile SMS binding povides support for sending SMS via [FreeMobile API](https://www.freenews.fr/freenews-edition-nationale-299/free-mobile-170/nouvelle-option-notifications-par-sms-chez-free-mobile-14817).

## Supported Things

This binding supports the following thing types:

| Thing         | Thing Type | Description                                             |
|---------------|------------|---------------------------------------------------------|
| account       | Thing      | One callable account.                                   |

## Thing Configuration

### Account

The *account* thing requires the following configuration parameters:

| Parameter Label | Parameter ID | Description           | Required | Default |
|-----------------|--------------|-----------------------|----------|---------|
| User Identifier | user         | The user identifier.  | true     | None    |
| Password        | password     | The related password. | true     | None    |

## Channels

Things support the following channels:

| Channel Type ID | Item Type | Description                               |
|-----------------|-----------|-------------------------------------------|
| message         | Text      | This channel lets send message to account |

## Full Example

demo.things:

```xtend
Thing freemobilesms:account:dad "Dad" [ user="12345", password="abcde" ]
```

demo.items:

```xtend
String DAD_SMS "Dad's SMS" { channel="freemobilesms:account:dad:message" }
```

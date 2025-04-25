# Mail Binding

The Mail binding provides support for sending mails from rules.

## Supported Things

There are three things: `smtp`, `imap` and `pop3` which represents respective servers.

## Thing Configuration

### SMTP Server (`smtp`)

There are two mandatory parameters `hostname` and `sender`.

The `hostname` may contain an IP address or a FQDN like `smtp.gmail.com`.
The `sender` must be a valid mail address used as sender address for mails.

The `security`, `port`, `username` and `password` parameters are optional.

The `security` parameter defines the transport security and can be set to `PLAIN` (default), `STARTTLS` or `SSL` (for implicit SSL/TLS).
The `port` parameter is used to change the default ports for the SMTP server.
Default ports are `25` (for `PLAIN` and `STARTTLS`) and `465` (for `SSL`).
For authentication, `username` and `password` can be supplied.
If one or both are empty, no authentication data is provided to the SMTP server during connect.

### IMAP Server (`imap`) / POP3 Server (`pop3`)

There is one mandatory parameter: `hostname`.
The `hostname` may contain an IP address or a FQDN like `mail.gmail.com`.
For authentication `username` and `password` also need to be supplied.

The `refresh`, `security`, `port`, `username` and `password` parameters are optional.

The `refresh` parameter is the time in seconds between two refreshes of the thing's channels.
If omitted, the default of 60s is used.
The `security` parameter defines the transport security and can be set to `PLAIN` (default), `STARTTLS` or `SSL` (for implicit SSL/TLS).
The `port` parameter is used to change the default ports for the SMTP server.
Default ports are `143` (for `PLAIN` and `STARTTLS`) and `993` (for `SSL`) in the case of `imap` or `110` (for `PLAIN` and `STARTTLS`) and `995` (for `SSL`) in the case of `pop3`.

## Channels

There are no channels for the `smtp` thing.
The `imap` and `pop3` things can be extended with `mailcount`- and `content`-type channels.

### Type `mailcount`

Each channel has two parameters: `folder` and `type`.

The `folder` is mandatory and denotes the folder name on the given account.

You can either use the root folder like (e.g. "INBOX") or a subdirectory of your structure (e.g. "INBOX.Sent" or "INBOX.Junk").
The `type` parameter can be `UNREAD` or `TOTAL` (default).
Channels with type `UNREAD` give the number on unread mails in that folder.

### Type `content`

The `content` type channel presents the contents of an unread mail.
If the message is a MIME- or MIME-multipart message, all parts are concatenated.
The content is converted to a plain string without processing (i.e. HTML tags are still present).
In most cases the mail content needs further processing in rules to trigger appropriate action.

Each channel has five parameters: `folder`, `subject`, `sender`, `transformation` and `markAsRead`.

The `folder` is mandatory and denotes the folder name on the given account.
You can either use the root folder like (e.g. "INBOX") or a subdirectory of your structure (e.g. "INBOX.Sent" or "INBOX.Junk").

`subject` and `sender` can be used to filter the messages that are processed by the channel.
Filters use regular expressions (e.g. `.*DHL.*` as `sender` would match all From-addresses that contain "DHL").
If a parameter is left empty, no filter is applied.

The `transformation` is applied before setting the channel status.
Transformations are defined using this syntax: `TYPE(FUNCTION)`, e.g.: `JSONPATH($.path)`.
The syntax: `TYPE:FUNCTION` is still supported, e.g.: `JSONPATH:$.path`.
Transformations can be chained in the UI by listing each transformation on a separate line, or by separating them with the mathematical intersection character "∩".
For example, `REGEX(.*Shipment-Status: ([a-z]+).*) ∩ MAP(status.map)` would first extract a character string with a regular expression and then apply the given MAP transformation on the result.
Please note that the values will be discarded if one transformation fails (e.g. REGEX did not match).
This means that you can also use it to filter certain emails e.g. `REGEX:(.*Sendungsbenachrichtigung.*)` would only match for mails containing the string "Sendungsbenachrichtigung" but output the whole message.

Since with each refresh all unread mails are processed the same message content would be sent to the channel multiple times.
This can be prevented by setting `markAsRead` to `true` (default is `false`), which marks all processed messages as read.

## Full Example

mail.things:

```java
Thing mail:smtp:samplesmtp [ hostname="smtp.example.com", sender="mail@example.com", security="SSL", username="user", password="pass" ]

Thing mail:imap:sampleimap [ hostname="imap.example.com", security="SSL", username="user", password="pass" ] {
    Channels:
        Type mailcount : inbox_total [ folder="INBOX", type="TOTAL" ]
        Type mailcount : inbox_unread [ folder="INBOX", type="UNREAD" ]
        Type content : fedex_notification [ folder="INBOX" sender="Fedex.*" markAsRead="true" ]
}
```

mail.items:

```java
Number InboxTotal  "INBOX [%d]"        { channel="mail:imap:sampleimap:inbox_total" }
Number InboxUnread "INBOX Unread [%d]" { channel="mail:imap:sampleimap:inbox_unread" }
String FedexNotification               { channel="mail:imap:sampleimap:fedex_notification" }
```

mail.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Text item=InboxTotal
        Text item=InboxUnread
    }
}
```

## Rule Action

This binding includes rule actions for sending email.
Six different actions available:

- `boolean success = sendMail(String recipient, String subject, String text)`
- `boolean success = sendMailWithAttachment(String recipient, String subject, String text, String url)`
- `boolean success = sendMailWithAttachments(String recipient, String subject, String text, List<String> urlList)`
- `boolean success = sendHtmlMail(String recipient, String subject, String htmlContent)`
- `boolean success = sendHtmlMailWithAttachment(String recipient, String subject, String htmlContent, String url)`
- `boolean success = sendHtmlMailWithAttachments(String recipient, String subject, String htmlContent, List<String> urlList)`

The `sendMail(...)` send a plain text mail (with attachments if supplied).
The `sendHtmlMail(...)` send a HTML mail (with attachments if supplied).

Both functions return a boolean as the result of the operation.

`recipient` can be a single address (`mail@example.com`) or a list of addresses, concatenated by a comma (`mail@example.com, mail2@example.com`).

Since there is a separate rule action instance for each `smtp` thing, this needs to be retrieved through `getActions(scope, thingUID)` (DSL) or `actions.get(scope, thingUID)` (Javascript).
The first parameter always has to be `mail` and the second is the full Thing UID of the SMTP server that should be used.
Once this action instance is retrieved, you can invoke the action method on it.

Please note: All strings are expected to be UTF-8 encoded.
Using different character sets may produce unwanted results.

Examples:

:::: tabs

::: tab DSL

```java
val mailActions = getActions("mail","mail:smtp:samplesmtp")
var success = mailActions.sendMail("mail@example.com", "Test subject", "This is the mail content.")
success = mailActions.sendMail("mail1@example.com, mail2@example.com", "Test subject", "This is the mail content sent to multiple recipients.")

```

```java
import java.util.List

val List<String> attachmentUrlList = newArrayList(
  "http://some.web/site/snap.jpg&param=value",
  "file:///tmp/201601011031.jpg")
val mailActions = getActions("mail","mail:smtp:sampleserver")
mailActions.sendHtmlMailWithAttachments("mail@example.com", "Test subject", "<h1>Header</h1>This is the mail content.", attachmentUrlList)
```

:::

::: tab JavaScript

```javascript
val mailActions = actions.get("mail","mail:smtp:samplesmtp")
var success = mailActions.sendMail("mail@example.com", "Test subject", "This is the mail content.")
success = mailActions.sendMail("mail1@example.com, mail2@example.com", "Test subject", "This is the mail content sent to multiple recipients.")
```

```javascript
import java.util.List

val List<String> attachmentUrlList = newArrayList(
  "http://some.web/site/snap.jpg&param=value",
  "file:///tmp/201601011031.jpg")
val mailActions = actions.get("mail","mail:smtp:sampleserver")
mailActions.sendHtmlMailWithAttachments("mail@example.com", "Test subject", "<h1>Header</h1>This is the mail content.", attachmentUrlList)
```

:::

::: tab JRuby

```ruby
mail = things["mail:smtp:samplesmtp"]
success = mail.send_mail("mail@example.com", "Test subject", "This is the mail content.")
success = mail.send_mail("mail1@example.com, mail2@example.com", "Test subject", "This is the mail content sent to multiple recipients.")
```

```ruby
attachment_urls = [
  "http://some.web/site/snap.jpg&param=value",
  "file:///tmp/201601011031.jpg"
]
things["mail:smtp:sampleserver"].send_html_mail_with_attachments("mail@example.com", "Test subject", "<h1>Header</h1>This is the mail content.", attachment_urls)
```

:::

::::

## Mail Headers

The binding allows one to add custom e-mail headers to messages that it sends.
For example if you want e-mails sent by this binding to be grouped into a "threaded view" in your email client, you must provide an e-mail "Reference" header, which acts as the key for grouping messages together.
Headers can be added inside a rule by calling the `mailActions.addHeader()` method before calling the respective `mailActions.sendMail()` method.
See the example below.

:::: tabs

::: tab DSL

```java
rule "Send Mail with a 'Reference' header; for threaded view in e-mail client"
when
    ...
then
    val mailActions = getActions("mail","mail:smtp:sampleserver")
    mailActions.addHeader("Reference", "<unique-thread-identifier>")
    mailActions.sendMail("mail@example.com", "Test subject", "Test message text")
end
```

:::

::: tab JavaScript

```javascript
val mailActions = actions.get("mail","mail:smtp:sampleserver")
mailActions.addHeader("Reference", "<unique-thread-identifier>")
mailActions.sendMail("mail@example.com", "Test subject", "Test message text")
```

:::

::: tab JRuby

```ruby
mail = things["mail:smtp:sampleserver"]
mail.add_header("Reference", "<unique-thread-identifier>")
mail.send_mail("mail@example.com", "Test subject", "Test message text")
```

:::

::::
Note: in the case of the "Reference" header, the `<unique-thread-identifier>` has to be an ASCII string enclosed in angle brackets.

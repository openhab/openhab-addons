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
The `imap` and `pop3` things can be extended with `mailcount`-type channels.

### Type `mailcount`

Each channel has two parameters: `folder` and `type`.
The `folder` is mandatory and denotes the folder name on the given account.
You can either use the root folder like (e.g. "INBOX") or a sub directory of your structure (e.g. "INBOX.Sent" or "INBOX.Junk").
The `type` parameter can be `UNREAD` or `TOTAL` (default).
Channels with type `UNREAD` give the number on unread mails in that folder.

## Full Example

mail.things:

```java
Thing mail:smtp:samplesmtp [ hostname="smtp.example.com", sender="mail@example.com", security="SSL", username="user", password="pass" ]

Thing mail:imap:sampleimap [ hostname="imap.example.com", security="SSL", username="user", password="pass" ] {
    Channels:
        Type mailcount : inbox_total [ folder="INBOX", type="TOTAL" ]
        Type mailcount : inbox_unread [ folder="INBOX", type="UNREAD" ]
}
```

mail.items:

```java
Number InboxTotal  "INBOX [%d]"        { channel="mail:imap:sampleimap:inbox_total" }
Number InboxUnread "INBOX Unread [%d]" { channel="mail:imap:sampleimap:inbox_unread" }
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

Since there is a separate rule action instance for each `smtp` thing, this needs to be retrieved through `getActions(scope, thingUID)`.
The first parameter always has to be `mail` and the second is the full Thing UID of the SMTP server that should be used.
Once this action instance is retrieved, you can invoke the action method on it.

Please note: All strings are expected to be UTF-8 encoded.
Using different character sets may produce unwanted results.

Examples:

```java
val mailActions = getActions("mail","mail:smtp:samplesmtp")
val success = mailActions.sendMail("mail@example.com", "Test subject", "This is the mail content.")
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

## Mail Headers

The binding allows one to add custom e-mail headers to messages that it sends.
For example if you want e-mails sent by this binding to be grouped into a "threaded view" in your email client, you must provide an e-mail "Reference" header, which acts as the key for grouping messages together.
Headers can be added inside a rule by calling the `mailActions.addHeader()` method before calling the respective `mailActions.sendMail()` method.
See the example below.

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

Note: in the case of the "Reference" header, the `<unique-thread-identifier>` has to be an ASCII string enclosed in angle brackets.

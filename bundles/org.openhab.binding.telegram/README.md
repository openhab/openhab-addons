# Telegram Binding

The Telegram binding allows sending and receiving messages to and from Telegram clients (https://telegram.org), by using the Telegram Bot API.

# Prerequisites

As described in the Telegram Bot API, this is the manual procedure needed in order to get the necessary information.

1. Create the Bot and get the Token

- On a Telegram client open a chat with BotFather.
- Send `/newbot` to BotFather and fill in all the needed information. The authentication token that is given will be needed in the next steps.

2. Create the destination chat

- Open a chat with your new Bot and send any message to it. The next step will not work unless you send a message to your bot first.

3. Get the chatId

- Open a browser and invoke `https://api.telegram.org/bot<token>/getUpdates` (where `<token>` is the authentication token previously obtained)
- Look at the JSON result to find the value of `id`. That is the chatId. Note that if using a Telegram group chat, the group chatIds are prefixed with a dash that must be included in the config file. (e.g. bot1.chatId: -22334455)

4. Test the bot

- Open this URL in your web browser, replacing <token> with the authentication token and <chatId> with the chatId:
- `https://api.telegram.org/bot<token>/sendMessage?chat_id=<chatId>&text=testing`
- Your Telegram-bot should send you a message with the text: `testing`

## Supported Things

**telegramBot** - A Telegram Bot that can send and receive messages.

The Telegram binding supports the following things which origin from the latest message sent to the Telegram bot:
* message text
* message date
* full name of sender (first name + last name)
* username of sender
* reply id (used to identify an answer from a user of a previously sent message by the binding)

Please note that the things cannot be used to send messages. In order to send a message, an action must be used instead.

## Thing Configuration

**telegramBot** parameters:

| Property                | Default | Required | Description                                                                                  |
|-------------------------|---------|:--------:|----------------------------------------------------------------------------------------------|
| `botUsername`                    |         | Yes      | The name of the bot                                                     |
| `chatIds`     |         | Yes      | Comma-separated list of chat ids                    |
| `botToken`      |         | Yes      | authentication token                                                                         |

## Channels

| Channel Type ID                      | Item Type | Description                                                     |   |   |
|--------------------------------------|-----------|-----------------------------------------------------------------|---|---|
| lastMessageText                      | String    | The last received message                                       |   |   |
| lastMessageDate                      | String    | The date of the last received message                           |   |   |
| lastMessageName                      | String    | The full name of the sender of the last received message        |   |   |
| lastMessageUsername                  | String    | The username of the sender of the last received message         |   |   |
| replyId                              | String    | The id of the reply which was passed to sendTelegram() as replyId argument. This id can be used to have an unambiguous assignment of the users reply to the message which was sent by the bot             |   |   |


## Rule Actions

This binding includes a rule action, which allows to send Telegram messages from within rules.

```
val telegramAction = getActions("telegram","telegram:telegramBot:<uid>")
```
where uid is the Thing UID of the Telegram thing (not the chat id!).


Once this action instance is retrieved, you can invoke the `sendTelegram' method on it:

```
telegramAction.sendTelegram("Hello world!")
```

The following actions are supported.
Each of the actions returns true on success or false on failure.

| Action                | Description  |
|-----------------------|--------------|
| sendTelegram(String message) | Sends a message. |
| sendTelegram(String format, Object... args)          | Sends a formatted message (See https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html for more information).
| sendTelegram(String message, String replyId, String... buttons) | Sends a question to the user that can be answered via the defined buttons. The replyId can be freely choosen and is sent back with the answer. Then, the id is required to identify what question has been answered (e.g. in case of multiple open questions). The final result looks like this: ![Telegram Inline Keyboard](doc/queryExample.png). |
| sendTelegramAnswer(String replyId, String message) | Sends a message after the user has answered a question. You should *always* call this method after you received an answer. It will remove buttons from the specific question and will also stop the progress bar displayed at the client side. If no message is necessary, just pass `null` here. |
| sendTelegramPhoto(String photoURL, String caption) | Sends a picture. The URL can be specified using the http, https, and file protocols. |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
b 
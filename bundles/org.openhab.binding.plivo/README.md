# Plivo Binding

This binding integrates with the [Plivo](https://www.plivo.com/) cloud communications platform.
It allows sending and receiving SMS, MMS, and WhatsApp messages, as well as making and receiving voice calls with text-to-speech and DTMF input support.

Typical use cases include:

- Sending SMS/MMS alerts when events occur (door opens, alarm triggers, temperature threshold)
- Receiving SMS commands to control your smart home ("status", "arm alarm", "turn on lights")
- Making voice calls for critical alerts with text-to-speech
- Receiving incoming calls with an interactive voice menu (press 1 for X, press 2 for Y)
- Sending and receiving WhatsApp messages

## Supported Things

| Thing Type | Description                                                                            |
| ---------- | -------------------------------------------------------------------------------------- |
| `account`  | A Plivo account (bridge). Holds API credentials and shared settings.                   |
| `phone`    | A Plivo phone number. Sends/receives messages and calls. Requires an `account` bridge. |

## Discovery

Once a Plivo Account bridge is added and goes online, the binding automatically discovers all phone numbers associated with the account and adds them to the inbox.
You can also trigger a manual scan from the UI.

The account bridge itself must be created manually with your [Plivo console](https://cx.plivo.com/) credentials.

## Thing Configuration

### `account` Bridge Configuration

| Name                  | Type    | Description                                                         | Default | Required | Advanced |
| --------------------- | ------- | ------------------------------------------------------------------- | ------- | -------- | -------- |
| authId                | text    | Plivo Auth ID (starts with MA or SA)                                | N/A     | yes      | no       |
| authToken             | text    | Plivo Auth Token                                                    | N/A     | yes      | no       |
| publicUrl             | text    | Public-facing base URL for webhooks (e.g. `https://my.domain.com`)  | N/A     | no       | yes      |
| autoConfigureWebhooks | boolean | Automatically create and assign a Plivo application via API         | true    | no       | yes      |
| useCloudWebhook       | boolean | Use openHAB Cloud for webhook callbacks (no port forwarding needed) | false   | no       | yes      |

The Auth ID and Auth Token can be found in the [Plivo console](https://cx.plivo.com/).

To receive incoming messages and calls, and to place outbound voice calls, you need **one** of the following:

- **openHAB Cloud Webhooks** (recommended): Set `useCloudWebhook` to `true`. Requires the openHAB Cloud Connector add-on to be installed and connected. No port forwarding or reverse proxy needed.
- **Public URL**: Set `publicUrl` to a publicly-reachable URL for your openHAB instance. You can use a reverse proxy, port forwarding, or a service like ngrok.

Outbound voice calls also require one of the above: unlike messaging, Plivo fetches the call flow from an answer URL that the binding hosts, so a publicly-reachable URL must be configured for `makeCall` and `makeTTSCall` to work.

### `phone` Thing Configuration

| Name            | Type    | Description                                                    | Default                                                     | Required | Advanced |
| --------------- | ------- | ------------------------------------------------------------- | ----------------------------------------------------------- | -------- | -------- |
| phoneNumber     | text    | Plivo phone number in E.164 format (e.g. +15551234567)        | N/A                                                         | yes      | no       |
| voiceGreeting   | text    | Plivo XML template for incoming voice calls                   | See below                                                   | no       | yes      |
| gatherResponse  | text    | Plivo XML returned after DTMF digits are gathered (fallback)  | `<Response><Speak>Thank you. Goodbye.</Speak></Response>`   | no       | yes      |
| responseTimeout | integer | Seconds to wait for a rule to respond with XML (1-14)         | 10                                                          | no       | yes      |

The default `voiceGreeting` includes a `<GetInput>` element that collects one DTMF digit:

```xml
<Response>
  <GetInput action="{gatherUrl}" inputType="dtmf" numDigits="1">
    <Speak>Hello. This is the openHAB smart home system. Press any key.</Speak>
  </GetInput>
  <Speak>No input received. Goodbye.</Speak>
</Response>
```

The `{gatherUrl}` placeholder is automatically replaced with the correct webhook URL.

## Channels

### State Channels

| Channel                | Type     | Description                                               |
| ---------------------- | -------- | --------------------------------------------------------- |
| last-message-body      | String   | Body text of the last received SMS/WhatsApp message       |
| last-message-from      | String   | Phone number of the last message sender                   |
| last-message-date      | DateTime | Timestamp of the last received message                    |
| last-message-media-url | String   | URL of the first media attachment (MMS/WhatsApp)          |
| last-message-uuid      | String   | Plivo Message UUID of the last received message           |
| last-call-from         | String   | Phone number of the last incoming caller                  |
| last-call-status       | String   | Status of the last call (ringing, in-progress, completed) |
| last-call-date         | DateTime | Timestamp of the last incoming call                       |
| last-dtmf-digits       | String   | Last DTMF digits received from a caller                   |

### Trigger Channels

| Channel            | Payload | Description                                 |
| ------------------ | ------- | ------------------------------------------- |
| sms-received       | JSON    | Triggered on incoming SMS/MMS               |
| whatsapp-received  | JSON    | Triggered on incoming WhatsApp message      |
| call-received      | JSON    | Triggered on incoming voice call            |
| dtmf-received      | JSON    | Triggered when DTMF digits are gathered     |
| message-status     | JSON    | Triggered on message delivery status change |
| call-status-update | JSON    | Triggered on call status change             |

Trigger channel payloads are JSON objects. Example `sms-received` payload:

```json
{"from":"+15559876543","to":"+15551234567","body":"Hello!","messageUuid":"...","type":"sms","mediaUrls":[]}
```

## Rule Actions

Actions are available on `phone` things under the `plivo` scope.

### SMS Actions

| Action    | Parameters                                   | Description                                                                       |
| --------- | -------------------------------------------- | --------------------------------------------------------------------------------- |
| `sendSMS` | `String to, String message`                  | Send a plain SMS                                                                  |
| `sendSMS` | `String to, String message, String mediaUrl` | Send an MMS with media. `message` is optional (may be `null`) to send media only. |

### WhatsApp Actions

| Action         | Parameters                                   | Description                                                                          |
| -------------- | -------------------------------------------- | ------------------------------------------------------------------------------------ |
| `sendWhatsApp` | `String to, String message`                  | Send a WhatsApp message                                                              |
| `sendWhatsApp` | `String to, String message, String mediaUrl` | Send WhatsApp with media. `message` is optional (may be `null`) to send media only.  |

WhatsApp freeform messages are only delivered within the 24-hour customer service window; the first contact (and any message outside that window) must use a pre-approved template configured in the Plivo console.

### Voice Actions

| Action            | Parameters                            | Description                                                     |
| ----------------- | ------------------------------------- | -------------------------------------------------------------- |
| `makeCall`        | `String to, String xml`               | Make a call with raw Plivo XML                                 |
| `makeTTSCall`     | `String to, String text`              | Make a call with text-to-speech                                |
| `makeTTSCall`     | `String to, String text, String voice`| TTS with voice selection (e.g. "WOMAN", "Polly.Joanna")        |
| `respondWithXml`  | `String callUuid, String xml`         | Respond to an active call with Plivo XML (see Dynamic Voice)   |

### Media URL Actions

| Action                | Parameters         | Returns        | Description                                                     |
| --------------------- | ------------------ | -------------- | --------------------------------------------------------------- |
| `createItemMediaUrl`  | `String itemName`  | `String` (URL) | Create a temporary public URL from an openHAB Image item        |
| `createProxyMediaUrl` | `String sourceUrl` | `String` (URL) | Create a temporary public URL that proxies a local/internal URL |

These actions create time-limited (5 minute) public URLs for media that Plivo can fetch.
This is useful for sending camera snapshots or locally-hosted media as MMS/WhatsApp attachments.
Either `publicUrl` must be configured on the bridge or `useCloudWebhook` must be enabled for these actions to work.

## Dynamic Voice Calls (respondWithXml)

The binding supports fully interactive voice calls where rules control the call flow in real time.
When an incoming call arrives or DTMF digits are pressed, the binding holds the HTTP response open and waits for a rule to provide Plivo XML via the `respondWithXml` action.

**How it works:**

1. Plivo sends a webhook (incoming call or DTMF input)
1. The binding fires a trigger channel (`call-received` or `dtmf-received`)
1. The binding waits up to `responseTimeout` seconds (default: 10) for a rule to call `respondWithXml`
1. If the rule responds in time, that XML is returned to Plivo
1. If the timeout expires, the default XML from the thing config is used as fallback

The `{gatherUrl}` placeholder can be used in your XML to create multi-step menus.
It is automatically replaced with the correct URL for the phone thing's gather endpoint.

### Timeout Behavior

If a rule does not call `respondWithXml` within the configured `responseTimeout` (default: 10 seconds), the binding returns the default XML from the thing configuration:

- For incoming calls: the `voiceGreeting` config parameter
- For DTMF gather: the `gatherResponse` config parameter

This means existing rules that don't use `respondWithXml` continue to work as before.
The timeout is configurable per phone thing via the `responseTimeout` advanced parameter (1-14 seconds).

## Webhook Setup

To receive incoming messages and calls, you need to configure webhooks so Plivo can reach your openHAB instance.

### Option 1: openHAB Cloud Webhooks (Recommended)

The simplest approach is to use the openHAB Cloud service to provide publicly-reachable webhook URLs.
This eliminates the need for port forwarding, reverse proxies, or a public IP address.

**Requirements:**

- The [openHAB Cloud Connector](https://www.openhab.org/addons/integrations/openhabcloud/) add-on must be installed and connected

**Setup:**

1. Enable `useCloudWebhook` on the bridge (set to `true`)
1. If not using auto-configure, copy the webhook URLs from the phone thing properties in the UI and paste them into the [Plivo console](https://cx.plivo.com/)

```java
Bridge plivo:account:myaccount "Plivo Account" [ authId="your_auth_id", authToken="your_auth_token", useCloudWebhook=true ] {
    Thing phone myphone "My Plivo Number" [ phoneNumber="+15551234567" ]
}
```

### Option 2: Public URL

1. Set the `publicUrl` on the bridge (e.g. `https://my.domain.com`)
1. The binding will automatically create a Plivo application with the webhook URLs and assign it to your phone number via the API

If you disable `autoConfigureWebhooks`, you can manually create an application in the [Plivo console](https://cx.plivo.com/) and assign it to your number:

- **Answer URL** (Voice): the `voiceWebhookUrl` property value
- **Message URL** (Messaging): the `messageWebhookUrl` property value

### URL Structure

All webhook and media endpoints are served under `/plivo/callback/` on your openHAB instance.
If using a reverse proxy, you must forward this entire path prefix.

| Path                                  | Method | Purpose                                                |
| ------------------------------------- | ------ | ------------------------------------------------------ |
| `/plivo/callback/{thingUID}/sms`      | POST   | Incoming SMS/MMS messages                              |
| `/plivo/callback/{thingUID}/whatsapp` | POST   | Incoming WhatsApp messages                             |
| `/plivo/callback/{thingUID}/voice`    | POST   | Incoming voice calls                                   |
| `/plivo/callback/{thingUID}/gather`   | POST   | DTMF input callbacks                                   |
| `/plivo/callback/{thingUID}/status`   | POST   | Message/call status updates                            |
| `/plivo/callback/{thingUID}/answer`   | POST   | Answer XML for outbound calls                          |
| `/plivo/callback/media/{uuid}`        | GET    | Temporary media serving (for MMS/WhatsApp attachments) |

The `{thingUID}` is the full thing UID (e.g. `plivo:phone:myaccount:myphone`).
The `{uuid}` is a randomly generated identifier for temporary media entries.

Incoming callbacks are authenticated with Plivo's request signature.
Voice, gather, and answer callbacks require a V3-family signature (`X-Plivo-Signature-V3` or `X-Plivo-Signature-Ma-V3`).
Messaging callbacks accept the documented V2 family (`X-Plivo-Signature-V2` or `X-Plivo-Signature-Ma-V2`) as well as the V3 family observed in live traffic.
Requests that fail signature validation are rejected with HTTP 403.

**Example full URLs** (assuming `publicUrl` is `https://my.domain.com`):

```text
https://my.domain.com/plivo/callback/plivo:phone:myaccount:myphone/sms
https://my.domain.com/plivo/callback/plivo:phone:myaccount:myphone/voice
https://my.domain.com/plivo/callback/media/550e8400-e29b-41d4-a716-446655440000
```

## Full Example

### Thing Configuration

```java
Bridge plivo:account:myaccount "Plivo Account" [ authId="your_auth_id", authToken="your_auth_token", publicUrl="https://my.domain.com" ] {
    Thing phone myphone "My Plivo Number" [ phoneNumber="+15551234567" ]
}
```

### Item Configuration

```java
String PlivoLastMessage "Last SMS [%s]" { channel="plivo:phone:myaccount:myphone:last-message-body" }
String PlivoLastFrom "From [%s]" { channel="plivo:phone:myaccount:myphone:last-message-from" }
DateTime PlivoLastDate "Received [%1$tF %1$tR]" { channel="plivo:phone:myaccount:myphone:last-message-date" }
String PlivoLastCallFrom "Last Caller [%s]" { channel="plivo:phone:myaccount:myphone:last-call-from" }
String PlivoLastDtmf "DTMF [%s]" { channel="plivo:phone:myaccount:myphone:last-dtmf-digits" }
```

### Rule Examples

#### Send SMS Alert

```javascript
rules.when().item('FrontDoor').changed().to('OPEN').then(event => {
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
    plivoActions.sendSMS('+15559876543', 'Alert: Front door was opened at ' + time.ZonedDateTime.now().toString());
}).build('Door opened alert');
```

#### Send MMS with openHAB Image Item

```javascript
rules.when().item('MotionSensor').changed().to('ON').then(event => {
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
    var mediaUrl = plivoActions.createItemMediaUrl('SecurityCamera');
    if (mediaUrl !== null) {
        plivoActions.sendSMS('+15559876543', 'Motion detected!', mediaUrl);
    }
}).build('Motion detected - send snapshot');
```

#### Receive SMS and Reply

```javascript
var ALLOWED_NUMBERS = ['+15559876543', '+15551112222'];

rules.when().channel('plivo:phone:myaccount:myphone:sms-received').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    if (ALLOWED_NUMBERS.indexOf(payload.from) === -1) {
        return;
    }
    if (payload.body.toLowerCase().includes('status')) {
        var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
        plivoActions.sendSMS(payload.from, 'All systems normal. Temperature: ' + items.IndoorTemp.state + ' F');
    }
}).build('Handle incoming SMS');
```

#### Make TTS Call for Critical Alert

```javascript
rules.when().item('SmokeDetector').changed().to('ON').then(event => {
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
    plivoActions.makeTTSCall('+15559876543',
        'Warning. Smoke has been detected in your home. Please check immediately.');
}).build('Smoke alarm call');
```

#### Security Panel IVR

```javascript
rules.when().channel('plivo:phone:myaccount:myphone:call-received').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');

    if (payload.from !== '+15559876543' && payload.from !== '+15551112222') {
        plivoActions.respondWithXml(payload.callUuid, '<Response><Speak>Access denied.</Speak></Response>');
        return;
    }

    plivoActions.respondWithXml(payload.callUuid,
        '<Response><GetInput action="{gatherUrl}" inputType="dtmf" numDigits="1">' +
        '<Speak>Security panel. Alarm is ' + items.AlarmSystem.state + '. ' +
        'Press 1 to arm. Press 2 to disarm. Press 3 for sensor status.</Speak>' +
        '</GetInput><Speak>No input. Goodbye.</Speak></Response>');
}).build('Security panel - incoming call');

rules.when().channel('plivo:phone:myaccount:myphone:dtmf-received').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');

    switch (payload.digits) {
        case '1':
            items.AlarmSystem.sendCommand('ARM');
            plivoActions.respondWithXml(payload.callUuid,
                '<Response><Speak>Alarm armed. Goodbye.</Speak></Response>');
            break;
        case '2':
            items.AlarmSystem.sendCommand('DISARM');
            plivoActions.respondWithXml(payload.callUuid,
                '<Response><Speak>Alarm disarmed. Goodbye.</Speak></Response>');
            break;
        case '3':
            plivoActions.respondWithXml(payload.callUuid,
                '<Response><Speak>Front door is ' + items.FrontDoor.state + '. Goodbye.</Speak></Response>');
            break;
    }
}).build('Security panel - DTMF handler');
```

#### Outgoing Alert Call with Confirmation

```javascript
rules.when().item('SmokeDetector').changed().to('ON').then(event => {
    var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
    plivoActions.makeCall('+15559876543',
        '<Response><GetInput action="{gatherUrl}" inputType="dtmf" numDigits="1">' +
        '<Speak>Emergency! Smoke detected in your home. Press 1 to acknowledge.</Speak>' +
        '</GetInput><Speak>No response received. We will call again.</Speak></Response>');
}).build('Fire alarm call');

rules.when().channel('plivo:phone:myaccount:myphone:dtmf-received').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    if (payload.digits === '1') {
        items.FireAlarmAcknowledged.postUpdate('ON');
        var plivoActions = actions.thingActions('plivo', 'plivo:phone:myaccount:myphone');
        plivoActions.respondWithXml(payload.callUuid, '<Response><Speak>Acknowledged. Stay safe.</Speak></Response>');
    }
}).build('Fire alarm confirmation');
```

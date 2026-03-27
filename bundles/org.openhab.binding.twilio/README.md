# Twilio Binding

This binding integrates with the [Twilio](https://www.twilio.com/) cloud communications platform.
It allows sending and receiving SMS, MMS, and WhatsApp messages, as well as making and receiving voice calls with text-to-speech and DTMF input support.

Typical use cases include:

- Sending SMS/MMS alerts when events occur (door opens, alarm triggers, temperature threshold)
- Receiving SMS commands to control your smart home ("status", "arm alarm", "turn on lights")
- Making voice calls for critical alerts with text-to-speech
- Receiving incoming calls with an interactive voice menu (press 1 for X, press 2 for Y)
- Sending and receiving WhatsApp messages

## Supported Things

| Thing Type | Description |
|------------|-------------|
| `account`  | A Twilio account (bridge). Holds API credentials and shared settings. |
| `phone`    | A Twilio phone number. Sends/receives messages and calls. Requires an `account` bridge. |

## Discovery

Once a Twilio Account bridge is added and goes online, the binding automatically discovers all phone numbers associated with the account and adds them to the inbox.
You can also trigger a manual scan from the UI.

The account bridge itself must be created manually with your [Twilio Console](https://console.twilio.com/) credentials.

## Thing Configuration

### `account` Bridge Configuration

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| accountSid | text | Twilio Account SID (starts with AC) | N/A | yes | no |
| authToken | text | Twilio Auth Token | N/A | yes | no |
| publicUrl | text | Public-facing base URL for webhooks (e.g. `https://my.domain.com`) | N/A | no | yes |
| autoConfigureWebhooks | boolean | Automatically set webhook URLs on Twilio phone numbers via API | false | no | yes |

The Account SID and Auth Token can be found in the [Twilio Console](https://console.twilio.com/).

The `publicUrl` is required to receive incoming messages and calls.
Your openHAB instance must be reachable from the internet at this URL.
You can use a reverse proxy, port forwarding, or a service like ngrok.

### `phone` Thing Configuration

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| phoneNumber | text | Twilio phone number in E.164 format (e.g. +15551234567) | N/A | yes | no |
| voiceGreeting | text | TwiML template for incoming voice calls | See below | no | yes |
| gatherResponse | text | TwiML returned after DTMF digits are gathered (fallback) | `<Response><Say>Thank you. Goodbye.</Say></Response>` | no | yes |
| responseTimeout | integer | Seconds to wait for a rule to respond with TwiML (1-14) | 10 | no | yes |

The default `voiceGreeting` includes a `<Gather>` element that collects one DTMF digit:

```xml
<Response>
  <Gather numDigits="1" action="{gatherUrl}">
    <Say>Hello. This is the open hab smart home system. Press any key.</Say>
  </Gather>
  <Say>No input received. Goodbye.</Say>
</Response>
```

The `{gatherUrl}` placeholder is automatically replaced with the correct webhook URL.

## Channels

### State Channels

| Channel | Type | Description |
|---------|------|-------------|
| lastMessageBody | String | Body text of the last received SMS/WhatsApp message |
| lastMessageFrom | String | Phone number of the last message sender |
| lastMessageDate | DateTime | Timestamp of the last received message |
| lastMessageMediaUrl | String | URL of the first media attachment (MMS/WhatsApp) |
| lastMessageSid | String | Twilio Message SID of the last received message |
| lastCallFrom | String | Phone number of the last incoming caller |
| lastCallStatus | String | Status of the last call (ringing, in-progress, completed) |
| lastCallDate | DateTime | Timestamp of the last incoming call |
| lastDtmfDigits | String | Last DTMF digits received from a caller |

### Trigger Channels

| Channel | Payload | Description |
|---------|---------|-------------|
| smsReceived | JSON | Triggered on incoming SMS/MMS |
| whatsappReceived | JSON | Triggered on incoming WhatsApp message |
| callReceived | JSON | Triggered on incoming voice call |
| dtmfReceived | JSON | Triggered when DTMF digits are gathered |
| messageStatus | JSON | Triggered on message delivery status change |

Trigger channel payloads are JSON objects. Example `smsReceived` payload:

```json
{"from":"+15559876543","to":"+15551234567","body":"Hello!","messageSid":"SM...","numMedia":"0","mediaUrls":"[]"}
```

## Rule Actions

Actions are available on `phone` things under the `twilio` scope.

### SMS Actions

| Action | Parameters | Description |
|--------|-----------|-------------|
| `sendSMS` | `String to, String message` | Send a plain SMS |
| `sendSMS` | `String to, String message, String mediaUrl` | Send an MMS with media |

### WhatsApp Actions

| Action | Parameters | Description |
|--------|-----------|-------------|
| `sendWhatsApp` | `String to, String message` | Send a WhatsApp message |
| `sendWhatsApp` | `String to, String message, String mediaUrl` | Send WhatsApp with media |

### Voice Actions

| Action | Parameters | Description |
|--------|-----------|-------------|
| `makeCall` | `String to, String twiml` | Make a call with raw TwiML |
| `makeTTSCall` | `String to, String text` | Make a call with text-to-speech |
| `makeTTSCall` | `String to, String text, String voice` | TTS with voice selection (e.g. "alice", "Polly.Joanna") |
| `respondWithTwiml` | `String callSid, String twiml` | Respond to an active call with TwiML (see Dynamic Voice below) |

### Media URL Actions

| Action | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `createItemMediaUrl` | `String itemName` | `String` (URL) | Create a temporary public URL from an openHAB Image item |
| `createProxyMediaUrl` | `String sourceUrl` | `String` (URL) | Create a temporary public URL that proxies a local/internal URL |

These actions create time-limited (5 minute) public URLs for media that Twilio can fetch.
This is useful for sending camera snapshots or locally-hosted media as MMS/WhatsApp attachments.
The `publicUrl` must be configured on the bridge for these actions to work.

**Supported media types:** Any content type works (images, audio, video, PDFs).
For Image items, the MIME type is determined from the item's `RawType` state.
For proxy URLs, the MIME type is detected from the source server's response.

## Dynamic Voice Calls (respondWithTwiml)

The binding supports fully interactive voice calls where rules control the call flow in real time.
When an incoming call arrives or DTMF digits are pressed, the binding holds the HTTP response open and waits for a rule to provide TwiML via the `respondWithTwiml` action.

**How it works:**

1. Twilio sends a webhook (incoming call or DTMF digits)
2. The binding fires a trigger channel (`callReceived` or `dtmfReceived`)
3. The binding waits up to `responseTimeout` seconds (default: 10) for a rule to call `respondWithTwiml`
4. If the rule responds in time, that TwiML is returned to Twilio
5. If the timeout expires, the default TwiML from the thing config is used as fallback

The `{gatherUrl}` placeholder can be used in your TwiML to create multi-step menus.
It is automatically replaced with the correct URL for the phone thing's gather endpoint.

### Example: Security Panel IVR

:::: tabs

::: tab JavaScript

```javascript
rules.when().channel('twilio:phone:myaccount:myphone:callReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');

    if (payload.from !== '+15559876543' && payload.from !== '+15551112222') {
        twilioActions.respondWithTwiml(payload.callSid,
            '<Response><Say>Access denied.</Say></Response>');
        return;
    }

    var alarmState = items.AlarmSystem.state;
    twilioActions.respondWithTwiml(payload.callSid,
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Security panel. Alarm is ' + alarmState + '. ' +
        'Press 1 to arm. Press 2 to disarm. Press 3 for sensor status. ' +
        'Press 4 for temperature. Press 9 to repeat.</Say>' +
        '</Gather><Say>No input. Goodbye.</Say></Response>');
}).build('Security panel - incoming call');

rules.when().channel('twilio:phone:myaccount:myphone:dtmfReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');

    switch (payload.digits) {
        case '1':
            items.AlarmSystem.sendCommand('ARM');
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Alarm armed. Press 9 for main menu.</Say></Gather></Response>');
            break;
        case '2':
            items.AlarmSystem.sendCommand('DISARM');
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Alarm disarmed. Press 9 for main menu.</Say></Gather></Response>');
            break;
        case '3':
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Front door is ' + items.FrontDoor.state + '. ' +
                'Back door is ' + items.BackDoor.state + '. ' +
                'Motion sensor is ' + items.MotionSensor.state + '. Press 9 for main menu.</Say>' +
                '</Gather></Response>');
            break;
        case '4':
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Temperature is ' + items.IndoorTemp.state + ' degrees. ' +
                'Humidity is ' + items.IndoorHumidity.state + ' percent. Press 9 for main menu.</Say>' +
                '</Gather></Response>');
            break;
        case '9':
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Main menu. Alarm is ' + items.AlarmSystem.state + '. ' +
                'Press 1 to arm. Press 2 to disarm. Press 3 for sensors. Press 4 for temperature.</Say>' +
                '</Gather></Response>');
            break;
        default:
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Invalid option. Press 9 for main menu.</Say></Gather></Response>');
    }
}).build('Security panel - DTMF handler');
```

:::

::: tab DSL

```java
rule "Handle incoming call - security panel"
when
    Channel "twilio:phone:myaccount:myphone:callReceived" triggered
then
    val json = receivedEvent.getEvent()
    val callSid = transform("JSONPATH", "$.callSid", json)
    val from = transform("JSONPATH", "$.from", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    if (from != "+15559876543" && from != "+15551112222") {
        actions.respondWithTwiml(callSid,
            '<Response><Say>Access denied.</Say></Response>')
        return
    }

    val alarmState = AlarmSystem.state.toString()
    actions.respondWithTwiml(callSid,
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Security panel. Alarm is ' + alarmState + '. ' +
        'Press 1 to arm. Press 2 to disarm. Press 3 for sensor status. ' +
        'Press 4 for temperature. Press 9 to repeat.</Say>' +
        '</Gather><Say>No input. Goodbye.</Say></Response>')
end

rule "Handle DTMF - security panel"
when
    Channel "twilio:phone:myaccount:myphone:dtmfReceived" triggered
then
    val json = receivedEvent.getEvent()
    val digits = transform("JSONPATH", "$.digits", json)
    val callSid = transform("JSONPATH", "$.callSid", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    switch (digits) {
        case "1": {
            AlarmSystem.sendCommand("ARM")
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Alarm armed. Press 9 for main menu.</Say></Gather></Response>')
        }
        case "2": {
            AlarmSystem.sendCommand("DISARM")
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Alarm disarmed. Press 9 for main menu.</Say></Gather></Response>')
        }
        case "3": {
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Front door is ' + FrontDoor.state.toString() + '. ' +
                'Back door is ' + BackDoor.state.toString() + '. ' +
                'Motion sensor is ' + MotionSensor.state.toString() + '. Press 9 for main menu.</Say>' +
                '</Gather></Response>')
        }
        case "4": {
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Temperature is ' + IndoorTemp.state.toString() + ' degrees. ' +
                'Humidity is ' + IndoorHumidity.state.toString() + ' percent. Press 9 for main menu.</Say>' +
                '</Gather></Response>')
        }
        case "9": {
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Main menu. Alarm is ' + AlarmSystem.state.toString() + '. ' +
                'Press 1 to arm. Press 2 to disarm. Press 3 for sensors. Press 4 for temperature.</Say>' +
                '</Gather></Response>')
        }
        default: {
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>Invalid option. Press 9 for main menu.</Say></Gather></Response>')
        }
    }
end
```

:::

::::

### Example: Outgoing Alert Call with Confirmation

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('SmokeDetector').changed().to('ON').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    twilioActions.makeCall('+15559876543',
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Emergency! Smoke detected in your home. Press 1 to acknowledge.</Say>' +
        '</Gather><Say>No response received. We will call again.</Say></Response>');
}).build('Fire alarm call');

rules.when().channel('twilio:phone:myaccount:myphone:dtmfReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    if (payload.digits === '1') {
        items.FireAlarmAcknowledged.postUpdate('ON');
        var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
        twilioActions.respondWithTwiml(payload.callSid,
            '<Response><Say>Acknowledged. Stay safe.</Say></Response>');
    }
}).build('Fire alarm confirmation');
```

:::

::: tab DSL

```java
rule "Fire alarm - call and confirm"
when
    Item SmokeDetector changed to ON
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    actions.makeCall("+15559876543",
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Emergency! Smoke detected in your home. Press 1 to acknowledge.</Say>' +
        '</Gather><Say>No response received. We will call again.</Say></Response>')
end

rule "Handle fire alarm confirmation"
when
    Channel "twilio:phone:myaccount:myphone:dtmfReceived" triggered
then
    val json = receivedEvent.getEvent()
    val digits = transform("JSONPATH", "$.digits", json)
    val callSid = transform("JSONPATH", "$.callSid", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    if (digits == "1") {
        FireAlarmAcknowledged.postUpdate(ON)
        actions.respondWithTwiml(callSid,
            '<Response><Say>Acknowledged. Stay safe.</Say></Response>')
    }
end
```

:::

::::

### Example: Query Room Temperatures by Phone

:::: tabs

::: tab JavaScript

```javascript
rules.when().channel('twilio:phone:myaccount:myphone:callReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    twilioActions.respondWithTwiml(payload.callSid,
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Press 1 for living room. Press 2 for bedroom. Press 3 for kitchen. Press 4 for outside.</Say>' +
        '</Gather></Response>');
}).build('Temperature menu');

rules.when().channel('twilio:phone:myaccount:myphone:dtmfReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');

    var temps = {
        '1': 'Living room: ' + items.LivingRoomTemp.state,
        '2': 'Bedroom: ' + items.BedroomTemp.state,
        '3': 'Kitchen: ' + items.KitchenTemp.state,
        '4': 'Outside: ' + items.OutdoorTemp.state
    };

    var temp = temps[payload.digits];
    if (temp) {
        twilioActions.respondWithTwiml(payload.callSid,
            '<Response><Gather numDigits="1" action="{gatherUrl}">' +
            '<Say>' + temp + ' degrees. Press another number for a different room, or hang up.</Say>' +
            '</Gather></Response>');
    } else {
        twilioActions.respondWithTwiml(payload.callSid,
            '<Response><Say>Invalid room. Goodbye.</Say></Response>');
    }
}).build('Read temperature');
```

:::

::: tab DSL

```java
rule "Temperature menu"
when
    Channel "twilio:phone:myaccount:myphone:callReceived" triggered
then
    val json = receivedEvent.getEvent()
    val callSid = transform("JSONPATH", "$.callSid", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    actions.respondWithTwiml(callSid,
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Press 1 for living room. Press 2 for bedroom. Press 3 for kitchen. Press 4 for outside.</Say>' +
        '</Gather></Response>')
end

rule "Read temperature"
when
    Channel "twilio:phone:myaccount:myphone:dtmfReceived" triggered
then
    val json = receivedEvent.getEvent()
    val digits = transform("JSONPATH", "$.digits", json)
    val callSid = transform("JSONPATH", "$.callSid", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    var temp = ""
    switch (digits) {
        case "1": temp = "Living room: " + LivingRoomTemp.state.toString()
        case "2": temp = "Bedroom: " + BedroomTemp.state.toString()
        case "3": temp = "Kitchen: " + KitchenTemp.state.toString()
        case "4": temp = "Outside: " + OutdoorTemp.state.toString()
    }

    if (temp != "") {
        actions.respondWithTwiml(callSid,
            '<Response><Gather numDigits="1" action="{gatherUrl}">' +
            '<Say>' + temp + ' degrees. Press another number for a different room, or hang up.</Say>' +
            '</Gather></Response>')
    } else {
        actions.respondWithTwiml(callSid,
            '<Response><Say>Invalid room. Goodbye.</Say></Response>')
    }
end
```

:::

::::

### Timeout Behavior

If a rule does not call `respondWithTwiml` within the configured `responseTimeout` (default: 10 seconds), the binding returns the default TwiML from the thing configuration:

- For incoming calls: the `voiceGreeting` config parameter
- For DTMF gather: the `gatherResponse` config parameter

This means existing rules that don't use `respondWithTwiml` continue to work as before.
The timeout is configurable per phone thing via the `responseTimeout` advanced parameter (1-14 seconds).

## Webhook Setup

To receive incoming messages and calls, you need to configure webhooks so Twilio can reach your openHAB instance.

### URL Structure

All webhook and media endpoints are served under `/twilio/callback/` on your openHAB instance.
If using a reverse proxy, you must forward this entire path prefix.

| Path | Method | Purpose |
|------|--------|---------|
| `/twilio/callback/{thingUID}/sms` | POST | Incoming SMS/MMS messages |
| `/twilio/callback/{thingUID}/whatsapp` | POST | Incoming WhatsApp messages |
| `/twilio/callback/{thingUID}/voice` | POST | Incoming voice calls |
| `/twilio/callback/{thingUID}/gather` | POST | DTMF digit gather callbacks |
| `/twilio/callback/{thingUID}/status` | POST | Message/call status updates |
| `/twilio/callback/media/{uuid}` | GET | Temporary media serving (for MMS/WhatsApp attachments) |
| `/twilio/callback/twiml/{uuid}` | GET/POST | Temporary TwiML serving (for call flow chaining) |

The `{thingUID}` is the full thing UID (e.g. `twilio:phone:myaccount:myphone`).
The `{uuid}` is a randomly generated identifier for temporary media/TwiML entries.

**Example full URLs** (assuming `publicUrl` is `https://my.domain.com`):

```
https://my.domain.com/twilio/callback/twilio:phone:myaccount:myphone/sms
https://my.domain.com/twilio/callback/twilio:phone:myaccount:myphone/voice
https://my.domain.com/twilio/callback/media/550e8400-e29b-41d4-a716-446655440000
```

### Reverse Proxy Configuration

If you use a reverse proxy (nginx, Apache, Caddy, etc.), you must forward all requests under `/twilio/callback/` to openHAB.

**nginx example:**

```nginx
location /twilio/callback/ {
    proxy_pass http://localhost:8080/twilio/callback/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### Option 1: Manual Configuration

1. Set the `publicUrl` on the bridge (e.g. `https://my.domain.com`)
2. Create a phone thing — the webhook URLs will appear as thing properties in the UI
3. Copy the URLs and paste them into the [Twilio Console](https://console.twilio.com/) under your phone number's configuration:
   - **Messaging > A Message Comes In**: paste the `smsWebhookUrl` property value
   - **Voice & Fax > A Call Comes In**: paste the `voiceWebhookUrl` property value

### Option 2: Auto-Configure

1. Set the `publicUrl` on the bridge
2. Enable `autoConfigureWebhooks` on the bridge
3. When a phone thing initializes, it will automatically configure the webhook URLs on the Twilio phone number via the API

## Full Example

### Thing Configuration

```java
Bridge twilio:account:myaccount "Twilio Account" [ accountSid="ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", authToken="your_auth_token", publicUrl="https://my.domain.com" ] {
    Thing phone myphone "My Twilio Number" [ phoneNumber="+15551234567" ]
}
```

### Item Configuration

```java
String TwilioLastMessage "Last SMS [%s]" { channel="twilio:phone:myaccount:myphone:lastMessageBody" }
String TwilioLastFrom "From [%s]" { channel="twilio:phone:myaccount:myphone:lastMessageFrom" }
DateTime TwilioLastDate "Received [%1$tF %1$tR]" { channel="twilio:phone:myaccount:myphone:lastMessageDate" }
String TwilioLastCallFrom "Last Caller [%s]" { channel="twilio:phone:myaccount:myphone:lastCallFrom" }
String TwilioLastDtmf "DTMF [%s]" { channel="twilio:phone:myaccount:myphone:lastDtmfDigits" }
```

### Rule Examples

#### Send SMS Alert

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('FrontDoor').changed().to('OPEN').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    twilioActions.sendSMS('+15559876543', 'Alert: Front door was opened at ' + time.ZonedDateTime.now().toString());
}).build('Door opened alert');
```

:::

::: tab DSL

```java
rule "Door opened alert"
when
    Item FrontDoor changed to OPEN
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    actions.sendSMS("+15559876543", "Alert: Front door was opened at " + now.toString())
end
```

:::

::::

#### Send MMS with openHAB Image Item

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('MotionSensor').changed().to('ON').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    var mediaUrl = twilioActions.createItemMediaUrl('SecurityCamera');
    if (mediaUrl !== null) {
        twilioActions.sendSMS('+15559876543', 'Motion detected!', mediaUrl);
    }
}).build('Motion detected - send snapshot');
```

:::

::: tab DSL

```java
rule "Motion detected - send camera snapshot"
when
    Item MotionSensor changed to ON
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    val mediaUrl = actions.createItemMediaUrl("SecurityCamera")
    if (mediaUrl !== null) {
        actions.sendSMS("+15559876543", "Motion detected!", mediaUrl)
    }
end
```

:::

::::

#### Send MMS with Local Camera URL (Proxy)

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('Doorbell').changed().to('ON').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    var mediaUrl = twilioActions.createProxyMediaUrl('http://192.168.1.100/snapshot.jpg');
    if (mediaUrl !== null) {
        twilioActions.sendSMS('+15559876543', 'Someone is at the door!', mediaUrl);
    }
}).build('Doorbell snapshot');
```

:::

::: tab DSL

```java
rule "Doorbell pressed"
when
    Item Doorbell changed to ON
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    val mediaUrl = actions.createProxyMediaUrl("http://192.168.1.100/snapshot.jpg")
    if (mediaUrl !== null) {
        actions.sendSMS("+15559876543", "Someone is at the door!", mediaUrl)
    }
end
```

:::

::::

#### Make TTS Call for Critical Alert

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('SmokeDetector').changed().to('ON').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    twilioActions.makeTTSCall('+15559876543',
        'Warning. Smoke has been detected in your home. Please check immediately.');
}).build('Smoke alarm call');
```

:::

::: tab DSL

```java
rule "Smoke alarm"
when
    Item SmokeDetector changed to ON
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    actions.makeTTSCall("+15559876543", "Warning. Smoke has been detected in your home. Please check immediately.")
end
```

:::

::::

#### Receive SMS and Reply

:::: tabs

::: tab JavaScript

```javascript
rules.when().channel('twilio:phone:myaccount:myphone:smsReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    if (payload.body.toLowerCase().includes('status')) {
        var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
        twilioActions.sendSMS(payload.from,
            'All systems normal. Temperature: ' + items.IndoorTemp.state + ' F');
    }
}).build('Handle incoming SMS');
```

:::

::: tab DSL

```java
rule "Handle incoming SMS"
when
    Channel "twilio:phone:myaccount:myphone:smsReceived" triggered
then
    val json = receivedEvent.getEvent()
    val body = transform("JSONPATH", "$.body", json)
    val from = transform("JSONPATH", "$.from", json)

    if (body.toLowerCase().contains("status")) {
        val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
        actions.sendSMS(from, "All systems normal. Temperature: " + IndoorTemp.state + " F")
    }
end
```

:::

::::

#### Handle DTMF Input with Dynamic Response

:::: tabs

::: tab JavaScript

```javascript
rules.when().channel('twilio:phone:myaccount:myphone:dtmfReceived').triggered().then(event => {
    var payload = JSON.parse(event.receivedEvent);
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');

    switch (payload.digits) {
        case '1':
            items.AlarmSystem.sendCommand('ARM');
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Say>Alarm armed. Goodbye.</Say></Response>');
            break;
        case '2':
            items.AlarmSystem.sendCommand('DISARM');
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Say>Alarm disarmed. Goodbye.</Say></Response>');
            break;
        case '3':
            items.AllLights.sendCommand('ON');
            twilioActions.respondWithTwiml(payload.callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>All lights on. Press 9 to return to menu.</Say></Gather></Response>');
            break;
    }
}).build('Handle DTMF');
```

:::

::: tab DSL

```java
rule "Handle DTMF"
when
    Channel "twilio:phone:myaccount:myphone:dtmfReceived" triggered
then
    val json = receivedEvent.getEvent()
    val digits = transform("JSONPATH", "$.digits", json)
    val callSid = transform("JSONPATH", "$.callSid", json)
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")

    switch (digits) {
        case "1": {
            AlarmSystem.sendCommand("ARM")
            actions.respondWithTwiml(callSid,
                '<Response><Say>Alarm armed. Goodbye.</Say></Response>')
        }
        case "2": {
            AlarmSystem.sendCommand("DISARM")
            actions.respondWithTwiml(callSid,
                '<Response><Say>Alarm disarmed. Goodbye.</Say></Response>')
        }
        case "3": {
            AllLights.sendCommand(ON)
            actions.respondWithTwiml(callSid,
                '<Response><Gather numDigits="1" action="{gatherUrl}">' +
                '<Say>All lights on. Press 9 to return to menu.</Say></Gather></Response>')
        }
    }
end
```

:::

::::

#### Outgoing Emergency Call with Confirmation

:::: tabs

::: tab JavaScript

```javascript
rules.when().item('PanicButton').changed().to('ON').then(event => {
    var twilioActions = actions.thingActions('twilio', 'twilio:phone:myaccount:myphone');
    twilioActions.makeCall('+15559876543',
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Emergency alert from your home. Press 1 to acknowledge. Press 2 to trigger siren.</Say>' +
        '</Gather><Say>No response. Calling again.</Say></Response>');
}).build('Emergency call');
```

:::

::: tab DSL

```java
rule "Emergency call with menu"
when
    Item PanicButton changed to ON
then
    val actions = getActions("twilio", "twilio:phone:myaccount:myphone")
    actions.makeCall("+15559876543",
        '<Response><Gather numDigits="1" action="{gatherUrl}">' +
        '<Say>Emergency alert from your home. Press 1 to acknowledge. Press 2 to trigger siren.</Say>' +
        '</Gather><Say>No response. Calling again.</Say></Response>')
end
```

:::

::::

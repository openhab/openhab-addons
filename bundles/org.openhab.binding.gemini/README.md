# Gemini Binding

The openHAB Gemini Binding allows openHAB to communicate with the Gemini AI Model using the Google Vertex AI SDK.

Gemini is a powerful natural language processing (NLP) tool that can be used to understand and respond to a wide range of text-based commands and questions. 
With this binding, you can use Gemini to formulate proper sentences for any kind of information that you would like to output.

## Supported Things

The binding supports a single thing type `account`, which corresponds to the Google service account that is to be used for the integration.

## Thing Configuration

The `account` thing requires service account key file (json), which is downloaded from the Google cloud console.
Billing needs to be activated for the account.
There is a free contingent for queries, but costs could arise: <https://cloud.google.com/vertex-ai/generative-ai/pricing>

Set up your account for Vertex AI: <https://cloud.google.com/vertex-ai/docs/featurestore/setup>.
Then go the cloud console, create a service account, export the key file as json.
Copy it to the folder `misc` of the openHAB config directory.

| Name            | Type    | Description                                               | Default                                    | Required | Advanced |
|-----------------|---------|-----------------------------------------------------------|--------------------------------------------|----------|----------|
| keyFile         | text    | Service account key file (json) exported from cloud management console (in `misc` folder, without path) | N/A                                        | yes      | no       |
| location        | text    | Server location, https://cloud.google.com/vertex-ai/generative-ai/docs/learn/locations | europe-west3 | yes       | no      |
|

## Channels

The `account` thing comes with a single channel `chat` of type `chat`.
It is possible to extend the thing with further channels of type `chat`, so that different configurations can be used concurrently.

| Channel | Type   | Read/Write | Description                                                                        |
|---------|--------|------------|------------------------------------------------------------------------------------|
| chat    | String | RW         | This channel takes prompts as commands and delivers the response as a state update |

Each channel of type `chat` takes the following configuration parameters:

| Name            | Type    | Description                                                                                                                                                | Default       | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|----------|----------|
| model           | text    | The model to be used for the responses.                                                                                                                    | gemini-pro-vision | no       | no       |


## Full Example

### Thing Configuration

```java
Thing gemini:account:1 [ keyFile="<your api key file here>", location="europe-west3" ] {
    Channels:
        Type chat : chat "Weather Advice" [
            model="gemini-pro-vision"
        ]
}
```

### Item Configuration

```java
String Weather_Announcement { channel="gemini:account:1:chat" }
```

### Example Rules

```java
rule "Good morning"
when
  Time cron "0 0 7 * * *"
then
    Weather_Announcement.sendCommand("Tell me about the weather on planet mars today")
end
```

Assuming the rule is executed, it results e.g. in:

```
Item 'Weather_Announcement' received command Tell me about the weather on planet mars today
2024-05-31 18:30:01.912 [INFO ] [openhab.event.ItemStateChangedEvent ] - Item 'Weather_Announcement' changed from NULL to I do not have access to real-time information and my knowledge cutoff is April 2023, so I cannot provide an accurate weather report for Mars today.

For the most up-to-date weather conditions on Mars, please refer to NASA's Mars Weather Report website or other reputable sources.
```

The state updates can be used for a text-to-speech output, and they will give your announcements at home a personal touch.

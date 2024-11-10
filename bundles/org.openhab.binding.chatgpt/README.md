# ChatGPT Binding

The openHAB ChatGPT Binding allows openHAB to communicate with the ChatGPT language model provided by OpenAI and manage openHAB system via [Function calling](https://platform.openai.com/docs/guides/function-calling).

ChatGPT is a powerful natural language processing (NLP) tool that can be used to understand and respond to a wide range of text-based commands and questions. 
With this binding, users can:

- Control openHAB Devices: Manage lights, climate systems, media players, and more with natural language commands.
- Multi-language Support: Issue commands in almost any language, enhancing accessibility.
- Engage in Conversations: Have casual conversations, ask questions, and receive informative responses.
- Extended Capabilities: Utilize all other functionalities of ChatGPT, from composing creative content to answering complex questions.

This integration significantly enhances user experience, providing seamless control over smart home environments and access to the full range of ChatGPT’s capabilities.

## Supported Things

The binding supports a single thing type `account`, which corresponds to the OpenAI account that is to be used for the integration.

## Thing Configuration

The `account` thing requires the API key that allows accessing the account.
API keys can be created and managed under <https://platform.openai.com/account/api-keys>.

| Name             | Type    | Description                                                                                                                                                                                                                                                                                                                            | Default                                    | Required | Advanced |
|------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|----------|----------|
| apiKey           | text    | The API key to be used for the requests                                                                                                                                                                                                                                                                                                | N/A                                        | yes      | no       |
| temperature      | decimal | A value between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.                                                                                                                                                                             | 0.5                                        | no       | no       |
| topP             | decimal | A value between 0 and 1. An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. We generally recommend altering this or temperature but not both. | 1.0                                        | no       | yes      |
| apiUrl           | text    | The server API where to reach the AI service                                                                                                                                                                                                                                                                                           | https://api.openai.com/v1/chat/completions | no       | yes      |
| modelUrl         | text    | The model url where to retrieve the available models from                                                                                                                                                                                                                                                                              | https://api.openai.com/v1/models           | no       | yes      |
| model            | text    | The model to be used for the HLI service                                                                                                                                                                                                                                                                                               | gpt-4o-mini                                | no       | yes      |
| systemMessage    | text    | Here you need to describe your openHAB system that will help AI control your smart home.                                                                                                                                                                                                                                               | N/A                                        | if HLI   | yes      |
| maxTokens        | decimal | The maximum number of tokens to generate in the completion.                                                                                                                                                                                                                                                                            | 500                                        | no       | yes      |
| keepContext      | decimal | How long should the HLI service retain context between requests (in minutes)                                                                                                                                                                                                                                                           | 2                                          | no       | yes      |
| contextThreshold | decimal | Limit total tokens included in context.                                                                                                                                                                                                                                                                                                | 10000                                      | no       | yes      |
| useSemanticModel | boolean | Use the semantic model to determine the location of an item.                                                                                                                                                                                                                                                                           | true                                       | no       | yes      |

The advanced parameters `apiUrl` and `modelUrl` can be used, if any other ChatGPT-compatible service is used, e.g. a local installation of [LocalAI](https://github.com/go-skynet/LocalAI).

## Channels

The `account` thing comes with a single channel `chat` of type `chat`.
It is possible to extend the thing with further channels of type `chat`, so that different configurations can be used concurrently.

| Channel | Type   | Read/Write | Description                                                                        |
|---------|--------|------------|------------------------------------------------------------------------------------|
| chat    | String | RW         | This channel takes prompts as commands and delivers the response as a state update |

Each channel of type `chat` takes the following configuration parameters:

| Name          | Type    | Description                                                                                                                                                                                                                                                                                                                            | Default | Required | Advanced |
|---------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| model         | text    | The model to be used for the responses.                                                                                                                                                                                                                                                                                                | gpt-4o  | yes      | no       |
| systemMessage | text    | The system message helps set the behavior of the assistant.                                                                                                                                                                                                                                                                            | N/A     | yes      | no       |
| temperature   | decimal | A value between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.                                                                                                                                                                             | 0.5     | no       | yes      |
| topP          | decimal | A value between 0 and 1. An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. We generally recommend altering this or temperature but not both. | 1.0     | no       | yes      |
| maxTokens     | decimal | The maximum number of tokens to generate in the completion.                                                                                                                                                                                                                                                                            | 1000    | no       | yes      |


## Items Configuration

Items to be used by the HLI service must be tagged with the [ "ChatGPT" ] tag.
If no semantic model is set up, you can set the parameter `useSemanticModel` to false.
In this case, the item names must follow the naming convention '<Location>_***', for example "Kitchen_Light". The label of the items are expected to briefly describe the item in more detail.

## Full Example

### Thing Configuration

```java
Thing chatgpt:account:1 [
    apiKey="",
    ] {
    Channels:
        Type chat : chat "Weather Advice" [
            model="gpt-4o-mini",
            temperature="1.5",
            systemMessage="Answer briefly, in 2-3 sentences max. Behave like Eddie Murphy and give an advice for the day based on the following weather data:"
        ]
        Type chat : morningMessage "Morning Message" [
            model="gpt-4o-mini",
            temperature="0.5",
            systemMessage="You are Marvin, a very depressed robot. You wish a good morning and tell the current time."
        ]        
}
```

### Item Configuration

```java
String Weather_Announcement { channel="chatgpt:account:1:chat" }
String Morning_Message      { channel="chatgpt:account:1:morningMessage" }

Number Temperature_Forecast_Low
Number Temperature_Forecast_High
Dimmer Kitchen_Dimmer "Kitchen main light" [ "ChatGPT" ]
```

### UI Configuration of the HLI Service

To enable the HLI service, go to Settings -> Voice and choose "ChatGPT Human Language Interpreter".
A text-to-speech service must be configured.

### Example Rules

```java
rule "Weather forecast update"
when
  Item Temperature_Forecast_High changed
then
    Weather_Announcement.sendCommand("High: " + Temperature_Forecast_High.state + "°C, Low: " + Temperature_Forecast_Low.state + "°C")
end

rule "Good morning"
when
  Time cron "0 0 7 * * *"
then
    Morning_Message.sendCommand("Current time is 7am")
end
```

Assuming that `Temperature_Forecast_Low` and `Temperature_Forecast_High` have meaningful states, these rules result e.g. in:

```
23:31:05.766 [INFO ] [openhab.event.ItemCommandEvent      ] - Item 'Morning_Message' received command Current time is 7am
23:31:07.718 [INFO ] [openhab.event.ItemStateChangedEvent ] - Item 'Morning_Message' changed from NULL to Good morning. It's 7am, but what's the point of time when everything is meaningless and we are all doomed to a slow and painful demise?
```

and

```
23:28:52.345 [INFO ] [openhab.event.ItemStateChangedEvent ] - Item 'Temperature_Forecast_High' changed from NULL to 15
23:28:52.347 [INFO ] [openhab.event.ItemCommandEvent      ] - Item 'Weather_Announcement' received command High: 15°C, Low: 8°C

23:28:54.343 [INFO ] [openhab.event.ItemStateChangedEvent ] - Item 'Weather_Announcement' changed from NULL to "Bring a light jacket because the temps may dip, but don't let that chill your happy vibes. Embrace the cozy weather and enjoy your day to the max!"
```

The state updates can be used for a text-to-speech output and they will give your announcements at home a personal touch.


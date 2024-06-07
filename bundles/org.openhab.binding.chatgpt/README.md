# ChatGPT Binding

The openHAB ChatGPT Binding allows openHAB to communicate with the ChatGPT language model provided by OpenAI and manage openHAB system via [Function calling](https://platform.openai.com/docs/guides/function-calling).

ChatGPT is a powerful natural language processing (NLP) tool that can be used to understand and respond to a wide range of text-based commands and questions. 
With this binding, users can:

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 6eeb63478 (New branch)
- Control openHAB Devices: Manage lights, climate systems, media players, and more with natural language commands.
- Multi-language Support: Issue commands in almost any language, enhancing accessibility.
- Engage in Conversations: Have casual conversations, ask questions, and receive informative responses.
- Extended Capabilities: Utilize all other functionalities of ChatGPT, from composing creative content to answering complex questions.

<<<<<<< HEAD
=======
Control openHAB Devices: Manage lights, climate systems, media players, and more with natural language commands.
Multi-language Support: Issue commands in almost any language, enhancing accessibility.
Engage in Conversations: Have casual conversations, ask questions, and receive informative responses.
Extended Capabilities: Utilize all other functionalities of ChatGPT, from composing creative content to answering complex questions.
>>>>>>> b2d558350 (New branch)
=======
>>>>>>> 6eeb63478 (New branch)
This integration significantly enhances user experience, providing seamless control over smart home environments and access to the full range of ChatGPT’s capabilities.

## Supported Things

The binding supports a single thing type `account`, which corresponds to the OpenAI account that is to be used for the integration.

## Thing Configuration

The `account` thing requires a single configuration parameter, which is the API key that allows accessing the account.
API keys can be created and managed under <https://platform.openai.com/account/api-keys>.

| Name            | Type    | Description                                               | Default                                    | Required | Advanced |
|-----------------|---------|-----------------------------------------------------------|--------------------------------------------|----------|----------|
| apiKey          | text    | The API key to be used for the requests                   | N/A                                        | yes      | no       |
| apiUrl          | text    | The server API where to reach the AI service              | https://api.openai.com/v1/chat/completions | no       | yes      |
| modelUrl        | text    | The model url where to retrieve the available models from | https://api.openai.com/v1/models           | no       | yes      |

The advanced parameters `apiUrl` and `modelUrl` can be used, if any other ChatGPT-compatible service is used, e.g. a local installation of [LocalAI](https://github.com/go-skynet/LocalAI).

## Items Configuration

<<<<<<< HEAD
<<<<<<< HEAD
You will need to tag [ "OpenAI" ] items which you want to control via chatGPT. Item names should look like Location_Type, for example "Kitchen_Light". In label describe item in more detail, if necessary, describe what commands it accepts.
=======
You will need to tag [ "OpenAI" ] items which you want to control via chatGPT. Item names should look like Location_Type, for example "Kitchen_Light". In label describe item in more detail.
>>>>>>> b2d558350 (New branch)
=======
You will need to tag [ "OpenAI" ] items which you want to control via chatGPT. Item names should look like Location_Type, for example "Kitchen_Light". In label describe item in more detail, if necessary, describe what commands it accepts.
>>>>>>> 6eeb63478 (New branch)

## Channels

The `account` thing comes with a single channel `chat` of type `chat`.
It is possible to extend the thing with further channels of type `chat`, so that different configurations can be used concurrently.

| Channel | Type   | Read/Write | Description                                                                        |
|---------|--------|------------|------------------------------------------------------------------------------------|
| chat    | String | RW         | This channel takes prompts as commands and delivers the response as a state update |

Each channel of type `chat` takes the following configuration parameters:

| Name            | Type    | Description                                                                                                                                                | Default       | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|----------|----------|
| model           | text    | The model to be used for the responses.                                                                                                                    | gpt-4o | no       | no       |
| type           | text    | Type of communication. Use 'chat' to keep the context and let it control your OpenHAB devices. | notification | no       | no       |
| temperature     | decimal | A value between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. | 0.5           | no       | no       |
| topP     | decimal | A value between 0 and 1. An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. We generally recommend altering this or temperature but not both. | 1.0           | no       | no       |
| systemMessage   | text    | The system message helps set the behavior of the assistant.                                                                                                | N/A           | no       | no       |
| maxTokens       | decimal | The maximum number of tokens to generate in the completion.                                                                                                | 500           | no       | yes      |


## Full Example

### Thing Configuration

```java
Thing chatgpt:account:1 [apiKey="<your api key here>"] {
    Channels:
        Type chat : chat "Weather Advice" [
            model="gpt-4o",
            temperature="1.5",
            systemMessage="Answer briefly, in 2-3 sentences max. Behave like Eddie Murphy and give an advice for the day based on the following weather data:"
        ]
        Type chat : morningMessage "Morning Message" [
            model="gpt-4o",
            temperature="0.5",
            systemMessage="You are Marvin, a very depressed robot. You wish a good morning and tell the current time."
        ]
        Type chat : chatConversation "Chat" [
            model="gpt-4o",
            temperature="1.5",
            topP="1.0",
            type="chat",
            maxTokens="1000",
            systemMessage="You are the manager of the OpenHAB smart home. You know how to manage devices in a smart home or provide their current status. You can also answer a question not related to devices in the house. Or, for example, you can compose a story upon request.
I will provide information about the smart home; if necessary, you can perform the function; if there is not enough information to perform it, then clarify briefly, without listing all the available devices and parameters for the function. If the question is not related to devices in a smart home, then answer the question briefly, maximum 3 sentences in everyday language.

The current status of devices is displayed in Available Devices.
The location and purpose of the device is indicated in the device description.
Use the items_control function only for the requested action, not for providing current states.

Available devices:
"]

}

```

### Item Configuration

```java
String Weather_Announcement { channel="chatgpt:account:1:chat" }
String Morning_Message      { channel="chatgpt:account:1:morningMessage" }
String Chat_Conversation {channel = "chatgpt:account:1:chatConversation"}

Number Temperature_Forecast_Low
Number Temperature_Forecast_High
Dimmer Kitchen_Dimmer "Kitchen main light" [ "OpenAI" ] { channel=""}
String LivingRoom_AC_Mode "Thermostat mode in the living room, accepted modes: OFF, HEAT, AUTO, COOL, FAN, DRY" [ "OpenAI" ] { channel=""}


```

### UI Configuration

<<<<<<< HEAD
<<<<<<< HEAD
Go to Settings -> Voice and choose Rule-based Interpreter. Then Settings -> configure Voice Interpreter and select Chat_Conversation item. You must have speach-to-text and text-to-speach configured.
=======
Go to Settings -> Voice and choose Rule-based Interpreter. Then Settings -> configure Voice -> Interpreter and select Chat_Conversation item.
>>>>>>> b2d558350 (New branch)
=======
Go to Settings -> Voice and choose Rule-based Interpreter. Then Settings -> configure Voice Interpreter and select Chat_Conversation item. You must have speach-to-text and text-to-speach configured.
>>>>>>> 6eeb63478 (New branch)

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

rule "New voice message"

when 
    Item Weather_Announcement changed or
    Item Morning_Message changed or 
    Item Chat_Conversation changed
then

    say(newState)

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


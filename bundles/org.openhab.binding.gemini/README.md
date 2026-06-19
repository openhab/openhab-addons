# Google Gemini Binding

The openHAB Google Gemini Binding allows openHAB to communicate with the Gemini family of large language models (LLM) from Google.

Google Gemini is a powerful natural language processing (NLP) tool that can be used to understand and respond to a wide range of text-based commands and questions.
This binding provides the following features:

- Control openHAB with natural language commands: Turn on the lights, control the TV or check the status of the garage door.
- Multi-Language Support: Issue commands and ask questions in almost any language.
- Chat with a LLM: Ask questions and receive informational responses.

## Supported Things

The binding supports a single thing type `account`, which corresponds to the Google Gemini account that is to be used for the integration.

## Discovery

Discovery is not supported.
Things must be added manually.

## Thing Configuration

The `account` thing requires the API key that allows accessing the account.
API keys can be created and managed under Google AI Studio: <https://aistudio.google.com/app/apikey>.

| Name            | Type    | Description                                                                                                                              | Default          | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------------------------------------------------------------------|------------------|----------|----------|
| apiKey          | text    | The API key to authenticate against the Gemini API.                                                                                      | N/A              | yes      | no       |
| requestTimeout  | integer | Timeout in seconds for chat API requests.                                                                                                | 30               | no       | yes      |
| model           | text    | The model to be used ([Models](https://ai.google.dev/gemini-api/docs/models), [Pricing](https://ai.google.dev/gemini-api/docs/pricing)). | gemini-2.5-flash | no       | no       |
| temperature     | decimal | A value between 0.0 and 1.0, where higher values make the output more random and lower values make it more focused and deterministic.    | 1.0              | no       | yes      |
| topP            | decimal | A value between 0.0 and 1.0 for nucleus sampling, where the model considers the results of the tokens with topP probability mass.        | 1.0              | no       | yes      |
| maxOutputTokens | integer | The maximum number of tokens to include in a candidate.                                                                                  | 2048             | no       | yes      |

It is generally recommended to either alter temperature or topP, but not both.
For Gemini 3.x models, Google recommends keeping both values at their default.

## Channels

The `account` thing comes with a single `chat` channel of type `chat` by default.
Additional channels can be added to the thing, allowing specifying different models and parameters for each channel.

Channels of type `chat` take the following configuration parameters:

| Name            | Type    | Description                                                                                    | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------------------------|----------|----------|
| model           | text    | The model to be used for the responses, overriding the thing-level model.                      | no       | no       |
| systemMessage   | text    | The system message helps set the behavior of the assistant.                                    | no       | no       |
| temperature     | decimal | A value between 0.0 and 2.0, which overrides the thing-level temperature.                      | no       | yes      |
| topP            | decimal | A value between 0.0 and 1.0, which overrides the thing-level topP.                             | no       | yes      |
| maxOutputTokens | integer | The maximum number of tokens to include in a candidate, overriding the thing-level max tokens. | no       | yes      |

Channel configuration defaults to the [Thing configuration](#thing-configuration), except for `systemMessage`, which defaults to _You are a helpful assistant_.

## Thing Actions

The binding provides actions to interact with the Google Gemini API directly from rules.
The first parameter when retrieving actions must always be `gemini` and the second must be the full Thing UID of the Gemini account.

You can retrieve the actions as follows:

:::: tabs

::: tab DSL

```java
val geminiActions = getActions("gemini", "gemini:account:myaccount")
```

:::

::: tab JS

```javascript
var geminiActions = actions.thingActions("gemini", "gemini:account:myaccount");
```

:::

::::

### Available Actions

The `account` Thing provides the following actions:

| Action Signature                                                                                                                                   | Return Type | Description                                                                                                                                                                                    |
|----------------------------------------------------------------------------------------------------------------------------------------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `sendMessage(String prompt)`                                                                                                                       | `String`    | Sends a prompt to Gemini using the Thing's configured model and parameters, and returns the response text, or `null` if the request failed.                                                    |
| `sendMessage(String prompt, String model)`                                                                                                         | `String`    | Sends a prompt to Gemini using the specified model, falling back to default parameters, and returns the response text, or `null` if the request failed.                                        |
| `sendMessage(String prompt, String model, String systemMessage, Double temperature, Double topP, Integer maxOutputTokens, Integer requestTimeout)` | `String`    | Sends a prompt with detailed generation settings (where null values fall back to [Thing configuration](#thing-configuration)), and returns the response text, or `null` if the request failed. |

### Examples

:::: tabs

::: tab DSL

```java
val geminiActions = getActions("gemini", "gemini:account:myaccount")

// 1. Simple sendMessage
val response1 = geminiActions.sendMessage("What is the capital of France?")
logInfo("Gemini", "Response 1: " + response1)

// 2. sendMessage with model override
val response2 = geminiActions.sendMessage("Write a poem about openHAB.", "gemini-2.5-flash")
logInfo("Gemini", "Response 2: " + response2)

// 3. sendMessage with custom parameters (use null for default values)
val response3 = geminiActions.sendMessage(
    "How does electricity work?",
    "gemini-2.5-flash",
    "Explain it to a 5-year-old.", // system message
    0.7,  // temperature
    null, // topP (default)
    500,  // maxOutputTokens
    60    // requestTimeout
)
logInfo("Gemini", "Response 3: " + response3)
```

:::

::: tab JS

```javascript
const geminiActions = actions.thingActions("gemini", "gemini:account:myaccount");

// 1. Simple sendMessage
const response1 = geminiActions.sendMessage("What is the capital of France?");
console.info("Gemini Response 1: " + response1);

// 2. sendMessage with model override
const response2 = geminiActions.sendMessage("Write a poem about openHAB.", "gemini-2.5-flash");
console.info("Gemini Response 2: " + response2);

// 3. sendMessage with custom parameters (use null for default values)
const response3 = geminiActions.sendMessage(
    "How does electricity work?",
    "gemini-2.5-flash",
    "Explain it to a 5-year-old.", // system message
    0.7,  // temperature
    null, // topP (default)
    500,  // maxOutputTokens
    60    // requestTimeout
);
console.info("Gemini Response 3: " + response3);
```

:::

::::

## Human Language Interpreter

The `account` Thing automatically registers a human language interpreter implementation.

To configure the Gemini HLI as default, go to _Settings_ → _Voice_ and select _Gemini Human Language Interpreter_ as default.

In that place, you can also configure the system prompt used to instruct the LLM on how to process the user's input.
The used model, temperature, topP and maximum output tokens parameters can be configured in the thing configuration.

For more information on human language interpreters, refer to the [Voice documentation]({{base}}/docs/configuration/multimedia.html#human-language-interpreter).

## Full Example

### Thing Configuration

```java
Thing gemini:account:myaccount [apiKey="xxx-yyy-zzz", model="gemini-2.5-flash", temperature=1.0, topP=1.0, maxOutputTokens=2048, requestTimeout=30] {
  Channels:
    Type chat : chat "Chat" [model="gemini-2.5-flash", temperature=1.0, topP=1.0, maxOutputTokens=2048, systemMessage="You are a helpful assistant."]
}
```

Additional channels of type `chat` can be added to the thing.

### Item Configuration

```java
String GeminiChat "Gemini Chat" { channel="gemini:account:myaccount:chat" }
```

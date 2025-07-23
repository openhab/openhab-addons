# OpenAI Text-to-Speech

The OpenAI TTS (Text-to-Speech) add-on for openHAB allows you to integrate OpenAI's Text-to-Speech capabilities into your openHAB system.
The advantage of this service over others is that one selected voice can speak different languages.
This is useful, for example, in conjunction with ChatGPT binding, which will help in learning foreign languages.
You can find the price for this service here - <https://openai.com/api/pricing/>

## Configuration

To configure the OpenAI TTS, **Settings / Other Services - OpenAI Text-to-Speech** and set:

- **apiKey** - The API key to be used for the requests.
- **apiUrl** - The server API where to reach the AI TTS service.
- **model**  - The ID of the model to use for TTS.

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

- Go to **Settings**.
- Edit **System Services - Voice**.
- Set **OpenAI TTS Service** as **Default Text-to-Speech**.
- Choose your preferred **Default Voice** for your setup.

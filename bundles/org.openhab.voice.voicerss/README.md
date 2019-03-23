# VoiceRSS Text-to-Speech

## Overview

VoiceRSS is an Internet based TTS service hosted at <http://api.voicerss.org>.
You must obtain an API Key to get access to this service.
The free version allows you to make up to 350 requests per day; for more you may need a commercial subscription.

For more information, see <http://www.voicerss.org/>.

## Samples

Replace API_KEY with your personal API key for simple testing of different API calls:

```
# EN
https://api.voicerss.org/?key=API_KEY&hl=en-us&src=Hello%20World
https://api.voicerss.org/?key=API_KEY&hl=en-us&c=WAV&src=Hello%20World
https://api.voicerss.org/?key=API_KEY&hl=en-us&f=44khz_16bit_mono&src=Hello%20World
https://api.voicerss.org/?key=API_KEY&hl=en-gb&f=44khz_16bit_stereo&src=Hello%20World

# DE
https://api.voicerss.org/?key=API_KEY&hl=de-de&f=44khz_16bit_mono&src=Hallo%20Welt
```

## Configuration

You must add your API_KEY to your configuration by adding a file "voicerss.cfg" to the services folder, with this entry:

```
apiKey=1234567890
```

It actually supports only one voice: "voicerss:default", which is configured to use 44kHz, mono, 16 bit sampling quality.

## Caching

The VoiceRSS extension does cache audio files from previous requests, to reduce traffic, improve performance, reduce number of requests and provide same time offline capability.

For convenience, there is a tool where the audio cache can be generated in advance, to have a prefilled cache when starting this extension.
You have to copy the generated data to your userdata/voicerss/cache folder.

Synopsis of this tool:

```
Usage: java org.openhab.voice.voicerss.tool.CreateTTSCache <args>
Arguments: --api-key <key> <cache-dir> <locale> { <text> | @inputfile }
  key       the VoiceRSS API Key, e.g. "123456789"
  cache-dir is directory where the files will be stored, e.g. "voicerss-cache"
  locale    the language locale, has to be valid, e.g. "en-us", "de-de"
  text      the text to create audio file for, e.g. "Hello World"
  inputfile a name of a file, where all lines will be translatet to text, e.g. "@message.txt"

Sample: java org.openhab.voice.voicerss.tool.CreateTTSCache --api-key 1234567890 cache en-US @messages.txt
```


## Open Issues

*   add all media formats
*   add all supported languages
*   do not log API-Key in plain text

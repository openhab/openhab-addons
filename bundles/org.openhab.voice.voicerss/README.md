# VoiceRSS Text-to-Speech

VoiceRSS is an Internet based TTS service hosted at <https://api.voicerss.org>.
You must obtain an API Key to get access to this service.
The free version allows you to make up to 350 requests per day; for more you may need a commercial subscription.

For more information, see <https://www.voicerss.org>.

## Obtaining Credentials

Before you can integrate this service, you need to register to https://www.voicerss.org with at least a free account to get an API key.

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

The following settings can be edited in UI (**Settings / Other Services - VoiceRSS Text-to-Speech**):

* **VoiceRSS API Key** - The API Key to get access to https://www.voicerss.org.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `voicerss.cfg`

Its contents should look similar to:

```
org.openhab.voice.voicerss:apiKey=1234567890
```

## Voices

It supports the following voices (depending on language):

* Oda (Arabic - Egypt)
* Salim (Arabic - Saudi Arabia)
* Dimo (Bulgarian)
* Rut (Catalan)
* Luli (Chinese - China)
* Shu (Chinese - China)
* Chow (Chinese - China)
* Wang (Chinese - China)
* Jia (Chinese - Hong Kong)
* Xia (Chinese - Hong Kong)
* Chen (Chinese - Hong Kong)
* Akemi (Chinese - Taiwan)
* Lin (Chinese - Taiwan)
* Lee (Chinese - Taiwan)
* Nikola (Croatian)
* Josef (Czech)
* Freja (Danish)
* Daan (Dutch - Belgium)
* Lotte (Dutch - Netherlands)
* Bram (Dutch - Netherlands)
* Zoe (English - Australia)
* Isla (English - Australia)
* Evie (English - Australia)
* Jack (English - Australia)
* Rose (English - Canada)
* Clara (English - Canada)
* Emma (English - Canada)
* Mason (English - Canada)
* Alice (English - Great Britain)
* Nancy (English - Great Britain)
* Lily (English - Great Britain)
* Harry (English - Great Britain)
* Eka (English - India)
* Jai (English - India)
* Ajit (English - India)
* Oran (English - Ireland)
* Linda (English - United States)
* Amy (English - United States)
* Mary (English - United States)
* John (English - United States)
* Mike (English - United States)
* Aada (Finnish)
* Emile (French - Canada)
* Olivia (French - Canada)
* Logan (French - Canada)
* Felix (French - Canada)
* Bette (French - France)
* Iva (French - France)
* Zola (French - France)
* Axel (French - France)
* Theo (French - Switzerland)
* Lukas (German - Austria)
* Hanna (German - Germany)
* Lina (German - Germany)
* Jonas (German - Germany)
* Tim (German - Switzerland)
* Neo (Greek)
* Rami (Hebrew)
* Puja (Hindi)
* Kabir (Hindi)
* Mate (Hungarian)
* Intan (Indonesian)
* Bria (Italian)
* Mia (Italian)
* Pietro (Italian)
* Hina (Japanese)
* Airi (Japanese)
* Fumi (Japanese)
* Akira (Japanese)
* Nari (Korean)
* Aqil (Malay)
* Marte (Norwegian)
* Erik (Norwegian)
* Julia (Polish)
* Jan (Polish)
* Marcia (Portuguese - Brazil)
* Ligia (Portuguese - Brazil)
* Yara (Portuguese - Brazil)
* Dinis (Portuguese - Brazil)
* Leonor (Portuguese - Portugal)
* Doru (Romanian)
* Olga (Russian)
* Marina (Russian)
* Peter (Russian)
* Beda (Slovak)
* Vid (Slovenian)
* Juana (Spanish - Mexico)
* Silvia (Spanish - Mexico)
* Teresa (Spanish - Mexico)
* Jose (Spanish - Mexico)
* Camila (Spanish - Spain)
* Sofia (Spanish - Spain)
* Luna (Spanish - Spain)
* Diego (Spanish - Spain)
* Molly (Swedish)
* Hugo (Swedish)
* Sai (Tamil)
* Ukrit (Thai)
* Omer (Turkish)
* Chi (Vietnamese)

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **VoiceRSS** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=voicerss
org.openhab.voice:defaultVoice=voicerss:frFR_Zola
```

## Supported Audio Formats

It supports the following audio formats: MP3, OGG, AAC and WAV.

## Caching

The VoiceRSS TTS service uses the openHAB TTS cache to cache audio files produced from the most recent queries in order to reduce traffic, improve performance and reduce number of requests.

An additional and specific cache can be prepared in advance to provide offline capability for predefined queries.
For convenience, there is a tool where this cache can be generated in advance, to have a prefilled cache when starting this service.
You have to copy the generated data to your userdata/voicerss/cache folder.

Synopsis of this tool:

```
Usage: java org.openhab.voice.voicerss.tool.CreateTTSCache <args>
Arguments: --api-key <key> <cache-dir> <locale> <voice> { <text> | @inputfile } [ <codec> <format> ]
  key       the VoiceRSS API Key, e.g. "123456789"
  cache-dir is directory where the files will be stored, e.g. "voicerss-cache"
  locale    the language locale, has to be valid, e.g. "en-us", "de-de"
  voice     the voice, "default" for the default voice
  text      the text to create audio file for, e.g. "Hello World"
  inputfile a name of a file, where all lines will be translatet to text, e.g. "@message.txt"
  codec     the audio codec, "MP3", "WAV", "OGG" or "AAC", "MP3" by default
  format    the audio format, "44khz_16bit_mono" by default

Sample: java org.openhab.voice.voicerss.tool.CreateTTSCache --api-key 1234567890 cache en-US default @messages.txt
```

You will need to specify the classpath for your addon (jar) in the command line (java -cp <path> ...).

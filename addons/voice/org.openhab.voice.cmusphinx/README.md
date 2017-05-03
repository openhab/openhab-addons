# CMU Sphinx Speech-to-Text

[CMU Sphinx](http://cmusphinx.sourceforge.net) is a pure Java speech-to-text engine.

It can be used to perform offline voice recognition without an Internet connection. It is rather resource-intensive and therefore not particularly well-suited for embedded hardware.

 ## Prerequisites
 
 CMU Sphinx requires these additional components to work:
 
 1. An acoustic model;
 2. A dictionary;
 3. a grammar or a language model.
 
Download acoustic models, dictionaries and language models for a number of languages from [SourceForge](https://sourceforge.net/projects/cmusphinx/files/Acoustic%20and%20Language%20Models/).
For US English the versions distributed with CMU Sphinx (preferred) are also available on [GitHub](https://github.com/cmusphinx/sphinx4/tree/master/sphinx4-data/src/main/resources/edu/cmu/sphinx/models/en-us).
 
The language model option allows in theory to convert any sentence into text, but it is strongly discouraged since performance and accuracy will be poor.

Therefore, use the grammar option and create a grammar file in JSGF format similar to this:

```
#JSGF V1.0;

grammar commands;

<location> = living room | kitchen | bedroom | corridor | bathroom | garage;
<thing> = lights | heating | fan | blinds;
<item> = <location> <thing>; 

<onoff> = on | off;
<turn> = turn | switch;
<put> = put | bring;
<increase> = increase | brighten | harden | enhance;
<decrease> = decrease | dim | lower | soften;
<color> = white | pink | yellow | orange | purple | red | green | blue;

<switchcmd> = <turn> [the] <item> <onoff>;
<increasecmd> = <increase> the <item>;
<decreasecmd> = <decrease> the <item>;
<upcmd> = <put> the <item> up;
<downcmd> = <put> the <item> down;
<colorcmd> = [set] [the] color [of] the <item> [to] <color>; 


<keyword> = wake up;

public <command> = <keyword> | <switchcmd> | <increasecmd> | <decreasecmd> | <upcmd> | <downcmd> | <colorcmd>;
```
**IMPORTANT:** The grammar file name must have a .gram extension and match the declared name (i.e. _commands.gram_ in this example).

Using the above grammar, the engine will be able to recognize sentences like _dim the kitchen lights_, _set the color of the bedroom lights to blue_, _switch the bathroom heating on_, etc. Refer to [this page on the CMU Sphinx wiki](http://cmusphinx.sourceforge.net/wiki/sphinx4:jsgfsupport) for more examples.

Note that the final command also includes the keyword you wish to use.

## Configuration

Once everything is ready, configure the extension (using a `cmusphinx.cfg` file in the services folder or with Paper UI):

- **dictionaryPath**: Locale e.g. _en-US_, _fr-FR_, _de-DE_, must match the globally configured locale;
- **acousticModelPath**: Location of the folder containing the acoustic model (containing files like `feat.params`, `mdef`, `means`, `mixture_weights` etc.);
- **dictionaryPath**: Path to the dictionary file;
- **languageModelPath**: Path to the language model file (leave blank if you use a grammar);
- **grammarPath**: Path to the _directory_ containing the grammar file;
- **grammarName**: Name of the grammar file to use without the .gram extension
- **startDialog**: If this option is switched on, voice commands will be immediately captured, recognized and interpreted when this extension is activated. Before you turn this option on, ensure you have configured defaults properly, for example in Paper UI. You need to have a default audio source, keyword spotter and speech-to-text services (likely this extension), a human language interpreter, a default text-to-speech service and audio sink (for spoken feedback) and finally a keyword to spot.



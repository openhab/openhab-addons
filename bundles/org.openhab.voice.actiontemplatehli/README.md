# Action Template Interpreter

A human language interpreter implementation powered by OpenNLP.
This is an attempt to provide you with a template system to match text commands to specific items and read its state or send command to them.
For doing this the interpreter takes advantage of some nlp techniques.

The (Apache OpenNLP)[https://opennlp.apache.org] library is a machine learning based toolkit for the processing of natural language text.
This human language interpreter aims to have no language dependency as it does nothing out of the box, please report any incompatibility with your language.

You can find models provided by OpenNLP for some languages [here](https://opennlp.apache.org/models.html) and [here](http://opennlp.sourceforge.net/models-1.5/).
Those are not required, as you can use the build-in white space or simple tokenizers (from OpenNLP),they are just required to use the match by lemmas and the optional language tag functionalities.

There are some examples at the end that you can review if you want a general idea of what can be done.

## NLP Terminology

I will briefly explain some terms that will be used:

* Tokenize: first step of the recognition is to split the text, each of these parts is called token. 
* Named Entity Recognition (NER): is the process of finding a subset of tokens on the input.
* Part Of Speech (POS) tagging: categorizing tokens in a text, depending on the definition of the token and its context.
* Lemmatize: is the process of getting a generic representation of the tokens, each of it is called lemma. (Example of one token to lemma conversion: 'is' -> 'be').

## Action Template Target:

This interpreter allows two ways of target and item:

* You can link an action to a specific item by adding it the custom metadata namespace 'actiontemplatehli'.
* You can link an action to all items of a type by providing the file '<OPENHAB_USERDATA>/actiontemplatehli/type_actions/<ITEM_TYPE_NAME>.json' (here you can restrict each action by item tags).

Two important notes:

* Actions linked to items have prevalence over actions linked to a whole item type.
* On actions linked to an item type the itemLabel placeholder will always be applied (explained bellow), if there are multiple item label detected in the text this part will be aborted.

## Action Templates Configuration:

Actions can read from an item or send a command to it.
This is defined by the boolean field read which is 'false' by default.

When read is false:

* template: action template. (Required)
* value: value to by send. It can be used to capture the transformed placeholder values. (Required unless if the target item type is String in that case silence mode is assumed true and the whole text is passed to item).
* type: action template type.
* requiredTags: allow to restrict the item targets by its tags.
* placeholders: defined placeholders that can be used on the template and replaced on the value.
* silence: boolean used to avoid confirmation message.
* targetMembers: when targeting a Group item, can be used to send the command to its member items instead.

When read is true:

* template: action template. (Required)
* value: read template, can use the placeholders symbols $itemLabel and $state.
* emptyValue: An alternative template. Is used when the state value is empty or NULL after the post transformation. The $itemLabel is available.
* type: action template type.
* requiredTags: allow to restrict the item targets by its tags.
* placeholders: only the placeholder with label state will be used, to process its POS transformation on the state.
* targetMembers: when targeting a Group item, can be used access the state of one of its members, if multiples matches a warning is shown and the first one is used.

## Placeholders

This configuration allow you to define a symbols to use on your templates.
You can define the sets of tokens to match using ner, and a transformation using pos.
Those are its fields:

 * label: label for the placeholder, is prefixed by '$' and spaces are replaced by '_' to create the symbol you can use on the template (Required).
 * nerValues: list of string containing parts of the text to look for. Takes precedence over the ner field.
 * ner: name for a file under the ner folder (<OPENHAB_USERDATA>/actiontemplatehli/ner), first it will look for a <ner>.bin model and then for a <ner>.xml dictionary for applying ner (prevalence over 'nerValues').
 * posValues: apply a pos transformation with static values. Takes precedence over the pos field.
 * pos: name for a file under the pos folder (<OPENHAB_USERDATA>/actiontemplatehli/pos), first it will look for a <pos>.bin model and then for a <pos>.xml dictionary (prevalence over 'posValues').

The placeholder symbol replaces the text tokens matched using NER (before scoring the actions) and the captured value could be transformed using POS and will be accessible to the value under its symbol.
As a summary, using the placeholders you can configure how parts of the speech are converted into valid item values and backward.

There are some special placeholders, described in another section.

### POS Transformation

POS is a technique which produces tags for each token, here though we are going to use it to match a group of words with a value so we should transform those words to a single token.
That's the reason why the whitespace character should be replaced by '__' in the POS dictionaries and static values, you can see some examples bellow.

### Target members:

When the target of an action is group item you can target its members instead.

You can use the following fields:

* itemName: name of the item member to target. If present the other fields are ignored.
* itemType: type of the item members to target.
* requiredTags: allow to restrict the member targets by its tags when matching by type.
* recursive: when matching by itemType, look for group members in a recursive way, default true.
* mergeState: on a read action when matching by itemType, merge the item states, only allowed for 'Switch' and 'Contact' item types, default false.

## Special Placeholders

### The 'itemLabel' Placeholder

The itemLabel placeholder is always applied when scoring actions linked to item types. It's replaced using NER (no case-sensitive) with your item labels. Its value is only available for read actions.

### The 'groupLabel' Placeholder

The groupLabel placeholder is only available for read actions when targeting a group member.
When it's present, the 'itemLabel' placeholder will take the value of the target member label and the 'groupLabel' the label of the group.

### The 'state' Placeholder

It's used to access the value on the read actions, you can configure a POS transformation for it.

### The '*' Placeholder (the dynamic placeholder)

The dynamic placeholder is designed to capture free text. The captured value is exposed to the value under the symbol '$*' as other placeholders.
It has some restrictions:

* Can not be the only token on the template.
* Can not be used as an optional token.
* Can not be used multiple type on the same template alternative.

Note that the dynamic placeholder does not score.

## Text Preprocessing

The interpreter needs to match the input text with a target item and action config, to know what to do.
To do so, it need the tokens and optionally the POS tags and the lemmas.

### Tokenizer

You can provide a custom model at '<OPENHAB_USERDATA>/actiontemplatehli/token.bin', otherwise it will use the build in simple tokenizer or whitespace tokenizer (configurable).

Tokenizing the text is enough to use the action type 'tokens' as tokens are the only ones required for scoring (but the option 'optionalLanguageTags' will not take effect unless you have the POS language tags).

### POSTagger (language tags)

You need to provide a model for POS tagging at '<OPENHAB_USERDATA>/actiontemplatehli/pos.bin' for your language. 
This will produce a language tag for each token, that can be used in 'optionalLanguageTags' to make some optional for scoring.
Note that you need the correct language tags for the lemmatizer to work.

### Lemmatizer

You need to provide a model for the lemmatizer at '<OPENHAB_USERDATA>/actiontemplatehli/lemma.bin' for your language. 
This will produce a lemma for each token, now you can use the action type 'lemmas'

## Action Scoring

The scoring mechanism scores each action config to look for the best scored.
If some token fails the comparison the action scores 0.
If all action scored 0 none is executed.

By the value of the field 'type' it defines if the template should by compared with the tokens or the lemmas.
Please note that the captured placeholder value is extracted from the tokens not from the lemmas, but the equivalent lemmas are replaced before scoring.

The action template string is a list of tokens separated by white space.
You can use the ';' separator to provide alternative templates and the '|' to provide alternative tokens.

Take in account that, as this is a token basis comparison, matching changes depending on the tokenizer you are using as they can produce different tokens for the same text.

### Interpreter Configuration

| Config   |  Group  |  Type  |   Default  | Description  |
|----------|----------|----------|----------|------------------------------|
| lowerText | nlp | boolean | false | Convert the input text to lowercase before processing |
| caseSensitive | nlp | boolean | false | Enable case sensitivity, do not apply to dictionaries and models, do not apply to the 'itemLabel' placeholder |
| useSimpleTokenizer | nlp | boolean | false | Prefer simple tokenizer over white space tokenizer |
| optionalLanguageTags | nlp | text |  | Comma separated POS language tags that will be optional when comparing |
| fallbackHLI | nlp | text |  | Human language interpreter to use if command is unhandled |
| commandSentMessage | messages | text | Done | Message for successful command |
| unhandledMessage | messages | text | I can not do that | Message for unsuccessful action |
| failMessage | messages | text | There was an error | Message for error during processing |

## Examples:

### String type action configs example:

This example contain the files to add actions for open an android application and check what is open.
These actions will target all String items with the tag 'launch_android_app'.

These are the files needed:

#### File '<OPENHAB_USERDATA>/actiontemplatehli/type_actions/String.json'

```json
[
    {
        "template": "launch|open $app on $itemLabel",
        "value": "$app",
	      "type": "tokens",
        "requiredTags": ["launch_android_app"],
        "placeholders": [
            {
                "label": "app",
                "ner": "applications",
                "pos": "application_to_package"
            }
        ]
    },
    {
        "template": "what app|application is open on $itemLabel;what app|application is on $itemLabel",
        "read": true,
        "value": "the open app is $state",
        "emptyValue": "no app open on $itemLabel",
	      "type": "tokens",
        "requiredTags": ["launch_android_app"],
        "placeholders": [
            {
                "label": "state",
                "pos": "package_to_application"
            }
        ]
    }
]
```

#### File '<OPENHAB_USERDATA>/actiontemplatehli/ner/applications.xml'

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dictionary case_sensitive="false">
  <entry>
      <token>youtube</token>
  </entry>
  <entry>
      <token>jellyfin</token>
  </entry>
  <entry>
      <token>amazon</token>
      <token>video</token>
  </entry>
  <entry>
      <token>netflix</token>
  </entry>
</dictionary>
```

#### File '<OPENHAB_USERDATA>/actiontemplatehli/pos/package_to_application.xml'

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dictionary>
  <entry tags="youtube">
    <token>com.google.android.youtube</token>
  </entry>
  <entry tags="netflix">
    <token>com.netflix.ninja</token>
  </entry>
  <entry tags="jellyfin">
    <token>org.jellyfin.androidtv</token>
  </entry>
  <entry tags="amazon__video"> // note the __
    <token>com.amazon.amazonvideo.livingroom</token>
  </entry>
</dictionary>
```

#### File '<OPENHAB_USERDATA>/actiontemplatehli/pos/application_to_package.xml'

```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <dictionary>
    <entry tags="com.google.android.youtube">
      <token>youtube</token>
    </entry>
    <entry tags="com.netflix.ninja">
      <token>netflix</token>
    </entry>
    <entry tags="org.jellyfin.androidtv">
      <token>jellyfin</token>
    </entry>
    <entry tags="com.amazon.amazonvideo.livingroom">
      <token>amazon__video</token> // note the __
    </entry>
  </dictionary>
```

### Switch type action configs example:

This example contain the files to add actions for turn a switch on or off.
These actions will target all Switch items.

#### File '<OPENHAB_USERDATA>/actiontemplatehli/type_actions/Switch.json'

```json
[
  {
    "template": "$onOff $itemLabel",
    "value": "$onOff",
    "type": "tokens",
    "placeholders": [
      {
        "label": "onOff",
        "nerValues": [
          "turn on",
          "turn off"
        ],
        "posValues": {
          "turn__on": "ON", // note the __
          "turn__off": "OFF"
        }
      }
    ]
  },
  {
    "template": "how be the $itemLabel",
    "read": true,
    "type": "lemmas",
    "value": "$itemLabel is $state"
  }
]
```

### Dynamic placeholder example:

This example contain the item metadata to add an action that uses the dynamic placeholder.
Add a custom metadata 'actiontemplatehli' to a String item with the following:

```yaml
value: ""
config:
  placeholders:
    - label: contact
      nerValues:
        - Andrea
        - Jacob
        - Raquel
  silence: true
  template: send message $* to $contact
  type: tokens
  value: $contact:$*
```

# Note From The Original Author

This human language interpreter takes advantage of just a subset of the capabilities offered by OpenNLP.

Don't hesitate on creating an issue proposing how to take advance of capabilities or opening a PR with any improvements.

And feel free to ping me if you need something, I'm not an expert on this field but I will try to help, @GiviMAD.

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

This interpreter allows two ways to target items:

* You can link an action to a specific item by adding the custom metadata namespace 'actiontemplatehli' to it.
* You can link an action to all items of a type by providing the file '<OPENHAB_USERDATA>/actiontemplatehli/type_actions/<ITEM_TYPE_NAME>.json' (here you can restrict each action by item tags).

Two important notes:

* Actions linked to items have prevalence over actions linked to a whole item type.
* On actions linked to an item type the itemLabel placeholder will always be applied (explained bellow). If there are multiple item labels detected on the input the actions linked to both will be scored, each using the correct itemLabel value, so there should be no collisions in case a template contains a token that matches an item label.

## Action Template Scoring

The scoring mechanism scores each action configuration to look for the best scored.
If a token fails the comparison, the action scores 0.
If all actions scored 0, none is executed.

The action configuration field 'type' defines if the template should be compared using tokens or lemmas.
Please note that the captured placeholder value is extracted from the tokens not from the lemmas, but the equivalent lemmas are replaced before scoring.

The action template string is a list of tokens separated by white space.
You can use the ';' separator to provide alternative templates and the '|' to provide alternative tokens.
Here is an example of string: "what app|application is open on $itemLabel;what app|application is on $itemLabel".

Take in account that, as this is a token basis comparison, matching depends on the tokenizer you are using as they can produce different tokens for the same text.

## Action Template Options:

The location where action configurations are placed changes whether you are targeting an item or many, so take a look to the 'Action Template Target' to understand where to put those configurations.
Also, you can check the paths indicated on the examples at the end.

Actions can read the state from an item or send a command to it.
This is defined by the boolean field read which is 'false' by default.

When read is false:

* template: action template. (Required)
* value: value to be sent. It can be used to capture the transformed placeholder values. (Required unless the target item type is String, in which case silent mode is assumed to be true and the whole text is passed to the item).
* type: action template type, either "tokens" or "lemmas".
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: defined placeholders that can be used on the template and replaced on the value.
* silent: boolean used to avoid confirmation message.
* targetMembers: when targeting a Group item, can be used to send the command to its member items instead.

When read is true:

* template: action template. (Required)
* value: read template, can use the placeholders symbols $itemLabel and $state.
* emptyValue: An alternative template. Is used when the state value is empty or NULL after the post transformation. The $itemLabel is available.
* type: action template type, either "tokens" or "lemmas".
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: only the placeholder with label state will be used, to process its POS transformation on the state.
* targetMembers: when targeting a Group item, can be used to access the state of one of its members. In case of multiple matches, a warning is shown and the first one is used.

## Placeholders

This configuration allow you to define symbols to use on your templates.
You can define the sets of tokens to match using ner, and a transformation using pos.
Those are its fields:

 * label: label for the placeholder, is prefixed with '$' and spaces are replaced by '_' to create the symbol you can use on the template (Required).
 * nerValues: list of strings containing parts of the text to look for. Takes precedence over the ner field.
 * ner: name for a file under the ner folder (<OPENHAB_USERDATA>/actiontemplatehli/ner), first it will look for a <ner>.bin model and then for a <ner>.xml dictionary for applying ner (prevalence over 'nerValues').
 * posValues: apply a pos transformation with static values. Takes precedence over the pos field.
 * pos: name for a file under the pos folder (<OPENHAB_USERDATA>/actiontemplatehli/pos), first it will look for a <pos>.bin model and then for a <pos>.xml dictionary (prevalence over 'posValues').

The placeholder symbol replaces the text tokens matched using NER (before scoring the actions) and the captured value could be transformed using POS and will be accessible to the value under its symbol.
As a summary, using the placeholders you can configure how parts of the speech are converted into valid item values and backward.
The examples at the end of the document can help you to see it clearer.

There are some special placeholders:

### The 'itemLabel' Placeholder

The itemLabel placeholder is always applied when scoring actions linked to item types. It's replaced using NER (no case-sensitive) with your item labels and synonyms (collisions will be reported in debug logs). Its value is only available for read actions.

### The 'groupLabel' Placeholder

The groupLabel placeholder is only available for read actions when targeting a group member.
When it's present, the 'itemLabel' placeholder will take the value of the target member label and the 'groupLabel' the label of the group.

### The 'state' Placeholder

It's used to access the value on the read actions, you can configure a POS transformation for it.

### The 'itemOption' Placeholder

This placeholder is available for both write and read actions and doesn't need to be configured.

When read is false, the 'itemOption' placeholder will be computed from the item command description options, or from the state description options if the command description options are not present.

When read is true, the 'itemOption' placeholder will be computed from the item state description options.

Note that, when targeting multiple group members, 'ner' (value matching) is done by merging all available member options but 'pos' (value transformation) is done using just the target member item options.

### The '*' Placeholder (the dynamic placeholder)

The dynamic placeholder is designed to capture free text. The captured value is exposed to the value under the symbol '$*' as other placeholders.
It has some restrictions:

* Can not be the only token on the template.
* Can not be used as an optional token.
* Can not be used multiple times on the same template alternative.

Note that the dynamic placeholder does not score. This way you can use it to fallback other sentences.

This example can help you to understand how this works:

You have an action with the template "play $* on living room" and another action with the template "play $musicAuthor on living room" and assuming 'mozart' is a valid value for the placeholder '$musicAuthor'.

The sentence "play mozart on living room" will score 4 when compared with the template containing the dynamic placeholder and 5 when compared with the one without it.
The action with template "play $musicAuthor on living room" will be executed.

The sentence "play beethoven on living room" will score 4 when compared with the template containing the dynamic placeholder and 0 when compared with the one without it.
The action with template "play $* on living room" will be executed.

### POS Transformation

POS is a technique which produces tags for each token, here though we are going to use it to match a group of words with a value so we should transform those words to a single token.
That's the reason why the whitespace character should be replaced by '__' in the POS dictionaries and static values, you can see some examples bellow.

### Target members:

When the target of an action is a group item, you can target its members instead.

You can use the following fields:

* itemName: name of the item member to target. If present the other fields are ignored.
* itemType: type of the item members to target.
* requiredTags: allow to restrict the members targeted by tags when matching by type.
* recursive: when matching by itemType, look for group members in a recursive way, default true.
* mergeState: on a read action when matching by itemType, merge the item states by performing an AND operation, only allowed for 'Switch' and 'Contact' item types, default false.

## Text Preprocessing

The interpreter needs to match the input text with a target item and action configuration, to know what to do.
To do so, it needs the tokens and optionally the POS tags and the lemmas.

### Tokenizer

You can provide a custom model at '<OPENHAB_USERDATA>/actiontemplatehli/token.bin', otherwise it will use the built-in simple tokenizer or whitespace tokenizer (configurable).

Here you have an example of the built-in ones:

* Using the white space tokenizer "What time is it?" produces the tokens "what" "time" "is" "it?"
* Using the simple tokenizer "What time is it?" produces the tokens "what" "time" "is" "it" "?"

Tokenizing the text is enough to use the action type 'tokens' as tokens are the only ones required for scoring (but the option 'optionalLanguageTags' will not take effect unless you have the POS language tags).

### POSTagger (language tags)

You need to provide a model for POS tagging at '<OPENHAB_USERDATA>/actiontemplatehli/pos.bin' for your language. 
This will produce a language tag for each token, that can be used in 'optionalLanguageTags' to make some optional for scoring. 
Please note that these labels may be different depending on the model, please refer to your model's documentation.
As an example:

The tokens "that,sounds,good" produces the tags "DT,VBZ,JJ".

Assuming optionalLanguageTags is empty, if we have an action with template "sounds good" it will get a 0 score when compared to the text "that sounds good" because the token "that" is not in the template.

But if we set optionalLanguageTags to "DT", the action template "sounds good" will score 2 against the text "that sounds good" as the tokens with the tag "DT" are considered optional when scoring.

Note that if we have another action with the template "that sounds good" it will score 3 and take prevalence.

You need the correct language tags for the lemmatizer to work.

### Lemmatizer

You need to provide a model for the lemmatizer at '<OPENHAB_USERDATA>/actiontemplatehli/lemma.bin' for your language. 
This will produce a lemma for each token, then you can use the action type 'lemmas'.

Note that you need the POS language tags for your language, the ones covered on the previous section, for the lemmatizer to work.

## Interpreter Configuration

| Config                  |  Group   |  Type   |   Default            | Description                                                                                                   |
|-------------------------|----------|---------|----------------------|---------------------------------------------------------------------------------------------------------------|
| lowerText               | nlp      | boolean | false                | Convert the input text to lowercase before processing                                                         |
| caseSensitive           | nlp      | boolean | false                | Enable case sensitivity, do not apply to dictionaries and models, do not apply to the 'itemLabel' placeholder |
| useSimpleTokenizer      | nlp      | boolean | false                | Prefer simple tokenizer over white space tokenizer                                                            |
| detokenizeOptimization  | nlp      | boolean | true                 | Enables build-in detokenization based on original text, otherwise string join by space is used                |
| optionalLanguageTags    | nlp      | text    |                      | Comma separated POS language tags that will be optional when comparing                                        |
| commandSentMessage      | messages | text    | Done                 | Message for successful command                                                                                |
| unhandledMessage        | messages | text    | I can not do that    | Message for unsuccessful action                                                                               |
| failureMessage          | messages | text    | There was an error   | Message for error during processing                                                                           |

## Examples:

### String type action configs example:

This example contains the files to add actions for opening an android application and checking what application is opened.
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

This example contains the files to add actions for turning a switch on or off.
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

### Switch item action configs example:

This example contains the item metadata to add an action to an item with type 'Switch''.
Add a custom metadata 'actiontemplatehli' to the 'Switch' item with the following:

```yaml
value: ""
config:
  placeholders:
    - label: onOff
      nerValues:
        - turn on
        - turn off
      posValues:
        turn__on: ON
        turn__off: OFF
  template: $onOff $itemLabel
  type: tokens
  value: $onOff
```

### Dynamic placeholder, sending a message example:

This example contains the item metadata to add an action that uses the dynamic placeholder.
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
  silent: true
  template: send message $* to $contact
  type: tokens
  value: $contact:$*
```

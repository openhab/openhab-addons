# Action Template Interpreter

This interpreter does nothing on his own.
It provides you with a template system to match text commands to items and read its states or send commands to them.

When reading from an item you can transform its state to a human-readable text and template it into your defined response.

When writing to an item you can extract parts of the text and transform those in a valid item command.

This human language interpreter aims to have no language dependency as it does nothing out of the box, please report any incompatibility with your language.

Be encourage to read the examples at the end to get a general idea of what can be done.


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

The action configuration field 'type' defines the scoring mechanism. Those are the available:

* "tokens": score by token equality. Will score 100% or 0% depending on whether all tokens match the template.
* "dice": score using dice's coefficient. It calculates a percentage based on the template and token text similarity, score is ignored if it's under the configured 'diceComparisonThreshold' value.

The action template string is a list of tokens separated by white space.
You can use the ';' separator to provide alternative templates, the '|' to provide alternative tokens and the '?' suffix to make a token optional.
Here is an example of string: "what app|application is open? on $itemLabel;what is the app|application on $itemLabel".

Take in account that, as this is a token basis comparison, matching depends on the tokenizer you are using as they can produce different tokens for the same text.

## Action Template Options:

The location where action configurations are placed changes whether you are targeting an item or many, so take a look to the 'Action Template Target' to understand where to put those configurations.
Also, you can check the paths indicated on the examples at the end.

Actions can read the state from an item or send a command to it.
This is defined by the boolean field read which is 'false' by default.

When read is false:

* template: action template. (Required)
* value: value to be sent. It can be used to capture the transformed placeholder values. (Required unless the target item type is String, in which case silent mode is assumed to be true and the whole text is passed to the item).
* type: action template type, either "tokens" or "dice".
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: defined placeholders that can be used on the template and replaced on the value.
* silent: boolean used to avoid confirmation message.
* targetMembers: when targeting a Group item, can be used to send the command to its member items instead.

When read is true:

* template: action template. (Required)
* value: read template, can use the placeholders symbols $itemLabel and $state.
* emptyValue: An alternative template. Is used when the state value is empty or NULL after the transforming it. The $itemLabel is available.
* type: action template type, either "tokens" or "dice".
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: only the placeholder with label state will be used, will apply its configured mappedValues on the state backwards.
* targetMembers: when targeting a Group item, can be used to access the state of one of its members. In case of multiple matches, a warning is shown and the first one is used.

## Placeholders

This configuration allow you to define symbols to use on your templates.
You can define the sets of tokens to be matched and its required transformations.
Those are its fields:

 * label: label for the placeholder, is prefixed with '$' and spaces are replaced by '_' to create the symbol you can use on the template (Required).
 * values: list of strings containing parts of the text to look for and capture this text as placeholder value.
 * valuesFile: name (without extension) for a json file under the values folder (<OPENHAB_USERDATA>/actiontemplatehli/values) containing and array of strings to use as values.
 * mappedValues: object with keys and values of type strings, keys are used to match parts of the text and capture its value as placeholder value.
 * mappedValuesFile: name (without extension) for a json file under the mapped_values folder (<OPENHAB_USERDATA>/actiontemplatehli/mapped_values) containing and object with keys and values of type string to use as mapped values.

So the total list of terms to look for as valid placeholder value are the combination of the values, valuesFiles, mappedValues keys and mappedValuesFileKeys fields, and at least one is required.

Note that mappedValues are applied backwards to read actions so the mappedValues object should always contain the human readable text as keys and the state that represented it as value. This way you can reuse mappedValues files between read and write actions. 

As a summary, using the placeholders you can configure how parts of the speech are converted into valid item values and backward.
The examples at the end of the document can help you to see it clearer.

There are some reserved placeholders, explained bellow.

### The 'itemLabel' Placeholder

The itemLabel placeholder is always applied when scoring actions linked to item types. It'll replace your item labels and synonyms (collisions will be reported in debug logs). Its value is only available for read actions.

### The 'groupLabel' Placeholder

The groupLabel placeholder is only available for read actions when targeting a group member.
When it's present, the 'itemLabel' placeholder will take the value of the target member label and the 'groupLabel' the label of the group.

### The 'state' Placeholder

It's used to access the value on the read actions, you can configure mappedValues for it (remember those are applied backwards).

### The 'itemOption' Placeholder

This placeholder is available for both write and read actions and doesn't need to be configured.

When read is false, the 'itemOption' placeholder will be computed from the item command description options, or from the state description options if the command description options are not present.

When read is true, the 'itemOption' placeholder will be computed from the item state description options.

Note that, when targeting multiple group members, text search is done by merging all available member value options, but the value transformation is done using just the target member item options.

### The '*' Placeholder (the dynamic placeholder)

The dynamic placeholder is designed to capture free text. The captured value is exposed to the value under the symbol '$*' as other placeholders.
It has some restrictions:

* Can not be the only token on the template.
* Can not be used as an optional token.
* Can not be used multiple times on the same template alternative.

Note that the dynamic placeholder does not score. This way you can use it to fallback other sentences.

This example can help you to understand how this works:

You have an action with the template "play $* on living room" and another action with the template "play $musicAuthor on living room" and assuming 'mozart' is a valid value for the placeholder '$musicAuthor'.

The sentence "play mozart on living room" will score 80% when compared with the template containing the dynamic placeholder and 100% when compared with the one without it.
The action with template "play $musicAuthor on living room" will be executed.

The sentence "play beethoven on living room" will score 80% when compared with the template containing the dynamic placeholder and 0 when compared with the one without it, as 'beethoven' is not a valid value for the '$musicAuthor' placeholder.
The action with template "play $* on living room" will be executed.

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

Tokenize: First step of the recognition is to split the text, each of these parts is called token. 

You can provide a custom model at '<OPENHAB_USERDATA>/actiontemplatehli/token.bin', otherwise it will use the built-in simple tokenizer or whitespace tokenizer (configurable).

Here you have an example of the built-in ones:

* Using the white space tokenizer "What time is it?" produces the tokens "what" "time" "is" "it?"
* Using the simple tokenizer "What time is it?" produces the tokens "what" "time" "is" "it" "?"

Tokenizing the text is enough to use the action type 'tokens' as tokens are the only ones required for scoring (but the option 'optionalLanguageTags' will not take effect unless you have the POS language tags).

### POSTagger (language tags)

Part Of Speech (POS) tagging: categorizing tokens in a text, depending on the definition of the token and its context.

You need to provide a model for POS tagging at '<OPENHAB_USERDATA>/actiontemplatehli/pos.bin' for your language. 
Please note that these labels may be different depending on the model, please refer to your model's documentation.
As an example:

The tokens "that,sounds,good" produces the tags "DT,VBZ,JJ".

You can match against these tags by prefixing a token by <tag> in a template.
As an example of this you can use the template "<tag>DT sound good" to match the tokens "that,sounds,good". 

You need the correct language tags for the lemmatizer to work.

### Lemmatizer

Lemmatize: is the process of getting a generic representation of the tokens, each of it is called lemma. (Example of one token to lemma conversion: 'is' -> 'be').

You need to provide a model for the lemmatizer at '<OPENHAB_USERDATA>/actiontemplatehli/lemma.bin' for your language.
This will produce a lemma for each token.

You can match against these lemmas by prefixing a token by <lemma> in a template.
As an example of this you can use the template "he <lemma>be good" to match the tokens "he,is,good".

Note that you need the POS language tags for your language, the ones covered on the previous section, for the lemmatizer to work.

## NLP Models

You can find models provided by OpenNLP for some languages [here](https://opennlp.apache.org/models.html) and [here](http://opennlp.sourceforge.net/models-1.5/).
Those are just required to use the matching by lemma or tag functionality.

## Interpreter Configuration

| Config                  |  Group   |  Type   |   Default          | Description                                                                                                   |
|-------------------------|----------|---------|--------------------|---------------------------------------------------------------------------------------------------------------|
| lowerText               | nlp      | boolean | false              | Convert the input text to lowercase before processing.                                                        |
| caseSensitive           | nlp      | boolean | false              | Enable case sensitivity, do not apply to the 'itemLabel' placeholder.                                         |
| useSimpleTokenizer      | nlp      | boolean | false              | Prefer simple tokenizer over white space tokenizer.                                                           |
| detokenizeOptimization  | nlp      | boolean | true               | Enables build-in detokenization based on original text, otherwise string join by space is used.               |
| diceComparisonThreshold | nlp      | number  |                    | Minimum score for dice type actions to not be discarded (percentage).                                         |
| commandSentMessage      | messages | text    | Done               | Message for successful command.                                                                               |
| unhandledMessage        | messages | text    | I can not do that  | Message for unsuccessful action.                                                                              |
| failureMessage          | messages | text    | There was an error | Message for error during processing.                                                                          |

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
                "mappedValues": "android_apps"
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
                "mappedValues": "android_apps"
            }
        ]
    }
]
```

#### File '<OPENHAB_USERDATA>/actiontemplatehli/pos/android_apps.json'

```json
{
  "youtube": "com.google.android.youtube",
  "netflix": "com.netflix.ninja",
  "jellyfin": "org.jellyfin.androidtv",
  "amazon video": "com.amazon.amazonvideo.livingroom"
}
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
        "mappedValues": {
          "turn on": "ON",
          "turn off": "OFF"
        }
      }
    ]
  },
  {
    "template": "how <lemma>be the $itemLabel",
    "read": true,
    "type": "tokens",
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
      mappedValues:
        "turn on": ON
        "turn off": OFF
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
      values:
        - Andrea
        - Jacob
        - Raquel
  silent: true
  template: send message $* to $contact
  type: tokens
  value: $contact:$*
```

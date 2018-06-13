# ihc2 Binding

OpenHAB 2 binding for LK IHC Controller Visual2

Based on the OpenHAB 1 Binding written by Pauli Anttila

## Supported Things

Bridge thing: 
"IHC Controller" settings for of the IHC Visual2 Controller
The UID of the "IHC Controller" is hardcoded to ensure only 
one controller can be defined.


The Ihc2 binding supports these base OH2 thing types: 

OpenHab  |   LK IHC SOAP Types
---------+-----------------------------------------------------------------------------------
Switch   | WSBooleanValue
Contact  | WSBooleanValue
Dimmer   | WSIntegerValue
DateTime | WSDateValue, WSTimeValue
String   | WSEnumValue
Number   | WSFloatingPointValue, WSIntegerValue, WSBooleanValue, WSTimerValue, WSWeekdayValue


## Discovery

In the setting of the "IHC Controller" four levels of discovery can be selected:
  "Do Nothing" - discovery is disabled. A previous discovery result in the Inbox is left untouched.
  "Only include Linked Resources" - Only include resources that are linked to other resources in the IHC Project.
  "Include ALL Resources" - everything from the IHC Project is added to the Inbox.
  "Clear the Inbox" - deletes the Ihc2 discoveries from the Inbox.


## Binding Configuration

IHC Connection:
  IP        (mandantory): The IP of the LK IHC Visual2 Controller. The binding uses HTTPS only.
  User Name (mandantory): The login name.
  Password  (mandantory): The password to use. (Note this is visiable through the REST interface - keep it inhouse)
  Timeout   (mandantory): The time in ms the binding will wait for an answer from the IHC Controller.
  
Run Parameters:
  IHC Project File (mandantory): The Binding downloads the IHC Project VIS file from the Controller and stores it here.
  Resource File      (optional): An extract of resources from the IHC Project VIS file. This could be usefull if Things have to be defined manually.
  
Discovery Parameters:
  See ## Discovery

## Thing Configuration

Switch:
 Configuration Parameters:
    Switch Resource ID: The IHC Projects Resource ID either in Integer or Hex format.    
    Read Only         : Can the value in the IHC Controller be updated or not.
    Pulse Time        : Is the Switch used as a pseudo push button it will automatically sitch OFF after this time.

Contact:
  Configuration Parameters:
    DateTime Resource ID: The IHC Projects Resource ID either in Integer or Hex format.
    Pattern             : How to format the text OPEN/CLOSE. Supports mapping e.g.: "MAP(dk.map):%s
    Invert Signal       : Inverts the OPEN/CLOSE status. Usefull for Alarm NC input.   
    
Dimmer:  
  Configuration Parameters:
    Dimmer Resource ID: The IHC Projects Resource ID either in Integer or Hex format.
    
DateTime:
  Configuration Parameters:
    Contact Resource ID: The IHC Projects Resource ID either in Integer or Hex format.
    Pattern            : How to format the DateTime. E.g.:  %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS
    Read Only          : Can the value in the IHC Controller be updated or not.
    
String: 
 Configuration Parameters:
    String Resource ID: The IHC Projects Resource ID either in Integer or Hex format.
    Pattern           : How to format the String. E.g.: %s
    Read Only         : Can the value in the IHC Controller be updated or not.
    
Number:
  Configuration Parameters:
    Number Resource ID: The IHC Projects Resource ID either in Integer or Hex format.
    Minimum Value      : Min value.
    Maximum Value      : Max value.
    Step Value         : Increase/Decese value by. 
    Pattern            : How to format the Number. E.g.: %d
    Read Only          : Can the value in the IHC Controller be updated or not.


## Channels

Channel Type |  Value Name
-------------+-------------
switchState  | switchStatus
contactState | contactStatus
percent      | percentValue
dateTime     | dateTimeValue
number       | numberValue
string       | stringValue

Ex.: ihc2:dimmer:a7da1c95:percentValue

## Full Example

ihc2.sitemap:
  sitemap ihc2 label="Main Menu"
  {
    Frame {   
      Group item=gGF label="Ground Floor" icon="groundfloor"
    }
  }

ihc2.items:
  Group gGF           "Ground Floor"  <groundfloor>
  Group GF_Living     "Living Room"   <video>   (gGF)
  Dimmer Light_GF_Living_Ceiling  "Ceiling"     (GF_Living)    { channel = "ihc2:dimmer:a7da1c95:percentValue" }
  Switch Light_GF_Living_Table    "Table"       (GF_Living)    { channel = "ihc2:switch:4b3c63c7:switchStatus" }


## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_

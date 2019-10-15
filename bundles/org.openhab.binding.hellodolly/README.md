# HelloDolly Binding

The addon will place a line of Louis Armstrongs song Hello Dolly. 


## Thing Configuration

Currently a Hello Dolly thing will have 3 channels.
In your sitemap file, create a group named HelloDolly.
In your items file, create a string element, i.e. HelloDollyText.

'''
String HelloDollyTextChannel1
    "Hello Dolly Channel 1"
    <none>
    (HelloDolly)
    {channel="hellodolly:sample:70e3e7e2:channel1"}

Switch HelloDollyUpdateSwitch
    "Hello Dolly Update Switch"
    <none>
    (HelloDolly)
    {channel="hellodolly:sample:70e3e7e2:channel2"}
'''

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| channel1 | String | Text of song.                |
|----------|--------|------------------------------|
| channel2 | Switch | Switch to update random pick.|
|--------------------------------------------------|

## Full Example

Sitemap:
'''
sitemap HouseDeveloper label="House" {
    Frame label="Development Area" {
        Group item=DevelopmentArea
        Text item=JobaAndroidRoom {
            Switch item=JobaAndroidRoomFirstFloor
            Switch item=JobaAndroidRoomSecondFloor
            Switch item=JobaAndroidRoomThirdFloor
        }
        Group item=HelloDolly
    }
}
'''

Items:
'''
String HelloDollyTextChannel1
    "Hello Dolly Channel 1"
    <none>
    (HelloDolly)
    {channel="hellodolly:sample:70e3e7e2:channel1"}

Switch HelloDollyUpdateSwitch
    "Hello Dolly Update Switch"
    <none>
    (HelloDolly)
    {channel="hellodolly:sample:70e3e7e2:channel2"}
'''

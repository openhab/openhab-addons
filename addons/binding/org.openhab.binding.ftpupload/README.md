# FTP Upload Binding

This binding can be used to receive image files from FTP clients.
Binding acts as a FTP server.
Images stored on the FTP server are not saved to the file system, therefore the binding shouldn't cause any problems on flash based openHAB installations.

## Supported Things

This binding supports ```ftpupload``` Thing.
Every thing is identified by FTP user name.
Therefore, every thing should use unique user name to login FTP server.

## Discovery

Automatic discovery is not supported.

## Binding Configuration

Bindings FTP server listening 2121 TCP port by default, but port can be configured.
Also idle timeout can be configured.

## Channels

This binding currently supports following channels:

| Channel Type ID | Item Type    | Description                                                                            |
|-----------------|--------------|----------------------------------------------------------------------------------------|
| image           | Image        | Image file received via FTP.                                                           |

Binding also supports custom Image channels, where a matching filename can be configured.
When an image file is uploaded to FTP server, the binding tries to find the channel whose filename match to the uploaded image filename.
If any direct match isn't found, the default image channel is updated.
The filename parameter supports regular expression patterns.
See more details in the Things example. 


### Trigger Channels

| Channel Type ID | Options                | Description                                         |
|-----------------|------------------------|-----------------------------------------------------|
| image-received  | IMAGE_RECEIVED         | Triggered when image file received from FTP client. |

Binding also supports custom trigger channels, where a matching filename can be configured.
When an image file is uploaded to FTP server, the binding tries to find the trigger channel whose filename match to the upload image filename.
If any direct match isn't found, the default trigger channel is called.
The filename parameter supports regular expression patterns.
See more details in the Things example. 

## Full Example

Things:

```
Thing ftpupload:imagereceiver:images1 [ userName="test1", password="12345" ] {

Thing ftpupload:imagereceiver:images2 [ userName="test2", password="12345" ] {
   Channels:
        Type image-channel : my_image1 "My Image channel 1" [
            filename="test12[0-9]{2}.png" // match to filename test12xx.png, where xx can be numbers between 00-99
        ]
        Type image-channel : my_image2 "My Image channel 2" [
            filename="test.jpg"
        ]
        Trigger String : my_image_trigger1 [
            filename="test12[0-9]{2}.png"
        ]
        Trigger String : my_image_trigger2 [
            filename="test.jpg"
        ]
}    
```

Items:

```
Image  Image1 { channel="ftpupload:imagereceiver:images1:image" }
Image  Image2 { channel="ftpupload:imagereceiver:images2:my_image1" }
```

Rules:

```
rule "example trigger rule 1"
when
    Channel 'ftpupload:imagereceiver:images1:image-received' triggered IMAGE_RECEIVED 
then
    logInfo("Test","Image received")
end

rule "example trigger rule 2"
when
    Channel 'ftpupload:imagereceiver:images2:my_image_trigger1' triggered IMAGE_RECEIVED 
then
    logInfo("Test","Image received")
end

```

Sitemap:

```
Frame label="FTP images" {
    Image item=Image1
    Image item=Image2
}
```

## Use case example

Binding is used to receive images from network camera which can send images to a FTP server when motion or sound is detected.

Things:

```
Thing ftpupload:imagereceiver:garagecamera [ userName="garage", password="12345" ]
```

Items:

```
Image  Garage_NetworkCamera_Motion_Image { channel="ftpupload:imagereceiver:garagecamera:image" }
```

Rules:

```
rule "example trigger rule"
when
    Channel 'ftpupload:imagereceiver:garagecamera:image-received' triggered IMAGE_RECEIVED 
then
    logInfo("Test","Garage motion detected")
end
```

Sitemap:

```
Frame label="Garage network camera" icon="camera" {
    Image item=Garage_NetworkCamera_Motion_Image
}
```

## Logging and problem solving

For problem solving, if binding logging is not enough, Apache FTP server logging can also be enabled by the following command in the karaf console:

```
log:set DEBUG org.apache.ftpserver
```

and set back to default level:

```
log:set DEFAULT org.apache.ftpserver
```

If you meet any problems to receive images from the network cameras, you could test connection to binding with any FTP client.
You can send image files via FTP client and thing channels should be updated accordingly.

 
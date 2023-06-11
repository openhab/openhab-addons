# FTP Upload Binding

This binding can be used to receive image files from FTP clients.
The binding acts as a FTP server.
Images stored on the FTP server are not saved to the file system, therefore the binding shouldn't cause any problems on flash based openHAB installations if files are uploaded to FTP server continuously (e.g. network camera images).

## Supported Things

This binding supports Things of type `ftpupload`.
Every Thing is identified by FTP user name.
Therefore, every thing should use unique user name to login FTP server.

## Discovery

Automatic discovery is not supported.

## Binding Configuration

The binding has the following configuration options:

| Parameter    | Name          | Description                                                                                                                                                                                                                                                                                                               | Required | Default value |
|--------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| port         | TCP Port      | TCP port of the FTP server                                                                                                                                                                                                                                                                                                | no       | 2121          |
| idleTimeout  | Idle timeout  | The number of seconds before an inactive client is disconnected. If this value is set to 0, the idle time is disabled.                                                                                                                                                                                                    | no       | 60            |
| passivePorts | Passive Ports | A string of passive ports, can contain a single port (as an integer), multiple ports seperated by commas (e.g. 123,124,125) or ranges of ports, including open ended ranges (e.g. 123-125, 30000-, -1023). Combinations for single ports and ranges is also supported. Empty (default) allows all ports as passive ports. | no       |               |

## Thing Configuration

The `ftpupload` Thing has the following configuration parameters:

| Parameter                   | Description                                                                                                         | Required | Default value |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------|----------|---------------|
| userName                    | User name to login to the FTP server. User name is used to identify the Thing, so it should be unique per Thing.    | yes      |               |
| password                    | Password to login to the FTP server.                                                                                | yes      |               |

## Channels

This binding currently supports the following channels:

| Channel         | Channel Type Id | Item Type    | Description                                                                            |
|-----------------|-----------------|--------------|----------------------------------------------------------------------------------------|
| image           | image-channel   | Image        | Image file received via FTP.                                                           |

Additionally user can introduce custom image-channel's to Thing (see examples).
When an image file is uploaded to FTP server, the binding tries to find the channel whose filename matches the uploaded image filename.
If no match is found, no channel is updated.
The filename parameter supports regular expression patterns.
See more details in the Things example.

Image channel supports following options:

| Parameter   | Name         | Description                                                              | Required | Default value |
|-------------|--------------|--------------------------------------------------------------------------|----------|---------------|
| filename    | Filename     | Filename to match received files. Supports regular expression patterns.  | yes      | .*            |

### Trigger Channels

| Channel Type ID | Options                | Description                                         |
|-----------------|------------------------|-----------------------------------------------------|
| image-received  | IMAGE_RECEIVED         | Triggered when image file received from FTP client. |

When an image file is uploaded to FTP server, the binding tries to find the trigger channel whose filename matches the upload image filename.
If no match is found, no channel is updated.
The filename parameter supports regular expression patterns.
See more details in the Things example.

Trigger channels supports following options:

| Parameter   | Name         | Description                                                              | Required | Default value |
|-------------|--------------|--------------------------------------------------------------------------|----------|---------------|
| filename    | Filename     | Filename to match received files. Supports regular expression patterns.  | yes      | .*            |

## Full Example

Things:

```java
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

```java
Image  Image1 { channel="ftpupload:imagereceiver:images1:image" }
Image  Image2 { channel="ftpupload:imagereceiver:images2:my_image1" }
```

Rules:

```java
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

```perl
Frame label="FTP images" {
    Image item=Image1
    Image item=Image2
}
```

## Use case example

The binding can be used to receive images from network cameras that send images to a FTP server when motion or sound is detected.

Things:

```java
Thing ftpupload:imagereceiver:garagecamera [ userName="garage", password="12345" ]
```

Items:

```java
Image  Garage_NetworkCamera_Motion_Image { channel="ftpupload:imagereceiver:garagecamera:image" }
```

Rules:

```java
rule "example trigger rule"
when
    Channel 'ftpupload:imagereceiver:garagecamera:image-received' triggered IMAGE_RECEIVED 
then
    logInfo("Test","Garage motion detected")
end
```

Sitemap:

```perl
Frame label="Garage network camera" icon="camera" {
    Image item=Garage_NetworkCamera_Motion_Image
}
```

## Logging and Problem Solving

For problem solving, if binding logging is not enough, Apache FTP server logging can also be enabled by the following command in the Karaf console:

```shell
log:set DEBUG org.apache.ftpserver
```

and set back to default level:

```shell
log:set DEFAULT org.apache.ftpserver
```

If you meet any problems to receive images from the network cameras, you could test connection to binding with any FTP client.
You can send image files via FTP client and thing channels should be updated accordingly.

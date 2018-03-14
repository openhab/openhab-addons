# Face Recognition Binding

This binding receives images from IP cameras and provides user authentication through face recognition.

## Supported IP cameras

Every IP camera that provides an HTTP based API for retrieving JPEG images is supported by this binding. The URL for downloading images is vendor specific and should be found in the camera manual. Anyway some known URLs are:

| Model       |          Snapshot URL            |
|-------------|----------------------------------|
| Wansview K2 | http://IP/mjpeg/snap.cgi?chn=0   |
| EasyN       | http://IP:PORT/snapshot.cgi      |
| FDT 720P    | http://IP/tmpfs/auto.jpg         |

## Prerequisites

The file `haarcascade_frontalface_default.xml` from the package `opencv-data` is required to detect faces. On Debian/Ubuntu based systems just install that package:

    ~# apt-get install opencv-data

On other systems copy that file to `/usr/share/opencv/haarcascades/` directory.

## Discovery

Discovery is currently not supported.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Following parameters can be configured:

| Parameter | Type   | Description                                                |
|-----------|--------|------------------------------------------------------------|
| camera    | String | URL for receiving camera snapshots                         |
| interval  | Number | Interval in milliseconds between the captures              |
| username  | String | User name for accessing camera (if camera is protected)    |
| password  | String | Password for accessing camera (if camera is protected)     |
| threshold | Number | Maximum score value to accept a user as authenticated      |

*threshold* marks the maximum score where a user should be predicted as known. Person's appearance changes slightly from day to day. The face recognition algorithm predicts a user based on a trained face model and provides a score. The more a person's appearance changes, the higher the score. That means the actual face is lesser reliable than the trained face. The *threshold* must be set individual. Decreasing *threshold* increases security but also *False Rejection Rate*. Increasing threshold decreases security and increases *False Acceptance Rate*.

## Channels

The recognized face information is available on these channels:

| Channel                | Type     | Description                                                                        |
|------------------------|----------|------------------------------------------------------------------------------------|
| live#images            | Image    | Receives images periodically from the camera configured by interval                |
| accessGranted#image    | Image    | Receives image of person that has been granted access                              |
| accessGranted#username | String   | Receives user name of person that has been granted access                          |
| accessGranted#score    | Number   | Receives score of face recognition of the person that has been granted access      |
| accessGranted#time     | DateTime | Receives time of the access granted                                                |
| accessDenied#image     | Image    | Receives image of person that has been denied access                               |
| accessDenied#username  | String   | Receives user name of person (if known) that has been denied access                |
| accessDenied#score     | Number   | Receives score of face recognition of the person that has been denied access       |
| accessDenied#time      | DateTime | Receives time of the access denied                                                 |

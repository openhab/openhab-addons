# Camera Binding

The binding integrates simple IP base cameras.  

Almost every IP camera has a simple HTTP access API defined by Urls to access at least static images.
Additionally video streams like H264.x oder MJPEG and others are very wide spread. This binding supports these types of cameras.

-- Thomas Hartwig 2016-07-24

## Supported Cameras

All cameras supporting simple HTTP based API fetching for JPG/PNG images.
Please contact the author Thomas Hartwig <thomas.hartwig@gmail.com> for camera Url examples if not listed here.

| Vendor    |      Single snapshot URL          | Video capture (not yet supported)     |
|:------:   |:-----------------------------:    |:---------------------------------:    |
| Axis      | http://<CAMERA>/jpg/image.jpg     |                                       |
| Bosch     | http://<CAMER>/snap.jpg           |                                       |

## Discovery

Currently camera discovery is not supported, probably ONVIF might supported soon.

## Thing Configuration

Currently a camera can be added and configured in the PaperUi for instance. Following parameters are supported:

* Poll time: the update interval of the image to fetch from the camera
* Snapshot Url: the url to fetch the image from see examples above
* Username: username when the camera is protected
* Password: password when the camer is protected

## Channels

### image

This is the base image channel which is updated periodically in the configured interval. This is currently the only supported channel.


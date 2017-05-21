# Camera Binding

The binding integrates simple IP base cameras.  

Almost every IP camera has a simple HTTP access API defined by Urls to access at least static images.
Additionally video streams like H264.x oder MJPEG and others are very wide spread. This binding supports these types of cameras.

-- Thomas Hartwig 2016-07-24

## Supported Cameras

All cameras supporting simple HTTP based API fetching for JPG/PNG images.
Please contact the author Thomas Hartwig <thomas.hartwig@gmail.com> for camera Url examples if not listed here.

Vendor | Single snapshot URL | Video capture (not yet supported) |
:------:|:-------------------:|:---------------------------------:|
 Axis | http://<CAMERA>/jpg/image.jpg

## Discovery

Currently camera discovery is not supported, probably ONVIF might supported soon.

## Thing Configuration

* self explaining when devices are created in OpenHab


## Channels

### image

This is the base image channel which is updated periodically in the configured interval. This is currently the only supported channel.

## Near Future

* Motion detection
* PTZ Url support
* Timeline persistence
* ONVIF detection

## Far Future

* Video support
* Video archiving

## Ideas (welcome)

* Instead of polling for images via the backend applets or more intelligent viewers might integrated into the UI.
* Prefetch

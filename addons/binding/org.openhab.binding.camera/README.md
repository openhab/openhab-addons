# Camera Binding

The binding integrates simple IP base cameras.  

Almost every IP camera has a simple HTTP access API defined by Urls to access at least static images.
Additionally video streams like H264.x oder MJPEG and others are very wide spread. This binding supports these types of cameras.

-- Thomas Hartwig 2016-07-24

## Supported Cameras

### _More to add_

Please contact the author Thomas Hartwig<thomas.hartwig@gmail.com> for camera Url examples if not listed here.

## Discovery

Currently camera discovery is not supported, probably ONVIF might supported soon.

## Thing Configuration

* self explaining when devices are created in OpenHab


## Channels

### image

This is the base image channel which is updated periodically in the configured intervall. This is currently the only supported channel.

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
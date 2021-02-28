# ABB/Busch-free@home Smart Home binding

 openHAB ABB/Busch-free@home binding based on the offical free@home local api

![alt text](https://github.com/jannodeluxe/jannnnoooo/blob/main/free_at_home_logo_1.jpg)
![alt text](https://github.com/jannodeluxe/jannnnoooo/blob/main/abb_freeathome_2_0.png)
# Description

This openHAB binding allows you to connect your free@home Smart Home system from ABB / Busch-Jaeger to openHAB and to control and observe most of the components.
It requires a System Access Point with version 2.6.1 or higher.

# Supported Devices

- ABB / Busch-Jaeger System Access Point 2.0
- free@home Switch Actuator Sensor 1/1, 2/1, 2/2 (wired and wireless)
- free@home Dimming Actuator Sensor 1/1, 2/1 (wired and wireless)
- free@home Blind Actuator Sensor 1/1, 2/1 (wired and wireless)
- free@home Movement Detector Actuator
- free@home Radiator Thermostat
- free@home Switch Actuator 4-channel
- free@home Switch Actuator 4-channel
- free@home Dimming Actuator 4- and 6-channel
- IP-touch panel (function: door opener, door ring sensor)
- Hue devices (untested)


# Tested SysAP Versions

|Version|Supported|Notes|
|---|---|---|
|2.6.1|:clock11:|under testing|


# Setup / Installation

## Prerequisites

To make use of this Binding first the local free@home API has to be activated. The API is disabled by default!

1. Open the free@home next app
2. Browse to "Settings -> free@home settings -> local API and activate the checkbox

![alt text](https://github.com/jannodeluxe/jannnnoooo/blob/main/freeathome-settingsapi.PNG)


## Installing the binding

As this binding is not in the official release of openHAB, you will not find it in the "bindings" section.
To use this binding please do the following steps:

1. Download the latest jar file [here](https://github.com/andrasU/openhab-free-home-binding/tree/main/org.openhab.binding.freeathomesystem/target)
2. Upload the jar file to the user directory of your device running openHAB
 1. for openHABian e.g: /usr/share/openhab/addons
 2. for others: /etc/openhab/addons/
3. reboot the device with     `sudo reboot`

## Discovery

The free@home bridge shall be added manually. Once it is added as a Thing with correct credentials, the scan of free@home devices will be possible.
The devices with multiple channels are devided into multiple devices with a single channel. The label of these devices is built-up with the same deivce name and free@home device ID but the channel number is mentioned in the Thing label as well for better identification.

## Setup

1. Enter your openHAB webfrontend with     `<device IP>:8080`
![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/Login.png)

2. Log into openHAB with your crendetials at the lower left side
![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/login2.png)

3. Browse to "Settings -> Things" and press the "+" symbol

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/things1.png)

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/thing2.png)

4. Choose "FreeAtHome System Binding" and click "Free@home Bridge"

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/thing3.png)

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/bridge1.png)

5. Add the required data: SysAP IP address, username and password

**ATTENTION:** The username here has to be from "Settings -> free@home settings -> local API, NOT the username from webfrontend or used in the app for login)

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/bridge2.png)

6. Press save in the righter upper corner
7. If everthing is right the Bridge should went "Online"
8. "Scan" for the free@home devices and set them up

![Login](https://github.com/andrasU/openhab-free-home-binding/blob/main/images/scan1.png)


# Communities

[Busch-Jaeger Community](https://community.busch-jaeger.de/)

[free@home user group Facebook DE](https://www.facebook.com/groups/738242583015188)

[free@home user group Facebook EN](https://www.facebook.com/groups/452502972031360)

[openHAB Community free@home](https://community.openhab.org/t/busch-jaeger-free-home/31043/469)


# Changelog

The changelog can be viewed [here](CHANGELOG.md).


# Upgrade Notes

Upgrade Notes can be found in the [CHANGELOG](CHANGELOG.md).


# Help

If you have any questions or help please open an issue on the GitHub project page.


# Contributing

Pull requests are always welcome.


# Donation

If you find my work useful you can support the ongoing development of this project by buying me a [cup of coffee] tbd


# License

The project is subject to the MIT license unless otherwise noted. A copy can be found in the root directory of the project [LICENSE](LICENSE).


# Disclaimer

This Binding is a private contribution and not related to ABB or Busch-Jaeger. It may not work with future updates of the free@home firmware and can also cause unintended behavior. Use at your own risk!

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

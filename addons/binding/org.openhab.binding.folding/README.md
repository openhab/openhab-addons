# Folding@home Binding

Binding for the [Folding@home](https://folding.stanford.edu/) distributed computing
software.

This binding can control multiple Folding@home clients and slots, using the TCP
interface.
Clients are modeled as bridges, and support adding any number of slots
(though, usually CPU and GPU).
The binding provides control over Run / Pause and Finish.
It polls for the status of the client, updates the run / pause state, and
provides a basic description of the slot.

The clients must be added manually in the Paper UI, but the slots for that
client will then appear using auto-discovery.

## Requirements (network access to F@H)

The Folding@home TCP interface is enabled only on localhost by default, without
a password.
In order to allow control of Folding on other machines than the one
running openHAB, it is necessary to configure the Folding client to accept commands
from a non-localhost address.
Here is how to do it in the FAHControl application:

*   Open FAHControl on the client to be added
*   Click on Configure, then the Remote Access tab
*   Enter a password twice (invent one)
*   Locate the Allow box under IP Address Restrictions
*   Append a space and the IP address of the machine running openHAB to the text  
    in that box, so it reads something like `127.0.0.1 192.168.1.2`

You should now have access to the client, configure it using the password and
IP address in the manual thing configuration interface.

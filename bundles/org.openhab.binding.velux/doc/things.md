```java
//
// Definition of Velux bridge velux:klf200:home
//

Bridge velux:klf200:home    [ ipAddress="192.168.45.9", tcpPort=51200, password="verySecret" ] {

// Velux scenes

    Thing   scene   windowClosed    [ sceneName="V_DG_Window_Mitte_000" ]
    Thing   scene   windowUnlocked  [ sceneName="V_DG_Window_Mitte_005" ]
    Thing   scene   windowOpened    [ sceneName="V_DG_Window_Mitte_100" ]
    Thing   scene   unknownScene    [ sceneName="ThisIsADummySceneName" ]

// Velux IO-homecontrol devices

    Thing   window		V_DG_M_W    [ serial="56:23:3E:26:0C:1B:00:10" ]
    Thing   rollershutter	V_DG_M_S    [ serial="56:23:3E:26:0C:1B:00:10" ]
    Thing   rollershutter	V_DG_W_S    [ serial="53:09:40:5A:0C:2A:05:64" ]
    Thing   rollershuffer	V_DG_O_S    [ serial="53:09:40:5A:0C:23:0A:6E" ]
    Thing   actuator		V_SWITCH1   [ name="#4" ]
    Thing   actuator		V_SWITCH2   [ name="#5" ]

// Virtual rollershutter

    Thing   vshutter		V_WINDOW    [ sceneLevels="0,V_DG_Window_Mitte_000#5,V_DG_Window_Mitte_005#100,V_DG_Window_Mitte_100" ]
}
```

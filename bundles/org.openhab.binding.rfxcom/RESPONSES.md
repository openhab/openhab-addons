# Responses

Record of responses to some commands for working out how things work.

## Interface Message - Status Response

These messages were received when testing different firmwares.

```text
Ext    250   0D0100010253FA0400070001031C
Ext    251   0D0100010253FB0400070001031C
Ext    1001  140100010253010400070001031C03000000000000
Pro1   1044  1401000102532C04000700010300055D0000000000
Type1  1024  140100010253180000270001031C01000000000000
Type1  95    0D01000102535F0000270001031C
Type2  195   0D0100010253C30080270001031C
Type2  1022  140100010253160080270001031C02000000000000
```

## RFXMngr mode setting

These messages were sent by RFXMngr when enabling single modes.

On Pro1 firmware 1044 RFXtrx443 at 433.92MHZ

```text
enableUndecodedPackets        0D 00 00 03 03 53 00 80 00 00 00 00 00 00
enableImagintronixOpusPackets 0D 00 00 04 03 53 00 40 00 00 00 00 00 00
enableByronSXPackets          0D 00 00 05 03 53 00 20 00 00 00 00 00 00
enableRSLPackets              0D 00 00 06 03 53 00 10 00 00 00 00 00 00
enableLighting4Packets        0D 00 00 07 03 53 00 08 00 00 00 00 00 00
enableFineOffsetPackets       0D 00 00 08 03 53 00 04 00 00 00 00 00 00
enableRubicsonPackets         0D 00 00 09 03 53 00 02 00 00 00 00 00 00
enableAEPackets               0D 00 00 0A 03 53 00 01 00 00 00 00 00 00

enableBlindsT1T2T3T4Packets   0D 00 00 0B 03 53 00 00 80 00 00 00 00 00
enableBlindsT0Packets         0D 00 00 0C 03 53 00 00 40 00 00 00 00 00
? Blank in RFXmngr            0D 00 00 0C 03 53 00 00 20 00 00 00 00 00
Legrand CAD                   0D 00 00 0E 03 53 00 00 10 00 00 00 00 00
enableProGuardPackets         Not in RFXmngr
enableFS20Packets             Not in RFXmngr
enableLaCrossePackets         0D 00 00 0F 03 53 00 00 08 00 00 00 00 00
enableHidekiUPMPackets        0D 00 00 10 03 53 00 00 04 00 00 00 00 00
enableADPackets               0D 00 00 11 03 53 00 00 02 00 00 00 00 00
enableMertikPackets           0D 00 00 12 03 53 00 00 01 00 00 00 00 00

enableVisonicPackets          0D 00 00 13 03 53 00 00 00 80 00 00 00 00
enableATIPackets              0D 00 00 14 03 53 00 00 00 40 00 00 00 00
enableOregonPackets           0D 00 00 14 03 53 00 00 00 20 00 00 00 00
enableMeiantechPackets        0D 00 00 14 03 53 00 00 00 10 00 00 00 00
enableHomeEasyPackets         0D 00 00 14 03 53 00 00 00 08 00 00 00 00
enableACPackets               0D 00 00 14 03 53 00 00 00 04 00 00 00 00
enableARCPackets              0D 00 00 14 03 53 00 00 00 02 00 00 00 00
enableX10Packets              0D 00 00 14 03 53 00 00 00 01 00 00 00 00

enableHomeConfortPackets      0D 00 00 14 03 53 00 00 00 00 02 00 00 00
enableKEELOQPackets           0D 00 00 14 03 53 00 00 00 00 01 00 00 00
```

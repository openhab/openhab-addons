package org.openhab.binding.insteonplm.internal.message;

/**
 * The list of x10 commands for connecting to the device.
 * 
 * @author Bernd Pfrommer
 * @since 1.7.0
 */
public enum X10Command {
    AllUnitsOff, // 0
    AllUnitsOn, // 1
    On, // 2
    Off, // 3
    Dim, // 4
    Bright, // 5
    AllLightsOff, // 6
    ExtendedCode, // 7
    HailRequest, // 8
    HailAcknowledge, // 9
    PreSetDim, // 10
    PreSetDim2, // 11
    ExtendedDataAnalog, // 12
    Status0, // 13
    Status1, // 14
    StatusRequest // 15
}

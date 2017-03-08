package org.openhab.binding.insteonplm.internal.message;

/**
 * This class takes data coming from the serial port and turns it
 * into an message. For that, it has to figure out the length of the
 * message from the header, and read enough bytes until it hits the
 * message boundary. The code is tricky, partly because the Insteon protocol is.
 * Most of the time the command code (second byte) is enough to determine the length
 * of the incoming message, but sometimes one has to look deeper into the message
 * to determine if it is a standard or extended message (their lengths differ).
 *
 * @author Bernd Pfrommer
 * @since 1.5.0
 */
public enum StandardInsteonMessages {
    AssignToGroup(0x01),
    DeleteFromGroup(0x02),
    ProductDataRequest(0x03),
    EnterLinkingMode(0x09),
    EnterUnlinkingMode(0x0A),
    GetInsteonEngineVersion(0x0D),
    Ping(0x0f),
    IdRequest(0x10),
    LightOn(0x11),
    LightOnFast(0x12),
    LightOff(0x13),
    LightOffFast(0x14),
    Bright(0x15),
    Dim(0x16),
    StartManualChange(0x17),
    StopManualChange(0x18),
    LightStatusRequest(0x19),
    StatusReport(0x1A),
    ReadLastLevel(0x1B),
    SetLastLevel(0x1C),
    ReadPresetLevel(0x1D),
    SetPresetLevel(0x1E),
    GetOperatingFlags(0x1F),
    SetOperatingFlags(0x20),
    DeleteGroupX10Address(0x21),
    LoadOff(0x22),
    LoadOn(0x23),
    DoReadEE(0x24),
    LevelPoke(0x25),
    RatePoke(0x26),
    CurrentStatus(0x27),
    SetAddressMSB(0x28),
    Poke(0x29),
    PokeExtended(0x2A),
    Peek(0x2B),
    PeekInternal(0x2C),
    PokeInternal(0x2D),
    LightOnWithRamp(0x2E),
    LightOffWithRamp(0x2F),
    PoolOn(0x50),
    PoolOff(0x51),
    PoolTemperatureUp(0x52),
    PoolTemperatureDown(0x53),
    PoolControl(0x54),
    ThermostatTemperatureUp(0x68),
    ThermostatTemperatureDown(0x69),
    ThermostatGetZoneInformation(0x6A),
    ThermostatControl(0x6B),
    ThermostatSetCoolSetpoint(0x6C),
    ThermostatSetHeatSetpoint(0x6D),
    ResetPowerMeter(0x80),
    AssignToCompanionGroup(0x81),
    UpdatePowerMeter(0x82);

    private final int cmd;

    /**
     * Creates the standard message with the cmd byte.
     *
     * @param cmd The byte command
     */
    StandardInsteonMessages(int cmd) {
        this.cmd = cmd;
    }

    /**
     * Get the command as a byte.
     */
    public byte getCmd() {
        return (byte) (cmd & 0xff);
    }

    /**
     * Find the message from the cmd number.
     *
     * @return The message for the command.
     */
    public static StandardInsteonMessages fromByte(int cmd) {
        for (StandardInsteonMessages mess : StandardInsteonMessages.values()) {
            if (mess.getCmd() == cmd) {
                return mess;
            }
        }
        return null;
    }
}

package org.openhab.binding.insteonplm.internal.message;

/**
 * The message pieces to use for sending an extended message.
 *
 * @author David Bennett - Initial Contribution
 */
public enum ExtendedInsteonMessage {
    FxUsernameResponse(0x0301),
    SetDeviceTextString(0x0303),
    SetAllLinkCommandAlias(0x0304),
    SetAllLinkCommandAlisExtended(0x0305),
    BlockDataTransferFailure(0x2A00),
    BlockDataTransferCompleteOneByte(0x2A01),
    BlockDataTransferCompleteTwoBytes(0x2A02),
    BlockDataTransferCompleteThreeBytes(0x2A03),
    BlockDataTransferCompleteFourBytes(0x2A04),
    BlockDataTransferCompleteFiveBytes(0x2A05),
    BlockDataTransferCompleteSixBytes(0x2A06),
    BlockDataTransferCompleteSevenBytes(0x2A07),
    BlockDataTransferCompleteEightBytes(0x2A08),
    BlockDataTransferCompleteNineBytes(0x2A09),
    BlockDataTransferCompleteTenBytes(0x2A0A),
    BlockDataTransferCompleteElevenBytes(0x2A0B),
    BlockDataTransferCompleteTwelveBytes(0x2A0C),
    BlockDataTransferContinues(0x2A0D),
    BlockDataTransferRequest(0x2AFF),
    ExtendedGetSet(0x2E00),
    ReadWriteAllLinkDatabase(0x2F00),
    TriggerAllLinkCommand(0x3000),
    SetSpinklerProgram(0x4000),
    GetSpinklerProgramResponse(0x4100),
    IOSetSensorNominal(0x4B00),
    IOAlarmDataResponse(0x4C00),
    PoolSetDeviceTemperature(0x5000),
    PoolSetDeviveHysteresis(0x5001),
    ThermostatZoneTemperatureUp(0x6800),
    ThermostatZoneTemperatureDown(0x6900),
    ThermostatSetZoneCoolSetpoint(0x6C00),
    ThemostatSetZoneHeatSetpoint(0x6D00);

    private final int cmd;

    ExtendedInsteonMessage(int cmd) {
        this.cmd = cmd;
    }

    /**
     * @return Both byes for the inston message.
     */
    public int getFullCmd() {
        return cmd;
    }

    /**
     * @return The first bye to use in the insteon message.
     */
    public byte getCmd1() {
        return (byte) ((cmd & 0xff00) >> 8);
    }

    /**
     * @return The second bye to use in the insteon message.
     */
    public byte getCmd2() {
        return (byte) (cmd & 0xff);
    }
}

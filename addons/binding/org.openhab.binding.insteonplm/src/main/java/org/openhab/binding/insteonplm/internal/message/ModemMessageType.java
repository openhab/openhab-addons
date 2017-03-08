package org.openhab.binding.insteonplm.internal.message;

/**
 * Contains an Insteon Message consisting of the raw data, and the message definition.
 * For more info, see the public Insteon Developer's Guide, 2nd edition,
 * and the Insteon Modem Developer's Guide.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @since 1.5.0
 */

public enum ModemMessageType {
    PureAck(0x6),
    PureNack(0x15),
    StandardMessageReceived(0x50),
    ExtendedMessageReceived(0x51),
    X10MessageReceived(0x52),
    AllLinkingCompleted(0x53),
    ButtonEventReported(0x54),
    UserResetDetected(0x55),
    AllLinkCleanupFailureReport(0x56),
    AllLinkRecordResponse(0x57),
    AllLinkCleanupStatusReport(0x58),
    GetImInfo(0x60),
    SendAllLinkCommand(0x61),
    SendInsteonMessage(0x62),
    SendX10(0x63),
    StartAllLinking(0x64),
    CancelAllLinking(0x65),
    SetHostDeviceCategory(0x66),
    ResetTheIm(0x67),
    SetInsteonAckMessageByte(0x68),
    GetFirstAllLinkRecord(0x69),
    GetNextAllLinkRecord(0x6A),
    SetImConfiguration(0x6B),
    GetAllLinkRecordForSender(0x6C),
    LedOn(0x6D),
    LedOff(0x6E),
    ManageAllLinkRecord(0x6F),
    SetInsteonNakMessageByte(0x70),
    SetInsteonAckMessageTwoBytes(0x71),
    RFSleep(0x72),
    GetImConfiguration(0x73);

    private final int num;

    ModemMessageType(int num) {
        this.num = num;
    }

    public int getCommand() {
        return num;
    }

    public static ModemMessageType fromCommand(int num) {
        for (ModemMessageType ty : ModemMessageType.values()) {
            if (ty.getCommand() == num) {
                return ty;
            }
        }
        return null;
    }
}

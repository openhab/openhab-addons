package org.openhab.binding.insteonplm.internal.message;

/**
 * Contains an Insteon Message consisting of the raw data, and the message definition.
 * For more info, see the public Insteon Developer's Guide, 2nd edition,
 * and the Insteon Modem Developer's Guide. The sizes all include the 0x02 at the start
 * of the message.
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @since 1.5.0
 */

public enum ModemMessageType {
    PureAck(0x6, 2),
    PureNack(0x15, 2),
    StandardMessageReceived(0x50, 11),
    ExtendedMessageReceived(0x51, 23),
    X10MessageReceived(0x52, 4),
    AllLinkingCompleted(0x53, 10),
    ButtonEventReported(0x54, 3),
    UserResetDetected(0x55, 2),
    AllLinkCleanupFailureReport(0x56, 7),
    AllLinkRecordResponse(0x57, 10),
    AllLinkCleanupStatusReport(0x58, 3),
    GetImInfo(0x60, 9),
    SendAllLinkCommand(0x61, 6),
    SendInsteonMessage(0x62, 9),
    SendX10(0x63, 5),
    StartAllLinking(0x64, 5),
    CancelAllLinking(0x65, 3),
    SetHostDeviceCategory(0x66, 6),
    ResetTheIm(0x67, 3),
    SetInsteonAckMessageByte(0x68, 4),
    GetFirstAllLinkRecord(0x69, 3),
    GetNextAllLinkRecord(0x6A, 3),
    SetImConfiguration(0x6B, 4),
    GetAllLinkRecordForSender(0x6C, 3),
    LedOn(0x6D, 3),
    LedOff(0x6E, 3),
    ManageAllLinkRecord(0x6F, 12),
    SetInsteonNakMessageByte(0x70, 4),
    SetInsteonAckMessageTwoBytes(0x71, 5),
    RFSleep(0x72, 3),
    GetImConfiguration(0x73, 6);

    private final int num;
    private final int length;

    ModemMessageType(int num, int len) {
        this.num = num;
        this.length = len;
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

    public int getReceiveLength() {
        return length;
    }
}

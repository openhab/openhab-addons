package io.flic.fliclib.javaclient;

import java.io.*;
import java.nio.charset.StandardCharsets;

import io.flic.fliclib.javaclient.enums.*;

/**
 * Flic Protocol Packets
 */

abstract class CommandPacket {
    protected int opcode;

    public final byte[] construct() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            write(stream);
        } catch (IOException e) {
        }
        byte[] res = new byte[3 + stream.size()];
        res[0] = (byte)(1 + stream.size());
        res[1] = (byte)((1 + stream.size()) >> 8);
        res[2] = (byte)opcode;
        System.arraycopy(stream.toByteArray(), 0, res, 3, stream.size());
        return res;
    }

    protected abstract void write(OutputStream stream) throws IOException;
}

class CmdGetInfo extends CommandPacket {
    @Override
    protected void write(OutputStream stream) {
        opcode = 0;
    }
}

class CmdCreateScanner extends CommandPacket {
    public int scanId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 1;
        StreamUtils.writeInt32(stream, scanId);
    }
}

class CmdRemoveScanner extends CommandPacket {
    public int scanId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 2;
        StreamUtils.writeInt32(stream, scanId);
    }
}

class CmdCreateConnectionChannel extends CommandPacket {
    public int connId;
    public Bdaddr bdaddr;
    public LatencyMode latencyMode;
    public short autoDisconnectTime;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 3;
        StreamUtils.writeInt32(stream, connId);
        StreamUtils.writeBdaddr(stream, bdaddr);
        StreamUtils.writeEnum(stream, latencyMode);
        StreamUtils.writeInt16(stream, autoDisconnectTime);
    }
}

class CmdRemoveConnectionChannel extends CommandPacket {
    public int connId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 4;
        StreamUtils.writeInt32(stream, connId);
    }
}

class CmdForceDisconnect extends CommandPacket {
    public Bdaddr bdaddr;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 5;
        StreamUtils.writeBdaddr(stream, bdaddr);
    }
}

class CmdChangeModeParameters extends CommandPacket {
    public int connId;
    public LatencyMode latencyMode;
    public short autoDisconnectTime;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 6;
        StreamUtils.writeInt32(stream, connId);
        StreamUtils.writeEnum(stream, latencyMode);
        StreamUtils.writeInt16(stream, autoDisconnectTime);
    }
}

class CmdPing extends CommandPacket {
    public int pingId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 7;
        StreamUtils.writeInt32(stream, pingId);
    }
}

class CmdGetButtonInfo extends CommandPacket {
    public Bdaddr bdaddr;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 8;
        StreamUtils.writeBdaddr(stream, bdaddr);
    }
}

class CmdCreateScanWizard extends CommandPacket {
    public int scanWizardId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 9;
        StreamUtils.writeInt32(stream, scanWizardId);
    }
}

class CmdCancelScanWizard extends CommandPacket {
    public int scanWizardId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 10;
        StreamUtils.writeInt32(stream, scanWizardId);
    }
}

class CmdDeleteButton extends CommandPacket {
    public Bdaddr bdaddr;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 11;
        StreamUtils.writeBdaddr(stream, bdaddr);
    }
}

class CmdCreateBatteryStatusListener extends CommandPacket {
    public int listenerId;
    public Bdaddr bdaddr;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 12;
        StreamUtils.writeInt32(stream, listenerId);
        StreamUtils.writeBdaddr(stream, bdaddr);
    }
}

class CmdRemoveBatteryStatusListener extends CommandPacket {
    public int listenerId;

    @Override
    protected void write(OutputStream stream) throws IOException {
        opcode = 13;
        StreamUtils.writeInt32(stream, listenerId);
    }
}

abstract class EventPacket {
    public static final int EVT_ADVERTISEMENT_PACKET_OPCODE = 0;
    public static final int EVT_CREATE_CONNECTION_CHANNEL_RESPONSE_OPCODE = 1;
    public static final int EVT_CONNECTION_STATUS_CHANGED_OPCODE = 2;
    public static final int EVT_CONNECTION_CHANNEL_REMOVED_OPCODE = 3;
    public static final int EVT_BUTTON_UP_OR_DOWN_OPCODE = 4;
    public static final int EVT_BUTTON_CLICK_OR_HOLD_OPCODE = 5;
    public static final int EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OPCODE = 6;
    public static final int EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OR_HOLD_OPCODE = 7;
    public static final int EVT_NEW_VERIFIED_BUTTON_OPCODE = 8;
    public static final int EVT_GET_INFO_RESPONSE_OPCODE = 9;
    public static final int EVT_NO_SPACE_FOR_NEW_CONNECTION_OPCODE = 10;
    public static final int EVT_GOT_SPACE_FOR_NEW_CONNECTION_OPCODE = 11;
    public static final int EVT_BLUETOOTH_CONTROLLER_STATE_CHANGE_OPCODE = 12;
    public static final int EVT_PING_RESPONSE_OPCODE = 13;
    public static final int EVT_GET_BUTTON_INFO_RESPONSE_OPCODE = 14;
    public static final int EVT_SCAN_WIZARD_FOUND_PRIVATE_BUTTON_OPCODE = 15;
    public static final int EVT_SCAN_WIZARD_FOUND_PUBLIC_BUTTON_OPCODE = 16;
    public static final int EVT_SCAN_WIZARD_BUTTON_CONNECTED_OPCODE = 17;
    public static final int EVT_SCAN_WIZARD_COMPLETED_OPCODE = 18;
    public static final int EVT_BUTTON_DELETED_OPCODE = 19;
    public static final int EVT_BATTERY_STATUS_OPCODE = 20;

    public void parse(byte[] arr) {
        InputStream stream = new ByteArrayInputStream(arr);
        try {
            stream.skip(1);
            parseInternal(stream);
        } catch(IOException e) {
        }
    }

    protected abstract void parseInternal(InputStream stream) throws IOException;
}

class EvtAdvertisementPacket extends EventPacket {
    public int scanId;
    public Bdaddr addr;
    public String name;
    public int rssi;
    public boolean isPrivate;
    public boolean alreadyVerified;
    public boolean alreadyConnectedToThisDevice;
    public boolean alreadyConnectedToOtherDevice;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        scanId = StreamUtils.getInt32(stream);
        addr = StreamUtils.getBdaddr(stream);
        name = StreamUtils.getString(stream, 16);
        rssi = StreamUtils.getInt8(stream);
        isPrivate = StreamUtils.getBoolean(stream);
        alreadyVerified = StreamUtils.getBoolean(stream);
        alreadyConnectedToThisDevice = StreamUtils.getBoolean(stream);
        alreadyConnectedToOtherDevice = StreamUtils.getBoolean(stream);
    }
}

class EvtCreateConnectionChannelResponse extends EventPacket {
    public int connId;
    public CreateConnectionChannelError connectionChannelError;
    public ConnectionStatus connectionStatus;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        connId = StreamUtils.getInt32(stream);
        connectionChannelError = CreateConnectionChannelError.values()[StreamUtils.getUInt8(stream)];
        connectionStatus = ConnectionStatus.values()[StreamUtils.getUInt8(stream)];
    }
}

class EvtConnectionStatusChanged extends EventPacket {
    public int connId;
    public ConnectionStatus connectionStatus;
    public DisconnectReason disconnectReason;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        connId = StreamUtils.getInt32(stream);
        connectionStatus = ConnectionStatus.values()[StreamUtils.getUInt8(stream)];
        disconnectReason = DisconnectReason.values()[StreamUtils.getUInt8(stream)];
    }
}

class EvtConnectionChannelRemoved extends EventPacket {
    public int connId;
    public RemovedReason removedReason;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        connId = StreamUtils.getInt32(stream);
        removedReason = RemovedReason.values()[StreamUtils.getUInt8(stream)];
    }
}

class EvtButtonEvent extends EventPacket {
    public int connId;
    public ClickType clickType;
    public boolean wasQueued;
    public int timeDiff;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        connId = StreamUtils.getInt32(stream);
        clickType = ClickType.values()[StreamUtils.getUInt8(stream)];
        wasQueued = StreamUtils.getBoolean(stream);
        timeDiff = StreamUtils.getInt32(stream);
    }
}

class EvtNewVerifiedButton extends EventPacket {
    public Bdaddr bdaddr;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        bdaddr = StreamUtils.getBdaddr(stream);
    }
}

class EvtGetInfoResponse extends EventPacket {
    public BluetoothControllerState bluetoothControllerState;
    public Bdaddr myBdAddr;
    public BdAddrType myBdAddrType;
    public int maxPendingConnections;
    public int maxConcurrentlyConnectedButtons;
    public int currentPendingConnections;
    public boolean currentlyNoSpaceForNewConnections;
    public Bdaddr[] bdAddrOfVerifiedButtons;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        bluetoothControllerState = BluetoothControllerState.values()[StreamUtils.getUInt8(stream)];
        myBdAddr = StreamUtils.getBdaddr(stream);
        myBdAddrType = BdAddrType.values()[StreamUtils.getUInt8(stream)];
        maxPendingConnections = StreamUtils.getUInt8(stream);
        maxConcurrentlyConnectedButtons = StreamUtils.getInt16(stream);
        currentPendingConnections = StreamUtils.getUInt8(stream);
        currentlyNoSpaceForNewConnections = StreamUtils.getBoolean(stream);
        int nbVerifiedButtons = StreamUtils.getUInt16(stream);
        bdAddrOfVerifiedButtons = new Bdaddr[nbVerifiedButtons];
        for (int i = 0; i < nbVerifiedButtons; i++) {
            bdAddrOfVerifiedButtons[i] = StreamUtils.getBdaddr(stream);
        }
    }
}

class EvtNoSpaceForNewConnection extends EventPacket {
    public int maxConcurrentlyConnectedButtons;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        maxConcurrentlyConnectedButtons = StreamUtils.getUInt8(stream);
    }
}

class EvtGotSpaceForNewConnection extends EventPacket {
    public int maxConcurrentlyConnectedButtons;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        maxConcurrentlyConnectedButtons = StreamUtils.getUInt8(stream);
    }
}

class EvtBluetoothControllerStateChange extends EventPacket {
    public BluetoothControllerState state;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        state = BluetoothControllerState.values()[StreamUtils.getUInt8(stream)];
    }
}

class EvtGetButtonInfoResponse extends EventPacket {
    public Bdaddr bdaddr;
    public String uuid;
    public String color;
    public String serialNumber;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        bdaddr = StreamUtils.getBdaddr(stream);
        byte[] uuidBytes = StreamUtils.getByteArr(stream, 16);
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("%02x", uuidBytes[i]));
        }
        uuid = sb.toString();
        if ("00000000000000000000000000000000".equals(uuid)) {
            uuid = null;
        }
        color = StreamUtils.getString(stream, 16);
        if (color.isEmpty()) {
            color = null;
        }
        serialNumber = StreamUtils.getString(stream, 16);
        if (serialNumber.isEmpty()) {
            serialNumber = null;
        }
    }
}

class EvtScanWizardFoundPrivateButton extends EventPacket {
    public int scanWizardId;
    
    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        scanWizardId = StreamUtils.getInt32(stream);
    }
}

class EvtScanWizardFoundPublicButton extends EventPacket {
    public int scanWizardId;
    public Bdaddr addr;
    public String name;
    
    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        scanWizardId = StreamUtils.getInt32(stream);
        addr = StreamUtils.getBdaddr(stream);
        int nameLen = StreamUtils.getUInt8(stream);
        byte[] bytes = new byte[nameLen];
        for (int i = 0; i < nameLen; i++) {
            bytes[i] = (byte)stream.read();
        }
        for (int i = nameLen; i < 16; i++) {
            stream.skip(1);
        }
        name = new String(bytes, StandardCharsets.UTF_8);
    }
}

class EvtScanWizardButtonConnected extends EventPacket {
    public int scanWizardId;
    
    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        scanWizardId = StreamUtils.getInt32(stream);
    }
}

class EvtScanWizardCompleted extends EventPacket {
    public int scanWizardId;
    public ScanWizardResult result;
    
    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        scanWizardId = StreamUtils.getInt32(stream);
        result = ScanWizardResult.values()[StreamUtils.getUInt8(stream)];
    }
}

class EvtButtonDeleted extends EventPacket {
    public Bdaddr bdaddr;
    public boolean deletedByThisClient;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        bdaddr = StreamUtils.getBdaddr(stream);
        deletedByThisClient = StreamUtils.getBoolean(stream);
    }
}

class EvtBatteryStatus extends EventPacket {
    public int listenerId;
    public int batteryPercentage;
    public long timestamp;

    @Override
    protected void parseInternal(InputStream stream) throws IOException {
        listenerId = StreamUtils.getInt32(stream);
        batteryPercentage = StreamUtils.getInt8(stream);
        timestamp = StreamUtils.getInt64(stream);
    }
}

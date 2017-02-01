/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the protection command class.
 *
 * @author Chris Jackson
 * @author Jorg de Jong
 */
@XStreamAlias("protectionCommandClass")
public class ZWaveProtectionCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState, ZWaveGetCommands {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveProtectionCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    public static final int PROTECTION_SET = 1;
    public static final int PROTECTION_GET = 2;
    public static final int PROTECTION_REPORT = 3;

    // Version 2
    public static final int PROTECTION_SUPPORTED_GET = 0x04;
    public static final int PROTECTION_SUPPORTED_REPORT = 0x05;
    public static final int PROTECTION_EXCLUSIVECONTROL_SET = 0x06;
    public static final int PROTECTION_EXCLUSIVECONTROL_GET = 0x07;
    public static final int PROTECTION_EXCLUSIVECONTROL_REPORT = 0x08;
    public static final int PROTECTION_TIMEOUT_SET = 0x09;
    public static final int PROTECTION_TIMEOUT_GET = 0x0a;
    public static final int PROTECTION_TIMEOUT_REPORT = 0x0b;

    // PROTECTION_SUPPORTED_REPORT
    private static final int TIMEOUT_BITMASK = 0x01;
    private static final int EXCLUSIVE_CONTROL_BITMASK = 0x02;

    @XStreamOmitField
    private boolean supportedInitialised = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    @XStreamOmitField
    private LocalProtectionType currentLocalMode;

    private List<LocalProtectionType> localModes = new ArrayList<>();
    private List<RfProtectionType> rfModes = new ArrayList<>();

    /**
     * Creates a new instance of the ZWaveProtectionCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveProtectionCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.PROTECTION;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received PROTECTION command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case PROTECTION_REPORT:
                int localMode = serialMessage.getMessagePayloadByte(offset + 1) & 0x0f;

                if (localMode < LocalProtectionType.values().length) {
                    currentLocalMode = LocalProtectionType.values()[localMode];
                    ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(),
                            endpoint, getCommandClass(), currentLocalMode, Type.PROTECTION_LOCAL);
                    getController().notifyEventListeners(zEvent);
                }
                if (getVersion() > 1) {
                    int rfMode = serialMessage.getMessagePayloadByte(offset + 2) & 0x0f;
                    if (rfMode < RfProtectionType.values().length) {
                        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(),
                                endpoint, getCommandClass(), RfProtectionType.values()[rfMode], Type.PROTECTION_RF);
                        getController().notifyEventListeners(zEvent);
                    }
                    logger.debug("NODE {}: Received protection report local:{} rf:{}", getNode().getNodeId(),
                            LocalProtectionType.values()[localMode], RfProtectionType.values()[rfMode]);
                } else {
                    logger.debug("NODE {}: Received protection report local:{}", getNode().getNodeId(),
                            LocalProtectionType.values()[localMode]);
                }

                dynamicDone = true;
                break;
            case PROTECTION_SUPPORTED_REPORT:
                boolean exclusive = ((serialMessage.getMessagePayloadByte(offset + 1)
                        & EXCLUSIVE_CONTROL_BITMASK) != 0);
                boolean timeout = ((serialMessage.getMessagePayloadByte(offset + 1) & TIMEOUT_BITMASK) != 0);

                int localStateMask = (serialMessage.getMessagePayloadByte(offset + 2)
                        | serialMessage.getMessagePayloadByte(offset + 3) << 8);
                int rfStateMask = (serialMessage.getMessagePayloadByte(offset + 4)
                        | serialMessage.getMessagePayloadByte(offset + 5) << 8);

                LocalProtectionType localTypes[] = LocalProtectionType.values();
                for (int i = 0; i < localTypes.length; i++) {
                    if ((localStateMask >> i & 0x01) > 0) {
                        localModes.add(localTypes[i]);
                    }
                }
                RfProtectionType rfTypes[] = RfProtectionType.values();
                for (int i = 0; i < rfTypes.length; i++) {
                    if ((rfStateMask >> i & 0x01) > 0) {
                        rfModes.add(rfTypes[i]);
                    }
                }

                logger.debug(
                        "NODE {}: Received protection supported report Exclusive({}), Timeout({}},  Local states={}, RF states={}",
                        getNode().getNodeId(), exclusive ? "supported" : "Not supported",
                        timeout ? "supported" : "Not supported", localModes, rfModes);
                supportedInitialised = true;
                break;

            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the PROTECTION_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: PROTECTION_SUPPORTED_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command PROTECTION_SUPPORTED_GET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(PROTECTION_SUPPORTED_GET);

        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    /**
     * Gets a SerialMessage with the PROTECTION_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for command PROTECTION_GET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(2);
        outputData.write(getCommandClass().getKey());
        outputData.write(PROTECTION_GET);

        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    /**
     * Gets a SerialMessage with the PROTECTION_SET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage setValueMessage(LocalProtectionType localMode, RfProtectionType rfMode) {
        logger.debug("NODE {}: Creating new message for command PROTECTION_SET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        LocalProtectionType newLocalMode = localMode != null ? localMode : currentLocalMode;

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        if (getVersion() < 2 || rfMode == null) {
            outputData.write(getNode().getNodeId());
            outputData.write(3);
            outputData.write(getCommandClass().getKey());
            outputData.write(PROTECTION_SET);
            outputData.write(newLocalMode.ordinal());
        } else {
            outputData.write(getNode().getNodeId());
            outputData.write(4);
            outputData.write(getCommandClass().getKey());
            outputData.write(PROTECTION_SET);
            outputData.write(newLocalMode.ordinal());
            outputData.write(rfMode.ordinal());

        }
        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<>();
        if (getVersion() < 2) {
            return result;
        }

        if (refresh == true || supportedInitialised == false) {
            result.add(getSupportedMessage());
        }
        return result;
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<>();
        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }

    /**
     * Reported type of protection.
     */
    public enum Type {
        PROTECTION_LOCAL,
        PROTECTION_RF
    }

    /**
     * Z-Wave LocalProtectionType enumeration. The LocalProtection type indicates the type of local protection that is
     * reported.
     *
     * @author Jorg de Jong
     */
    @XStreamAlias("localProtection")
    public enum LocalProtectionType {
        UNPROTECTED,
        SEQUENCE,
        PROTECTED;
    }

    /**
     * Z-Wave RfProtectionType enumeration. The RfProtection type indicates the type of RF protection that is reported.
     *
     * @author Jorg de Jong
     */
    @XStreamAlias("rfProtection")
    public enum RfProtectionType {
        UNPROTECTED,
        NORFCONTROL,
        NORFRESPONSE;
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Color command class.
 *
 * @author Chris Jackson
 */

@XStreamAlias("colorCommandClass")
public class ZWaveColorCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveColorCommandClass.class);

    private static final int COLOR_CAPABILITY_GET = 0x01;
    private static final int COLOR_CAPABILITY_REPORT = 0x02;
    private static final int COLOR_GET = 0x03;
    private static final int COLOR_REPORT = 0x04;
    private static final int COLOR_SET = 0x05;

    private final Set<ZWaveColorType> supportedColors = new HashSet<ZWaveColorType>();
    private final Set<ZWaveColorType> refreshList = new HashSet<ZWaveColorType>();

    private final Map<ZWaveColorType, Integer> colorMap = new HashMap<ZWaveColorType, Integer>();

    @XStreamOmitField
    private boolean initialiseDone = false;
    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveColorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveColorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COLOR;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case COLOR_CAPABILITY_REPORT:
                logger.trace("NODE {}: Process Color Report", this.getNode().getNodeId());

                int supportedColors = serialMessage.getMessagePayloadByte(offset + 1);
                for (int i = 0; i < 8; ++i) {
                    if ((supportedColors & (1 << i)) == (1 << i)) {
                        ZWaveColorType color = ZWaveColorType.getColorType(i);

                        if (color == null) {
                            logger.warn("NODE {}: Invalid color {}", this.getNode().getNodeId(), i);
                            continue;
                        }

                        logger.debug("NODE {}: Color Supported = {}({})", this.getNode().getNodeId(), color.getLabel(),
                                color.getKey());

                        // Add color to the list of supported colors.
                        if (!this.supportedColors.contains(color)) {
                            this.supportedColors.add(color);
                        }
                    }
                }

                initialiseDone = true;
                break;
            case COLOR_SET:
                logger.debug("NODE {}: Process Color SET", this.getNode().getNodeId());
                processColorReport(serialMessage, offset, endpoint);
                break;
            case COLOR_REPORT:
                logger.debug("NODE {}: Process Color REPORT", this.getNode().getNodeId());
                processColorReport(serialMessage, offset, endpoint);
                break;
            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        this.getCommandClass().getLabel(), this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a COLOR_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processColorReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int color = serialMessage.getMessagePayloadByte(offset + 1);
        int level = serialMessage.getMessagePayloadByte(offset + 2);
        ZWaveColorType colorType = ZWaveColorType.getColorType(color);
        if (colorType == null) {
            logger.error("NODE {}: Color report for unknown color {} ({})", this.getNode().getNodeId(), color, level);
            return;
        }

        logger.info("NODE {}: Color report {} {}", this.getNode().getNodeId(), colorType.toString(), level);

        // Update our knowledge of the color
        colorMap.put(colorType, level);

        // See if we're done updating
        refreshList.remove(colorType);
        if (refreshList.isEmpty()) {
            // Yes - notify of a new color
            logger.info("NODE {}: Color report finished {}", this.getNode().getNodeId(), colorMap);
            ZWaveCommandClassValueEvent zEvent = new ZWaveColorValueEvent(this.getNode().getNodeId(), 0, colorMap);
            this.getController().notifyEventListeners(zEvent);
        }
    }

    /**
     * Gets a SerialMessage with the COLOR_GET command
     *
     * @return the serial message
     */
    public SerialMessage getValueMessage(int color) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command COLOR_GET {}", this.getNode().getNodeId(),
                color);
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(), (byte) COLOR_GET,
                (byte) color };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the COLOR_CAPABILITY_GET command
     *
     * @return the serial message
     */
    public SerialMessage getCapabilityMessage() {
        logger.debug("NODE {}: Creating new message for application command COLOR_CAPABILITY_GET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) COLOR_CAPABILITY_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the COLOR_SET command
     *
     * @param the level to set.
     * @return the serial message
     */
    public SerialMessage setValueMessage(int channel, int level) {
        logger.debug("NODE {}: Creating new message for application command COLOR_SET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 4, (byte) getCommandClass().getKey(), (byte) COLOR_SET,
                (byte) 3, (byte) level };
        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(this.getCapabilityMessage());
        }
        return result;
    }

    /**
     * Request the state (all colours) of the device
     *
     * @return collection of requests
     */
    public Collection<SerialMessage> getColor() {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refreshList.isEmpty() == false) {
            logger.debug("NODE {}: Color refresh is already in progress", this.getNode());
            return result;
        }

        // Request all colors supported by the bulb
        // We keep a list of the requests we've made so we know when we're done
        refreshList.clear();
        colorMap.clear();
        for (ZWaveColorType color : supportedColors) {
            refreshList.add(color);
            result.add(getValueMessage(color.getKey()));
        }

        return result;
    }

    /**
     * Set the state (all colours) of the device
     *
     * @return collection of requests
     */
    public Collection<SerialMessage> setColor(int red, int green, int blue, int coldWhite, int warmWhite) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (red > 255) {
            red = 255;
        }
        if (blue > 255) {
            blue = 255;
        }
        if (green > 255) {
            green = 255;
        }
        if (warmWhite > 255) {
            warmWhite = 255;
        }
        if (coldWhite > 255) {
            coldWhite = 255;
        }

        int len;
        if (getVersion() > 1) {
            len = 14;
        } else {
            len = 13;
        }

        SerialMessage msg = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        byte[] newPayload = new byte[len + 2];
        newPayload[0] = (byte) this.getNode().getNodeId();
        newPayload[1] = (byte) len;
        newPayload[2] = (byte) getCommandClass().getKey();
        newPayload[3] = (byte) COLOR_SET;
        newPayload[4] = 5;
        newPayload[5] = (byte) ZWaveColorType.RED.getKey();
        newPayload[6] = (byte) red;
        newPayload[7] = (byte) ZWaveColorType.GREEN.getKey();
        newPayload[8] = (byte) green;
        newPayload[9] = (byte) ZWaveColorType.BLUE.getKey();
        newPayload[10] = (byte) blue;
        newPayload[11] = (byte) ZWaveColorType.WARM_WHITE.getKey();
        newPayload[12] = (byte) warmWhite;
        newPayload[13] = (byte) ZWaveColorType.COLD_WHITE.getKey();
        newPayload[14] = (byte) coldWhite;
        if (getVersion() > 1) {
            // Add the transition duration
            newPayload[15] = (byte) 255;
        }
        msg.setMessagePayload(newPayload);
        result.add(msg);

        return result;
    }

    /**
     * Z-Wave ColorType enumeration.
     *
     * @author Chris Jackson
     */
    @XStreamAlias("colorType")
    public enum ZWaveColorType {
        WARM_WHITE(0, "Warm White"),
        COLD_WHITE(1, "Cold White"),
        RED(2, "Red"),
        GREEN(3, "Green"),
        BLUE(4, "Blue"),
        AMBER(5, "Amber"),
        CYAN(6, "Cyan"),
        PURPLE(7, "Purple"),
        INDEX(8, "Indexed Color");

        /**
         * A mapping between the integer code and its corresponding color type
         * to facilitate lookup by code.
         */
        private static Map<Integer, ZWaveColorType> codeToColorTypeMapping;

        private int key;
        private String label;

        private ZWaveColorType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToColorTypeMapping = new HashMap<Integer, ZWaveColorType>();
            for (ZWaveColorType s : values()) {
                codeToColorTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the color type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the color type.
         */
        public static ZWaveColorType getColorType(int i) {
            if (codeToColorTypeMapping == null) {
                initMapping();
            }

            return codeToColorTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Z-Wave Color Event class. Indicates that an color value changed.
     *
     * @author Chris Jackson
     */
    public class ZWaveColorValueEvent extends ZWaveCommandClassValueEvent {
        Map<ZWaveColorType, Integer> colorMap;

        /**
         * Constructor. Creates a instance of the ZWaveColorValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param colorType the color type that triggered the event;
         * @param value the value for the event.
         */
        private ZWaveColorValueEvent(int nodeId, int endpoint, Map<ZWaveColorType, Integer> colorMap) {
            super(nodeId, endpoint, CommandClass.COLOR, 0);
            this.colorMap = colorMap;
        }

        /**
         * Gets the color type for this color value event.
         */
        public Map<ZWaveColorType, Integer> getColorMap() {
            return colorMap;
        }
    }
}

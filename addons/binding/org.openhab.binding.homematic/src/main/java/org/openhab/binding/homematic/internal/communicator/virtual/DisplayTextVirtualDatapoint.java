/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * The {@link DisplayTextVirtualDatapoint} adds multiple virtual datapoints to the HM-Dis-WM55 device to easily send
 * colored text and icons to the display.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class DisplayTextVirtualDatapoint extends AbstractVirtualDatapointHandler {
    private static final int DISPLAY_LINES = 5;

    private static final String DATAPOINT_NAME_DISPLAY_LINE = "DISPLAY_LINE_";
    private static final String DATAPOINT_NAME_DISPLAY_COLOR = "DISPLAY_COLOR_";
    private static final String DATAPOINT_NAME_DISPLAY_ICON = "DISPLAY_ICON_";
    private static final String DATAPOINT_NAME_DISPLAY_SUBMIT = "DISPLAY_SUBMIT";

    private static final String START = "0x02";
    private static final String STOP = "0x03";
    private static final String LINE = "0x12";
    private static final String COLOR = "0x11";
    private static final String LF = "0x0a";
    private static final String ICON = "0x13";

    private static Map<String, String> replaceMap = new HashMap<String, String>();

    // replace special chars while encoding
    static {
        replaceMap.put("d6", "23");
        replaceMap.put("dc", "24");
        replaceMap.put("3d", "27");
        replaceMap.put("c4", "5b");
        replaceMap.put("df", "5f");
        replaceMap.put("e4", "7b");
        replaceMap.put("f6", "7c");
        replaceMap.put("fc", "7d");
    }

    /**
     * Available text colors.
     */
    private enum Color {
        NONE(""),
        WHITE("0x80"),
        RED("0x81"),
        ORANGE("0x82"),
        YELLOW("0x83"),
        GREEN("0x84"),
        BLUE("0x85");

        private final String code;

        private Color(String code) {
            this.code = code;
        }

        protected String getCode() {
            return code;
        }

        /**
         * Returns the color code.
         */
        public static String getCode(String name) {
            try {
                return valueOf(name).getCode();
            } catch (Exception ex) {
                return null;
            }
        }

    }

    /**
     * Available icons.
     */
    private enum Icon {
        NONE(""),
        OFF("0x80"),
        ON("0x81"),
        OPEN("0x82"),
        CLOSED("0x83"),
        ERROR("0x84"),
        OK("0x85"),
        INFO("0x86"),
        NEW_MESSAGE("0x87"),
        SERVICE("0x88"),
        SIGNAL_GREEN("0x89"),
        SIGNAL_YELLOW("0x8a"),
        SIGNAL_RED("0x8b");

        private final String code;

        private Icon(String code) {
            this.code = code;
        }

        protected String getCode() {
            return code;
        }

        /**
         * Returns the color code.
         */
        public static String getCode(String name) {
            try {
                return valueOf(name).getCode();
            } catch (Exception ex) {
                return null;
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return DATAPOINT_NAME_DISPLAY_SUBMIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(HmDevice device) {
        if (device.getType().equals(DEVICE_TYPE_STATUS_DISPLAY)) {
            for (HmChannel channel : device.getChannels()) {
                if (channel.hasDatapoint(new HmDatapointInfo(HmParamsetType.VALUES, channel, DATAPOINT_NAME_SUBMIT))) {
                    for (int i = 1; i <= DISPLAY_LINES; i++) {
                        addDatapoint(device, channel.getNumber(), DATAPOINT_NAME_DISPLAY_LINE + i, HmValueType.STRING,
                                null, false);
                        HmDatapoint dpColor = addDatapoint(device, channel.getNumber(),
                                DATAPOINT_NAME_DISPLAY_COLOR + i, HmValueType.ENUM, null, false);
                        dpColor.setOptions(getEnumNames(Color.class));
                        dpColor.setMinValue(0);
                        dpColor.setMaxValue(Color.values().length);

                        HmDatapoint dpIcon = addDatapoint(device, channel.getNumber(), DATAPOINT_NAME_DISPLAY_ICON + i,
                                HmValueType.ENUM, null, false);
                        dpIcon.setOptions(getEnumNames(Icon.class));
                        dpIcon.setMinValue(0);
                        dpIcon.setMaxValue(Icon.values().length);

                    }
                    addDatapoint(device, channel.getNumber(), DATAPOINT_NAME_DISPLAY_SUBMIT, HmValueType.BOOL, false,
                            false);
                }
            }
        }
    }

    /**
     * Returns a string array with all the constants in the Enum.
     */
    private String[] getEnumNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return dp.getChannel().getDevice().getType().equals(DEVICE_TYPE_STATUS_DISPLAY)
                && (getName().equals(dp.getName()) || dp.getName().startsWith(DATAPOINT_NAME_DISPLAY_LINE)
                        || dp.getName().startsWith(DATAPOINT_NAME_DISPLAY_COLOR)
                        || dp.getName().startsWith(DATAPOINT_NAME_DISPLAY_ICON));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);

        if (DATAPOINT_NAME_DISPLAY_SUBMIT.equals(dp.getName()) && MiscUtils.isTrueValue(dp.getValue())) {
            HmChannel channel = dp.getChannel();

            List<String> message = new ArrayList<String>();
            message.add(START);
            message.add(LF);

            for (int i = 1; i <= DISPLAY_LINES; i++) {
                String line = ObjectUtils.toString(
                        channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_DISPLAY_LINE + i).getValue());
                if (StringUtils.isEmpty(line)) {
                    line = " ";
                }
                message.add(LINE);
                message.add(encodeText(line));
                String color = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_DISPLAY_COLOR + i)
                        .getOptionValue();
                String icon = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_DISPLAY_ICON + i)
                        .getOptionValue();

                message.add(COLOR);
                String colorCode = Color.getCode(color);
                message.add(StringUtils.isBlank(colorCode) ? Color.WHITE.getCode() : colorCode);

                String iconCode = Icon.getCode(icon);
                if (StringUtils.isNotBlank(iconCode)) {
                    message.add(ICON);
                    message.add(iconCode);
                }
                message.add(LF);
            }
            message.add(STOP);

            gateway.sendDatapoint(channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_SUBMIT),
                    new HmDatapointConfig(), StringUtils.join(message, ","));
        }
    }

    /**
     * Encodes the given text for the display.
     */
    private String encodeText(String text) {
        final byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            sb.append("0x");
            String hexValue = String.format("%02x", b);
            sb.append(replaceMap.containsKey(hexValue) ? replaceMap.get(hexValue) : hexValue);
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}

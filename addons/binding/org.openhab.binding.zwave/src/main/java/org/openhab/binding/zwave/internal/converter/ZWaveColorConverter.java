/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveColorConverter class. Converter for communication with the {@link ZWaveColorCommandClass}. Implements
 * polling of the status and receiving of events.
 *
 * @author Chris Jackson
 */
public class ZWaveColorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveColorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveColorConverter} class.
     *
     */
    public ZWaveColorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveColorCommandClass commandClass = (ZWaveColorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COLOR, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());

        // Add a poll to update the color
        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        Collection<SerialMessage> rawMessages = commandClass.getColor();
        for (SerialMessage msg : rawMessages) {
            messages.add(node.encapsulate(msg, commandClass, channel.getEndpoint()));
        }
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        ZWaveColorValueEvent colorEvent = null;
        if (!(event instanceof ZWaveColorValueEvent)) {
            return null;
        }

        colorEvent = (ZWaveColorValueEvent) event;

        Map<ZWaveColorType, Integer> colorMap = colorEvent.getColorMap();
        State state;
        switch (channel.getDataType()) {
            case HSBType:
                float[] hsb = new float[3];
                RGBtoHSB(colorMap.get(ZWaveColorType.RED), colorMap.get(ZWaveColorType.GREEN),
                        colorMap.get(ZWaveColorType.BLUE), hsb);
                HSBType hsbState = new HSBType(new DecimalType(hsb[0]), new PercentType((int) hsb[1]),
                        new PercentType((int) hsb[2]));

                if ("RGB".equals(channel.getArguments().get("colorMode"))) {
                    state = new HSBType(hsbState.getHue(), PercentType.HUNDRED, PercentType.HUNDRED);
                } else {
                    state = hsbState;
                }
                break;
            case PercentType:
                if ("COLD_WHITE".equals(channel.getArguments().get("colorMode"))) {
                    state = new PercentType(colorMap.get(ZWaveColorType.COLD_WHITE));
                } else if ("WARM_WHITE".equals(channel.getArguments().get("colorMode"))) {
                    state = new PercentType(colorMap.get(ZWaveColorType.WARM_WHITE));
                } else {
                    state = new PercentType(0);
                }
                break;
            default:
                state = null;
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveColorCommandClass commandClass = (ZWaveColorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COLOR, channel.getEndpoint());

        Collection<SerialMessage> rawMessages = null;

        // Since we get an HSB, there is brightness information. However, we only deal with the color class here
        // so we need to scale the color and let the brightness be handled by the multi_level command class
        double scaling;
        if ("RGB".equals(channel.getArguments().get("colorMode"))) {
            // Command must be color - convert to zwave format
            HSBType color = (HSBType) command;
            logger.debug("NODE {}: Converted command '{}' to value {} {} {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), color.getRed().intValue(), color.getGreen().intValue(),
                    color.getBlue().intValue(), channel.getUID(), channel.getEndpoint());
            scaling = 100 / color.getBrightness().doubleValue() * 255 / 100;

            // Queue the command
            rawMessages = commandClass.setColor((int) (color.getRed().doubleValue() * scaling),
                    (int) (color.getGreen().doubleValue() * scaling), (int) (color.getBlue().doubleValue() * scaling),
                    0, 0);
        } else if ("COLD_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;
            logger.debug("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), color.intValue(), channel.getUID(), channel.getEndpoint());

            // Queue the command
            rawMessages = commandClass.setColor(0, 0, 0, color.intValue(), 0);
        } else if ("WARM_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;
            logger.debug("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), color.intValue(), channel.getUID(), channel.getEndpoint());

            // Queue the command
            rawMessages = commandClass.setColor(0, 0, 0, 0, color.intValue());
        } else if ("DIFF_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;
            logger.debug("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), color.intValue(), channel.getUID(), channel.getEndpoint());

            // Queue the command
            rawMessages = commandClass.setColor(0, 0, 0, 100 - color.intValue(), color.intValue());
        }

        if (rawMessages == null) {
            return null;
        }

        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        for (SerialMessage msg : rawMessages) {
            messages.add(node.encapsulate(msg, commandClass, channel.getEndpoint()));
        }

        // Add a poll to update the color
        rawMessages = commandClass.getColor();
        for (SerialMessage msg : rawMessages) {
            messages.add(node.encapsulate(msg, commandClass, channel.getEndpoint()));
        }
        return messages;
    }

    public float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        int max = (r > g) ? r : g;
        if (b > max) {
            max = b;
        }
        int min = (r < g) ? r : g;
        if (b < min) {
            min = b;
        }
        brightness = max / 2.55f;
        saturation = (max != 0 ? ((float) (max - min)) / ((float) max) : 0) * 100;
        if (saturation == 0) {
            hue = 0;
        } else {
            float red = ((float) (max - r)) / ((float) (max - min));
            float green = ((float) (max - g)) / ((float) (max - min));
            float blue = ((float) (max - b)) / ((float) (max - min));
            if (r == max) {
                hue = blue - green;
            } else if (g == max) {
                hue = 2.0f + red - blue;
            } else {
                hue = 4.0f + green - red;
            }
            hue = hue / 6.0f * 360;
            if (hue < 0) {
                hue = hue + 360.0f;
            }
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
}

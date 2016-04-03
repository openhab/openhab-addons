/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.handler.cluster;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.api.ZigBeeApiConstants;
import org.bubblecloud.zigbee.api.ZigBeeDeviceException;
import org.bubblecloud.zigbee.api.cluster.general.ColorControl;
import org.bubblecloud.zigbee.api.cluster.general.LevelControl;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Attribute;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ReportListener;
import org.bubblecloud.zigbee.api.cluster.impl.attribute.Attributes;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZigBeeColorClusterHandler extends ZigBeeClusterHandler implements ReportListener {
    private Logger logger = LoggerFactory.getLogger(ZigBeeClusterHandler.class);

    private HSBType currentHSB = new HSBType(new DecimalType(0), new PercentType(0), PercentType.HUNDRED);
    private Attribute attrLevel;
    private Attribute attrHue;
    private Attribute attrSaturation;
    private ColorControl clusColor;
    private LevelControl clusLevel;

    private boolean initialised = false;

    @Override
    public int getClusterId() {
        return ZigBeeApiConstants.CLUSTER_ID_COLOR_CONTROL;
    }

    @Override
    public void initializeConverter() {
        if (initialised == true) {
            return;
        }

        attrLevel = coordinator.openAttribute(address, LevelControl.class, Attributes.CURRENT_LEVEL, this);
        clusLevel = coordinator.openCluster(address, LevelControl.class);
        if (attrLevel == null || clusLevel == null) {
            logger.error("Error opening device level controls {}", address);
            return;
        }

        attrHue = coordinator.openAttribute(address, ColorControl.class, Attributes.CURRENT_HUE, null);
        attrSaturation = coordinator.openAttribute(address, ColorControl.class, Attributes.CURRENT_SATURATION, null);
        clusColor = coordinator.openCluster(address, ColorControl.class);
        if (attrHue == null || attrSaturation == null || clusColor == null) {
            logger.error("Error opening device color controls {}", address);
            return;
        }

        /*
         * attrColorTemp = coordinator.openAttribute(channel.getAddress(), ColorControl.class,
         * Attributes.COLOR_TEMPERATURE, null);
         * clusColor = coordinator.openCluster(channel.getAddress(), ColorControl.class);
         * if (attrColorTemp == null || clusColor == null) {
         * logger.error("Error opening device color controls {}", channel.getAddress());
         * return;
         * }
         */
        initialised = true;
    }

    @Override
    public void disposeConverter() {

    }

    @Override
    public void handleRefresh() {

    }

    @Override
    public void handleCommand(Command command) {
        if (initialised == false) {
            return;
        }

        if (command instanceof HSBType) {
            currentHSB = new HSBType(command.toString());
        } else if (command instanceof PercentType) {
            currentHSB = new HSBType(currentHSB.getHue(), (PercentType) command, PercentType.HUNDRED);
        } else if (command instanceof OnOffType) {
            PercentType saturation;
            if ((OnOffType) command == OnOffType.ON) {
                saturation = PercentType.HUNDRED;
            } else {
                saturation = PercentType.ZERO;
            }
            currentHSB = new HSBType(currentHSB.getHue(), saturation, PercentType.HUNDRED);
        }

        try {
            int hue = currentHSB.getHue().intValue();
            int saturation = currentHSB.getSaturation().intValue();
            clusColor.moveToHue((int) (hue * 254.0 / 360.0 + 0.5), 0, 10);
            clusColor.movetoSaturation((int) (saturation * 254.0 / 100.0 + 0.5), 10);
            clusLevel.moveToLevelWithOnOff((short) (currentHSB.getBrightness().intValue() * 254.0 / 100.0 + 0.5), 10);
        } catch (ZigBeeDeviceException e) {
            e.printStackTrace();
        }

        /*
         * // Color Temperature
         * PercentType colorTemp = PercentType.ZERO;
         * if (command instanceof PercentType) {
         * colorTemp = (PercentType) command;
         * } else if (command instanceof OnOffType) {
         * if ((OnOffType) command == OnOffType.ON) {
         * colorTemp = PercentType.HUNDRED;
         * } else {
         * colorTemp = PercentType.ZERO;
         * }
         * }
         *
         * // Range of 2000K to 6500K, gain = 4500K, offset = 2000K
         * double kelvin = colorTemp.intValue() * 4500.0 / 100.0 + 2000.0;
         * try {
         * clusColor.moveToColorTemperature((short) (1e6 / kelvin + 0.5), 10);
         * } catch (ZigBeeDeviceException e) {
         * e.printStackTrace();
         * }
         */
    }

    @Override
    public void receivedReport(String endPointId, short clusterId, Dictionary<Attribute, Object> reports) {
        logger.debug("ZigBee attribute reports {} from {}", reports, endPointId);
        if (attrLevel != null) {
            Object value = reports.get(attrLevel);
            if (value != null) {
                // updateChannelState((int) value);
            }
        }
    }

    @Override
    public List<Channel> getChannels(ThingUID thingUID, Device device) {
        List<Channel> channels = new ArrayList<Channel>();

        channels.add(createChannel(device, thingUID, ZigBeeBindingConstants.CHANNEL_COLOR_COLOR, "Color", "Color"));
        // channels.add(createChannel(device, thingUID, ZigBeeBindingConstants.CHANNEL_COLOR_TEMPERATURE, "Dimmer",
        // "Color Temperature"));

        return channels;
    }

}

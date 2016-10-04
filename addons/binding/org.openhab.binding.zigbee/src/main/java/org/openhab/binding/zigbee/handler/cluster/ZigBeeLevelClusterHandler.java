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
import org.bubblecloud.zigbee.api.cluster.general.LevelControl;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Attribute;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ReportListener;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Reporter;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ZigBeeClusterException;
import org.bubblecloud.zigbee.api.cluster.impl.attribute.Attributes;
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
public class ZigBeeLevelClusterHandler extends ZigBeeClusterHandler implements ReportListener {
    private Logger logger = LoggerFactory.getLogger(ZigBeeClusterHandler.class);

    private Attribute attrLevel;
    private LevelControl clusLevel;

    private boolean initialised = false;

    @Override
    public int getClusterId() {
        return ZigBeeApiConstants.CLUSTER_ID_LEVEL_CONTROL;
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

        try {
            Integer value = (Integer) attrLevel.getValue();
            if (value != null) {
                value = value * 100 / 255;
                if (value > 100) {
                    value = 100;
                }
                updateChannelState(new PercentType(value));
            }
        } catch (ZigBeeClusterException e) {
            // e.printStackTrace();
        }

        final Device device = coordinator.getDevice(address);
        final Reporter reporter = device.getCluster(getClusterId()).getAttribute(0).getReporter();
        if (reporter != null) {
            logger.debug("{} Reporting configured on cluster {}", address, getClusterId());
            reporter.addReportListener(this, false);
        }
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

        int level = 0;
        if (command instanceof PercentType) {
            level = ((PercentType) command).intValue();
        } else if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                level = 100;
            } else {
                level = 0;
            }
        }

        try {
            clusLevel.moveToLevelWithOnOff((short) (level * 254.0 / 100.0 + 0.5), 10);
        } catch (ZigBeeDeviceException e) {
            e.printStackTrace();
        }
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

        channels.add(createChannel(device, thingUID, ZigBeeBindingConstants.CHANNEL_SWITCH_DIMMER, "Dimmer", "Dimmer"));

        return channels;
    }

}

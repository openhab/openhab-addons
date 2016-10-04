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
import org.bubblecloud.zigbee.api.cluster.general.OnOff;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Attribute;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ReportListener;
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
public class ZigBeeOnOffClusterHandler extends ZigBeeClusterHandler implements ReportListener {
    private Logger logger = LoggerFactory.getLogger(ZigBeeClusterHandler.class);

    private OnOffType currentOnOff = OnOffType.OFF;
    private Attribute attrOnOff;
    private OnOff clusOnOff;

    private boolean initialised = false;

    @Override
    public int getClusterId() {
        return ZigBeeApiConstants.CLUSTER_ID_ON_OFF;
    }

    @Override
    public void initializeConverter() {
        if (initialised == true) {
            return;
        }

        attrOnOff = coordinator.openAttribute(address, OnOff.class, Attributes.ON_OFF, this);
        clusOnOff = coordinator.openCluster(address, OnOff.class);
        if (attrOnOff == null || clusOnOff == null) {
            logger.error("Error opening device on/off controls {}", address);
            return;
        }

        try {
            Object value = attrOnOff.getValue();
            if (value != null && (boolean) value == true) {
                updateChannelState(OnOffType.ON);
            } else {
                updateChannelState(OnOffType.OFF);
            }
        } catch (ZigBeeClusterException e) {
            // e.printStackTrace();
        }

        initialised = true;
    }

    @Override
    public void disposeConverter() {
        if (initialised == false) {
            return;
        }

        if (attrOnOff != null) {
            coordinator.closeAttribute(attrOnOff, this);
        }
        if (clusOnOff != null) {
            coordinator.closeCluster(clusOnOff);
        }
    }

    @Override
    public void handleRefresh() {
        if (initialised == false) {
            return;
        }

        try {
            Object value = attrOnOff.getValue();
            if (value != null && (boolean) value == true) {
                updateChannelState(OnOffType.ON);
            } else {
                updateChannelState(OnOffType.OFF);
            }
        } catch (ZigBeeClusterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (initialised == false) {
            return;
        }

        if (command instanceof PercentType) {
            if (((PercentType) command).intValue() == 0) {
                currentOnOff = OnOffType.OFF;
            } else {
                currentOnOff = OnOffType.ON;
            }
        } else if (command instanceof OnOffType) {
            currentOnOff = (OnOffType) command;
        }

        if (clusOnOff == null) {
            return;
        }
        try {
            if (currentOnOff == OnOffType.ON) {
                clusOnOff.on();
            } else {
                clusOnOff.off();
            }
        } catch (ZigBeeDeviceException e) {
            e.printStackTrace();
        }

        // this.thing.updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void receivedReport(String endPointId, short clusterId, Dictionary<Attribute, Object> reports) {
        logger.debug("ZigBee attribute reports {} from {}", reports, endPointId);
        if (attrOnOff != null) {
            Object value = reports.get(attrOnOff);
            if (value != null && (boolean) value == true) {
                updateChannelState(OnOffType.ON);
            } else {
                updateChannelState(OnOffType.OFF);
            }
        }
    }

    @Override
    public List<Channel> getChannels(ThingUID thingUID, Device device) {
        List<Channel> channels = new ArrayList<Channel>();

        channels.add(createChannel(device, thingUID, ZigBeeBindingConstants.CHANNEL_SWITCH_ONOFF, "Switch", "Switch"));

        return channels;
    }

}

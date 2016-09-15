/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.handler;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.PRESS;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazondashbutton.internal.ArpRequestTracker;
import org.openhab.binding.amazondashbutton.internal.ArpRequestTracker.ArpRequestHandler;
import org.openhab.binding.amazondashbutton.internal.PcapUtil;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfig;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.ArpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonDashButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class AmazonDashButtonHandler extends BaseThingHandler {

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(AmazonDashButtonHandler.class);

    private ArpRequestTracker arpRequestListener;

    private long lastCommandHandled = 0;

    public AmazonDashButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        AmazonDashButtonConfig dashButtonConfig = getConfigAs(AmazonDashButtonConfig.class);
        String pcapNetworkInterfaceName = dashButtonConfig.pcapNetworkInterfaceName;
        String macAddress = dashButtonConfig.macAddress;
        final Integer packetInterval = dashButtonConfig.packetInterval;
        PcapNetworkInterface pcapNetworkInterface = PcapUtil.getNetworkInterfaceByName(pcapNetworkInterfaceName);
        if (pcapNetworkInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "The networkinterface " + pcapNetworkInterfaceName + " is not present.");
            return;
        }

        ArpRequestTracker arpRequestListener = new ArpRequestTracker(pcapNetworkInterface);
        arpRequestListener.startCapturing(new ArpRequestHandler() {

            @Override
            public void handleArpRequest(ArpPacket arpPacket) {
                long now = System.currentTimeMillis();
                if (lastCommandHandled + packetInterval < now) {
                    postCommand(PRESS, OnOffType.ON);
                    lastCommandHandled = now;
                }
            }
        }, macAddress);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (arpRequestListener != null) {
            arpRequestListener.stopCapturing();
        }
    }
}

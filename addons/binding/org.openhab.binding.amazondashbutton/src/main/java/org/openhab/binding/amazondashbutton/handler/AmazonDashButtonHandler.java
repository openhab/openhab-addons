/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.handler;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.PRESS;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazondashbutton.internal.arp.ArpRequestHandler;
import org.openhab.binding.amazondashbutton.internal.arp.ArpRequestTracker;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfig;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceListener;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceService;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapUtil;
import org.pcap4j.packet.ArpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonDashButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class AmazonDashButtonHandler extends BaseThingHandler implements PcapNetworkInterfaceListener {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(AmazonDashButtonHandler.class);

    private ArpRequestTracker arpRequestListener;

    private long lastCommandHandled = 0;

    public AmazonDashButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There are no commands to be handled
    }

    @Override
    public void initialize() {
        PcapNetworkInterfaceService.instance().registerListener(this);
        AmazonDashButtonConfig dashButtonConfig = getConfigAs(AmazonDashButtonConfig.class);
        String pcapNetworkInterfaceName = dashButtonConfig.pcapNetworkInterfaceName;
        String macAddress = dashButtonConfig.macAddress;
        final Integer packetInterval = dashButtonConfig.packetInterval;
        PcapNetworkInterfaceWrapper pcapNetworkInterface = PcapUtil.getNetworkInterfaceByName(pcapNetworkInterfaceName);
        if (pcapNetworkInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "The networkinterface " + pcapNetworkInterfaceName + " is not present.");
            return;
        }

        arpRequestListener = new ArpRequestTracker(pcapNetworkInterface);
        boolean capturingStarted = arpRequestListener.startCapturing(new ArpRequestHandler() {

            @Override
            public void handleArpRequest(ArpPacket arpPacket) {
                long now = System.currentTimeMillis();
                if (lastCommandHandled + packetInterval < now) {
                    ChannelUID pressChannel = new ChannelUID(getThing().getUID(), PRESS);
                    triggerChannel(pressChannel);
                    lastCommandHandled = now;
                }
            }
        }, macAddress);
        if (capturingStarted) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "The capturing for " + pcapNetworkInterfaceName + " cannot be started.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (arpRequestListener != null) {
            arpRequestListener.stopCapturing();
            arpRequestListener = null;
        }
        PcapNetworkInterfaceService.instance().unregisterListener(this);
    }

    @Override
    public void onPcapNetworkInterfaceAdded(PcapNetworkInterfaceWrapper newNetworkInterface) {
        if (arpRequestListener != null) {
            final PcapNetworkInterfaceWrapper trackedPcapNetworkInterface = arpRequestListener
                    .getPcapNetworkInterface();
            if (trackedPcapNetworkInterface.equals(newNetworkInterface)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void onPcapNetworkInterfaceRemoved(PcapNetworkInterfaceWrapper removedNetworkInterface) {
        if (arpRequestListener != null) {
            final PcapNetworkInterfaceWrapper trackedPcapNetworkInterface = arpRequestListener
                    .getPcapNetworkInterface();
            if (trackedPcapNetworkInterface.equals(removedNetworkInterface)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "The networkinterface " + removedNetworkInterface.getName() + " is not present anymore.");
            }
        }
    }
}

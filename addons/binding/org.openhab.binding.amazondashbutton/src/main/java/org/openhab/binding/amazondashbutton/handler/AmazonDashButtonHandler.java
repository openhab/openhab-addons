/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.openhab.binding.amazondashbutton.internal.capturing.PacketCapturingHandler;
import org.openhab.binding.amazondashbutton.internal.capturing.PacketCapturingService;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfig;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceListener;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceService;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapUtil;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonDashButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class AmazonDashButtonHandler extends BaseThingHandler implements PcapNetworkInterfaceListener {
    private PacketCapturingService packetCapturingService;

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
        final String pcapNetworkInterfaceName = dashButtonConfig.pcapNetworkInterfaceName;
        final String macAddress = dashButtonConfig.macAddress;
        final Integer packetInterval = dashButtonConfig.packetInterval;
        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                PcapNetworkInterfaceWrapper pcapNetworkInterface = PcapUtil
                        .getNetworkInterfaceByName(pcapNetworkInterfaceName);
                if (pcapNetworkInterface == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "The networkinterface " + pcapNetworkInterfaceName + " is not present.");
                    return;
                }

                packetCapturingService = new PacketCapturingService(pcapNetworkInterface);
                boolean capturingStarted = packetCapturingService.startCapturing(new PacketCapturingHandler() {

                    @Override
                    public void packetCaptured(MacAddress macAddress) {
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
        });

    }

    @Override
    public void dispose() {
        super.dispose();
        if (packetCapturingService != null) {
            packetCapturingService.stopCapturing();
            packetCapturingService = null;
        }
        PcapNetworkInterfaceService.instance().unregisterListener(this);
    }

    @Override
    public void onPcapNetworkInterfaceAdded(PcapNetworkInterfaceWrapper newNetworkInterface) {
        if (packetCapturingService != null) {
            final PcapNetworkInterfaceWrapper trackedPcapNetworkInterface = packetCapturingService
                    .getPcapNetworkInterface();
            if (trackedPcapNetworkInterface.equals(newNetworkInterface)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void onPcapNetworkInterfaceRemoved(PcapNetworkInterfaceWrapper removedNetworkInterface) {
        if (packetCapturingService != null) {
            final PcapNetworkInterfaceWrapper trackedPcapNetworkInterface = packetCapturingService
                    .getPcapNetworkInterface();
            if (trackedPcapNetworkInterface.equals(removedNetworkInterface)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "The networkinterface " + removedNetworkInterface.getName() + " is not present anymore.");
            }
        }
    }
}

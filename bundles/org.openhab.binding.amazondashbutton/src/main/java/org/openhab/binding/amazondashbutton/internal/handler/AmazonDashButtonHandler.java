/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.amazondashbutton.internal.handler;

import static org.openhab.binding.amazondashbutton.internal.AmazonDashButtonBindingConstants.PRESS;
import static org.openhab.core.thing.CommonTriggerEvents.PRESSED;

import org.openhab.binding.amazondashbutton.internal.capturing.PacketCapturingHandler;
import org.openhab.binding.amazondashbutton.internal.capturing.PacketCapturingService;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfig;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceListener;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceService;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.pcap4j.util.MacAddress;

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
        scheduler.submit(() -> {
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
                        triggerChannel(PRESS, PRESSED);
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

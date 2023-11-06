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
package org.openhab.binding.elerotransmitterstick.internal.handler;

import org.openhab.binding.elerotransmitterstick.internal.config.EleroTransmitterStickConfig;
import org.openhab.binding.elerotransmitterstick.internal.stick.TransmitterStick;
import org.openhab.binding.elerotransmitterstick.internal.stick.TransmitterStick.StickListener;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link EleroTransmitterStickHandler} is responsible for managing the connection to an elero transmitter stick.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroTransmitterStickHandler extends BaseBridgeHandler implements BridgeHandler {
    private final SerialPortManager serialPortManager;
    private final TransmitterStick stick;

    public EleroTransmitterStickHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;

        stick = new TransmitterStick(new StickListener() {
            @Override
            public void connectionEstablished() {
                updateStatus(ThingStatus.ONLINE);
            }

            @Override
            public void connectionDropped(Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public void dispose() {
        stick.dispose();

        super.dispose();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        stick.initialize(getConfig().as(EleroTransmitterStickConfig.class), scheduler, serialPortManager);
    }

    public TransmitterStick getStick() {
        return stick;
    }

    public void addStatusListener(int channelId, StatusListener listener) {
        stick.addStatusListener(channelId, listener);
    }

    public void removeStatusListener(int channelId, StatusListener listener) {
        stick.removeStatusListener(channelId, listener);
    }
}

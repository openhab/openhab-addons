/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.handler;

import static org.openhab.binding.cul.internal.CULBindingConstants.CHANNEL_CREDITS;
import static org.openhab.binding.cul.internal.CULBindingConstants.CHANNEL_LED;

import java.util.stream.Stream;

import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.CULHandler;
import org.openhab.binding.cul.CULListener;
import org.openhab.binding.cul.internal.*;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public abstract class CulCunBaseBridgeHandler extends BaseBridgeHandler
        implements CULHandler, CULListener, CULCreditListener {

    private final Logger logger = LoggerFactory.getLogger(CulCunBaseBridgeHandler.class);

    protected CULConfig culConfig;
    private CULManager manager;
    private CULHandlerInternal<?> cul;

    public CulCunBaseBridgeHandler(Bridge thing, CULManager manager) {
        super(thing);
        this.manager = manager;
    }

    protected synchronized void connect() {
        try {
            cul = manager.getOpenCULHandler(culConfig);
            cul.registerListener(this);
            cul.registerCreditListener(this);
            updateStatus(ThingStatus.ONLINE);
        } catch (CULDeviceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("Can't open CUL", e);
        }
    }

    @Override
    public void dispose() {
        if (cul != null) {
            cul.unregisterCreditListener(this);
            cul.unregisterListener(this);
            manager.close(cul);
            cul = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_CREDITS:
                if (command instanceof RefreshType) {
                    int credit10ms = getCulHandler().getCredit10ms();
                    creditChanged(credit10ms);
                    break;
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getThing().getLabel(), channelUID,
                            command.getClass());
                }
                break;
            case CHANNEL_LED:
                if (command instanceof OnOffType) {
                    setLedMode((command == OnOffType.ON));
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, OnOffType.ON);
                } else {
                    logger.debug("{}: Unhandled command type: {}: {}", getThing().getLabel(), channelUID,
                            command.getClass());
                }
                break;
        }
    }

    public void setLedMode(boolean ledModeOn) {
        String data = "";
        if (ledModeOn) {
            data = "l02";
        } else {
            data = "l00";
        }
        try {
            getCulHandler().send(data);
        } catch (CULCommunicationException e) {
            logger.error("Unable to send CUL message {} because: {}", data, e.getMessage());
        }
    }

    private CULHandler getCulHandler() {
        if (cul == null) {
            logger.warn("Cul is not open yet");
        }
        return cul;
    }

    private Stream<CULListener> getCulListenerChildren() {
        return getThing().getThings().stream().map(Thing::getHandler).filter(CULListener.class::isInstance)
                .map(CULListener.class::cast);
    }

    @Override
    public void send(String command) throws CULCommunicationException {
        CULHandler culHandler = getCulHandler();
        if (culHandler != null) {
            culHandler.send(command);
        } else {
            logger.warn("Could not send command {}", command);
        }
    }

    @Override
    public int getCredit10ms() {
        CULHandler culHandler = getCulHandler();
        if (culHandler != null) {
            return culHandler.getCredit10ms();
        } else {
            logger.warn("Could not getCredit10ms");
            return 0;
        }
    }

    @Override
    public void dataReceived(String data) {
        getCulListenerChildren().forEach(culListener -> culListener.dataReceived(data));
    }

    @Override
    public void error(Exception e) {
        getCulListenerChildren().forEach(culListener -> culListener.error(e));
    }

    @Override
    public void creditChanged(int credit10ms) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_CREDITS), new DecimalType(credit10ms));
    }
}

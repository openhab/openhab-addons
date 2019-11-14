/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.handler;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.reader.common.FrameBaseOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameEjpOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameHcOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameTempoOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoAbstractElectricityMeterHandler} class defines a skeleton for Electricity Meters handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class TeleinfoAbstractElectricityMeterHandler extends BaseThingHandler
        implements TeleinfoControllerHandlerListener {
    private final Logger logger = LoggerFactory.getLogger(TeleinfoAbstractElectricityMeterHandler.class);

    public TeleinfoAbstractElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Electricity Meter thing handler {}.", getThing().getUID());

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);

        Bridge bridge = getBridge();
        logger.debug("bridge = {}", bridge);
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Controller status changed to {}.", bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);
            logger.debug("Controller is not online ({})", bridgeStatusInfo.getStatus());
            return;
        }

        logger.debug("Controller is ONLINE. Starting Electricity Meter thing initialization");
        updateStatus(ThingStatus.UNKNOWN);

        TeleinfoAbstractControllerHandler controllerHandler = (TeleinfoAbstractControllerHandler) getBridge()
                .getHandler();
        controllerHandler.addListener(this);
        logger.debug("Electricity Meter initialization complete.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported
    }

    protected void updateStatesForBaseFrameOption(@NonNull FrameBaseOption frameBaseOption) {
        updateState(CHANNEL_BASE_FRAME_BASE, new DecimalType(frameBaseOption.getBase()));
    }

    protected void updateStatesForHcFrameOption(@NonNull FrameHcOption frameHcOption) {
        updateState(CHANNEL_HC_FRAME_HCHC, new DecimalType(frameHcOption.getHchc()));
        updateState(CHANNEL_HC_FRAME_HCHP, new DecimalType(frameHcOption.getHchp()));
        updateState(CHANNEL_HC_FRAME_HHPHC, new StringType(frameHcOption.getHhphc().name()));
    }

    protected void updateStatesForTempoFrameOption(@NonNull FrameTempoOption frameTempoOption) {
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJR, new DecimalType(frameTempoOption.getBbrhpjr()));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJR, new DecimalType(frameTempoOption.getBbrhcjr()));
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJW, new DecimalType(frameTempoOption.getBbrhpjw()));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJW, new DecimalType(frameTempoOption.getBbrhcjw()));
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJB, new DecimalType(frameTempoOption.getBbrhpjb()));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJB, new DecimalType(frameTempoOption.getBbrhcjb()));
        updateState(CHANNEL_TEMPO_FRAME_HHPHC, new StringType(frameTempoOption.getHhphc().name()));
        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_1,
                new StringType(frameTempoOption.getProgrammeCircuit1().name()));
        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_2,
                new StringType(frameTempoOption.getProgrammeCircuit2().name()));

        if (frameTempoOption.getDemain() == null) {
            updateState(CHANNEL_TEMPO_FRAME_DEMAIN, UnDefType.NULL);
        } else {
            updateState(CHANNEL_TEMPO_FRAME_DEMAIN, new StringType(frameTempoOption.getDemain().name()));
        }
    }

    protected void updateStatesForEjpFrameOption(@NonNull FrameEjpOption frameEjpOption) {
        updateState(CHANNEL_EJP_FRAME_EJPHN, new DecimalType(frameEjpOption.getEjphn()));
        updateState(CHANNEL_EJP_FRAME_EJPHPM, new DecimalType(frameEjpOption.getEjphpm()));

        if (frameEjpOption.getPejp() == null) {
            updateState(CHANNEL_EJP_FRAME_PEJP, UnDefType.NULL);
        } else {
            updateState(CHANNEL_EJP_FRAME_PEJP, new DecimalType(frameEjpOption.getPejp()));
        }
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);

        if (ThingStatus.ONLINE.equals(status) == false) {
            for (Channel channel : getThing().getChannels()) {
                if (CHANNEL_LAST_UPDATE.equals(channel.getUID().getId()) == false) {
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            }
        }
    }
}

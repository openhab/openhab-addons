/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.dto.common.FrameBaseOption;
import org.openhab.binding.teleinfo.internal.dto.common.FrameEjpOption;
import org.openhab.binding.teleinfo.internal.dto.common.FrameHcOption;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoAbstractElectricityMeterHandler} class defines a skeleton for Electricity Meters handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoAbstractElectricityMeterHandler extends BaseThingHandler
        implements TeleinfoControllerHandlerListener {
    private final Logger logger = LoggerFactory.getLogger(TeleinfoAbstractElectricityMeterHandler.class);
    protected TeleinfoElectricityMeterConfiguration configuration = new TeleinfoElectricityMeterConfiguration();

    public TeleinfoAbstractElectricityMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);

        Bridge bridge = getBridge();
        logger.debug("bridge = {}", bridge);
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
        configuration = getConfigAs(TeleinfoElectricityMeterConfiguration.class);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);
            return;
        }

        Bridge bridge = getBridge();
        if (bridge != null) {
            TeleinfoAbstractControllerHandler controllerHandler = (TeleinfoAbstractControllerHandler) bridge
                    .getHandler();
            if (controllerHandler != null) {
                controllerHandler.addListener(this);
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported
    }

    protected void updateStatesForBaseFrameOption(FrameBaseOption frameBaseOption) {
        updateState(CHANNEL_BASE_FRAME_BASE, QuantityType.valueOf(frameBaseOption.getBase(), Units.WATT_HOUR));
    }

    protected void updateStatesForHcFrameOption(FrameHcOption frameHcOption) {
        updateState(CHANNEL_HC_FRAME_HCHC, QuantityType.valueOf(frameHcOption.getHchc(), Units.WATT_HOUR));
        updateState(CHANNEL_HC_FRAME_HCHP, QuantityType.valueOf(frameHcOption.getHchp(), Units.WATT_HOUR));
        updateState(CHANNEL_HC_FRAME_HHPHC, new StringType(frameHcOption.getHhphc().name()));
    }

    protected void updateStatesForTempoFrameOption(FrameTempoOption frameTempoOption) {
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJR, QuantityType.valueOf(frameTempoOption.getBbrhpjr(), Units.WATT_HOUR));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJR, QuantityType.valueOf(frameTempoOption.getBbrhcjr(), Units.WATT_HOUR));
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJW, QuantityType.valueOf(frameTempoOption.getBbrhpjw(), Units.WATT_HOUR));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJW, QuantityType.valueOf(frameTempoOption.getBbrhcjw(), Units.WATT_HOUR));
        updateState(CHANNEL_TEMPO_FRAME_BBRHPJB, QuantityType.valueOf(frameTempoOption.getBbrhpjb(), Units.WATT_HOUR));
        updateState(CHANNEL_TEMPO_FRAME_BBRHCJB, QuantityType.valueOf(frameTempoOption.getBbrhcjb(), Units.WATT_HOUR));
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

    protected void updateStatesForEjpFrameOption(FrameEjpOption frameEjpOption) {
        updateState(CHANNEL_EJP_FRAME_EJPHN, QuantityType.valueOf(frameEjpOption.getEjphn(), Units.WATT_HOUR));
        updateState(CHANNEL_EJP_FRAME_EJPHPM, QuantityType.valueOf(frameEjpOption.getEjphpm(), Units.WATT_HOUR));

        if (frameEjpOption.getPejp() == null) {
            updateState(CHANNEL_EJP_FRAME_PEJP, UnDefType.NULL);
        } else {
            updateState(CHANNEL_EJP_FRAME_PEJP, QuantityType.valueOf(frameEjpOption.getPejp(), Units.MINUTE));
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);

        if (!(ThingStatus.ONLINE.equals(status))) {
            for (Channel channel : getThing().getChannels()) {
                if (!CHANNEL_LAST_UPDATE.equals(channel.getUID().getId())) {
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            }
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        this.updateStatus(status, statusDetail, null);
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        this.updateStatus(status, ThingStatusDetail.NONE, null);
    }
}

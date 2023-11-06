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
package org.openhab.binding.teleinfo.internal.handler;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.data.Phase;
import org.openhab.binding.teleinfo.internal.data.Pricing;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.FrameUtil;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.ValueType;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoTicMode;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * The {@link TeleinfoElectricityMeterHandler} class defines a skeleton for Electricity Meters handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoElectricityMeterHandler extends BaseThingHandler implements TeleinfoControllerHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoElectricityMeterHandler.class);
    protected TeleinfoElectricityMeterConfiguration configuration = new TeleinfoElectricityMeterConfiguration();
    private boolean wasLastFrameShort = false;

    public TeleinfoElectricityMeterHandler(Thing thing) {
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
        TeleinfoAbstractControllerHandler controllerHandler = getControllerHandler();
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            if (controllerHandler != null) {
                controllerHandler.removeListener(this);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ERROR_OFFLINE_CONTROLLER_OFFLINE);
            return;
        }

        if (controllerHandler != null) {
            controllerHandler.addListener(this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        TeleinfoAbstractControllerHandler controllerHandler = getControllerHandler();
        if (controllerHandler != null) {
            controllerHandler.removeListener(this);
        }
        super.dispose();
    }

    private @Nullable TeleinfoAbstractControllerHandler getControllerHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (TeleinfoAbstractControllerHandler) bridge.getHandler() : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported
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

    @Override
    public void onFrameReceived(Frame frame) {
        String adco = configuration.getAdco();
        if (adco.equalsIgnoreCase(frame.get(Label.ADCO)) || adco.equalsIgnoreCase(frame.get(Label.ADSC))) {
            updateStatesForChannels(frame);
        }
    }

    private void updateStatesForChannels(Frame frame) {
        for (Entry<Label, String> entry : frame.getLabelToValues().entrySet()) {
            Label label = entry.getKey();
            if (!label.getChannelName().equals(NOT_A_CHANNEL)) {
                logger.trace("Update channel {} to value {}", label.getChannelName(), entry.getValue());
                if (label == Label.PTEC) {
                    updateState(label.getChannelName(), StringType.valueOf(entry.getValue().replace(".", "")));
                } else if (label.getType() == ValueType.STRING) {
                    updateState(label.getChannelName(), StringType.valueOf(entry.getValue()));
                } else if (label.getType() == ValueType.INTEGER) {
                    updateState(label.getChannelName(), QuantityType
                            .valueOf(label.getFactor() * Integer.parseInt(entry.getValue()), label.getUnit()));
                }
            }
            if (!label.getTimestampChannelName().equals(NOT_A_CHANNEL)) {
                String timestamp = frame.getAsDateTime(label);
                if (!timestamp.isEmpty()) {
                    logger.trace("Update channel {} to value {}", label.getTimestampChannelName(), timestamp);
                    updateState(label.getTimestampChannelName(), DateTimeType.valueOf(timestamp));
                }
            }
        }
        try {
            if (frame.getTicMode() == TeleinfoTicMode.HISTORICAL) {
                try {
                    if (frame.getPricing() == Pricing.TEMPO) {
                        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_1,
                                StringType.valueOf(frame.getProgrammeCircuit1()));
                        updateState(CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_2,
                                StringType.valueOf(frame.getProgrammeCircuit2()));
                    }
                } catch (InvalidFrameException e) {
                    logger.warn("Can not find pricing option.");
                }

                try {
                    Phase phase = frame.getPhase();
                    if (phase == Phase.ONE_PHASED) {
                        updateStateForMissingAlert(frame, Label.ADPS);
                    } else if (phase == Phase.THREE_PHASED) {
                        if (!wasLastFrameShort) {
                            updateStateForMissingAlert(frame, Label.ADIR1);
                            updateStateForMissingAlert(frame, Label.ADIR2);
                            updateStateForMissingAlert(frame, Label.ADIR3);
                        }
                        wasLastFrameShort = frame.isShortFrame();
                    }
                } catch (InvalidFrameException e) {
                    logger.warn("Can not find phase.");
                }
            } else {
                if (frame.getLabelToValues().containsKey(Label.RELAIS)) {
                    String relaisString = frame.get(Label.RELAIS);
                    if (relaisString != null) {
                        boolean[] relaisStates = FrameUtil.parseRelaisStates(relaisString);
                        for (int i = 0; i <= 7; i++) {
                            updateState(CHANNELS_LSM_RELAIS[i], OnOffType.from(relaisStates[i]));
                        }
                    }
                }
            }
        } catch (InvalidFrameException e) {
            logger.warn("Can not find TIC mode.");
        }

        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
    }

    private void updateStateForMissingAlert(Frame frame, Label label) {
        if (!frame.getLabelToValues().containsKey(label)) {
            updateState(label.getChannelName(), UnDefType.NULL);
        }
    }
}

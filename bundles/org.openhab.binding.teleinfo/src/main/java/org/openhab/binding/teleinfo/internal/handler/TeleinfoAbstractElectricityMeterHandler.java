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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.teleinfo.internal.reader.Frame;
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
            logger.debug("Controller is not online.", bridgeStatusInfo.getStatus());
            return;
        }

        logger.debug("Controller is ONLINE. Starting Electricity Meter thing initialisation");
        updateStatus(ThingStatus.UNKNOWN);
        // FIXME

        TeleinfoAbstractControllerHandler controllerHandler = (TeleinfoAbstractControllerHandler) getBridge()
                .getHandler();
        controllerHandler.addListener(this);
        logger.debug("Electricity Meter initialisation complete.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported
    }

    protected void updateStatesForCommonChannels(@NonNull Frame frame) {
        // update common channels
        updateState(CHANNEL_ISOUSC, new DecimalType(frame.getIntensiteSouscrite()));
        updateState(CHANNEL_PTEC, new StringType(frame.getPeriodeTarifaireEnCours().name()));
        if (frame.getIntensiteMaximale() == null) {
            updateState(CHANNEL_IMAX, UnDefType.NULL);
        } else {
            updateState(CHANNEL_IMAX, new DecimalType(frame.getIntensiteMaximale()));
        }

        if (frame.getAvertissementDepassementPuissanceSouscrite() == null) {
            updateState(CHANNEL_ADPS, UnDefType.NULL);
        } else {
            updateState(CHANNEL_ADPS, new DecimalType(frame.getAvertissementDepassementPuissanceSouscrite()));
        }
        updateState(CHANNEL_PAPP, new DecimalType(frame.getPuissanceApparente()));
        updateState(CHANNEL_IINST, new DecimalType(frame.getIntensiteInstantanee()));
        updateState(CHANNEL_LAST_UPDATE, new DateTimeType());

        BigDecimal powerFactor = (BigDecimal) getThing().getChannel(CHANNEL_CURRENT_POWER).getConfiguration()
                .get(CHANNEL_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
        updateState(CHANNEL_CURRENT_POWER, new DecimalType(frame.getIntensiteInstantanee() * powerFactor.intValue()));
    }
}

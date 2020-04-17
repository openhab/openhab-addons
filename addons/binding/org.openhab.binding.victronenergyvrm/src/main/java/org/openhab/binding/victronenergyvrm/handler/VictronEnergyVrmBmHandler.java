/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm.handler;

import static org.openhab.binding.victronenergyvrm.VictronEnergyVRMBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.victronenergyvrm.api.VictronEnergyVRMAuth;
import org.openhab.binding.victronenergyvrm.api.VictronEnergyVRMBatterySummery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VictronEnergyVrmBmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
@NonNullByDefault

public class VictronEnergyVrmBmHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VictronEnergyVrmBmHandler.class);

    private static final String BASE_URL = "https://vrmapi.victronenergy.com/v2/";

    // Default Value vrmlogger on Raspberry PI ist 900
    private static final int DEFAULT_REFRESH_RATE = 900;

    // avoid loops with errorcounter
    private int errorcounter;

    public VictronEnergyVrmBmHandler(Thing thing) {
        super(thing);
        errorcounter = 0;

    }

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable String token;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            // This should also happen at a newly linked channel also bei super.channelLinked()
            updateData();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Victron Energy VRM BM handler.");

        // Alten Token verwerfen, falls vorhanden.
        if (token != null) {
            token = null;
        }
        // Configuration?
        final Object username = getConfig().get(USERNAME);
        final Object password = getConfig().get(PASSWORD);

        logger.debug("Try to authenticate against VRM API.");

        VictronEnergyVRMAuth auth = new VictronEnergyVRMAuth(BASE_URL);

        String token = auth.GetToken(username.toString(), password.toString());
        this.token = token;

        if (token != null) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Login successfully");
            // logger.debug("Here is the token: " + token);
            pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, DEFAULT_REFRESH_RATE, TimeUnit.SECONDS);
        }

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void updateData() {
        logger.debug("updateData");
        final Object installId = getConfig().get(INSTALLID);
        final Object instanceId = getConfig().get(INSTANCEID);
        VictronEnergyVRMBatterySummery Bm = new VictronEnergyVRMBatterySummery(BASE_URL, (BigDecimal) installId,
                (BigDecimal) instanceId);
        Bm.loadData(this.token);
        if (Bm.getSuccess()) {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Set Errorcounter to 0");
                errorcounter = 0;
            }
            logger.debug("yeah! Got Data from api VRMBatterySummery");
            updateState(CHANNEL_BmV, new DecimalType(Bm.getBmV()));
            updateState(CHANNEL_BmVS, new DecimalType(Bm.getBmVS()));
            updateState(CHANNEL_BmI, new DecimalType(Bm.getBmI()));
            updateState(CHANNEL_BmCE, new DecimalType(Bm.getBmCE()));
            updateState(CHANNEL_BmSOC, new DecimalType(Bm.getBmSOC()));
            updateState(CHANNEL_BmTTG, new DecimalType(Bm.getBmTTG()));
            updateState(CHANNEL_BmAL, new StringType(Bm.getBmAL()));
            updateState(CHANNEL_BmAH, new StringType(Bm.getBmAH()));
            updateState(CHANNEL_BmALS, new StringType(Bm.getBmALS()));
            updateState(CHANNEL_BmAHS, new StringType(Bm.getBmAHS()));
            updateState(CHANNEL_BmASoc, new StringType(Bm.getBmASoc()));
            updateState(CHANNEL_BmALT, new StringType(Bm.getBmALT()));
            updateState(CHANNEL_BmAHT, new StringType(Bm.getBmAHT()));
            updateState(CHANNEL_BmAM, new StringType(Bm.getBmAM()));
            updateState(CHANNEL_BmSecondsAgo, new DecimalType(Bm.getsecondsAgo()));
        } else {
            // Wenn keine Daten geholt werden konnten und der Status nicht bereits schon "Offline" ist, setzt Status auf
            // Offline.
            if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                logger.warn("Couldn't get data from api VRMBatterySummery");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
            logger.debug("Increment Errorcounter.");
            errorcounter++;
            if (errorcounter < 3) {
                this.initialize();
                logger.debug("Try to initialize again. Already " + errorcounter + " times");
            } else {
                logger.warn("To many errors in a row. Maybe a wrong Installation ID. Please check this.");
            }
        }

    }

}

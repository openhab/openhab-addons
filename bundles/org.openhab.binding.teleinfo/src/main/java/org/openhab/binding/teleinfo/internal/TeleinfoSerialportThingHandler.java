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
package org.openhab.binding.teleinfo.internal;

import java.io.IOException;
import java.math.BigDecimal;

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
import org.openhab.binding.teleinfo.internal.reader.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.internal.reader.TeleinfoReaderListenerAdaptor;
import org.openhab.binding.teleinfo.internal.reader.io.TeleinfoInputStream;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.TeleinfoSerialportReader;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoSerialControllerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoSerialportThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoSerialportThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoSerialportThingHandler.class);

    private @Nullable TeleinfoSerialControllerConfiguration config;

    private @Nullable TeleinfoSerialportReader serialPortReader;
    private @Nullable TeleinfoReaderListenerAdaptor listener;
    // private @Nullable ScheduledFuture<?> pollingJob;

    public TeleinfoSerialportThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // // TODO: handle data refresh
        // }
        //
        // // TODO: handle command
        //
        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information:
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(TeleinfoSerialControllerConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                serialPortReader = new TeleinfoSerialportReader(config.serialport, 10 * 1000);
                serialPortReader.setWaitNextHeaderFrameTimeoutInMs(
                        TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 1000); // FIXME paramétrable
                serialPortReader.setReadingFrameTimeoutInMs(TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME * 1000); // FIXME
                                                                                                                       // paramétrable
                listener = new TeleinfoReaderListenerAdaptor() {
                    @Override
                    public void onFrameReceived(org.openhab.binding.teleinfo.internal.reader.TeleinfoReader reader,
                            final org.openhab.binding.teleinfo.internal.reader.Frame frame) {
                        logger.debug("onFrameReceived(TeleinfoReader, Frame) [start]");

                        // FIXME
                        FrameOptionHeuresCreuses hcFrame = (FrameOptionHeuresCreuses) frame;

                        updateState(TeleinfoBindingConstants.CHANNEL_ISOUSC,
                                new DecimalType(hcFrame.getIntensiteSouscrite()));
                        updateState(TeleinfoBindingConstants.CHANNEL_HCHC,
                                new DecimalType(hcFrame.getIndexHeuresCreuses()));
                        updateState(TeleinfoBindingConstants.CHANNEL_HCHP,
                                new DecimalType(hcFrame.getIndexHeuresPleines()));
                        updateState(TeleinfoBindingConstants.CHANNEL_PTEC,
                                new StringType(hcFrame.getPeriodeTarifaireEnCours().name()));
                        updateState(TeleinfoBindingConstants.CHANNEL_IMAX,
                                new DecimalType(hcFrame.getIntensiteMaximale()));
                        updateState(TeleinfoBindingConstants.CHANNEL_PAPP,
                                new DecimalType(hcFrame.getPuissanceApparente()));
                        updateState(TeleinfoBindingConstants.CHANNEL_IINST,
                                new DecimalType(hcFrame.getIntensiteInstantanee()));

                        BigDecimal powerFactor = (BigDecimal) getThing()
                                .getChannel(TeleinfoBindingConstants.CHANNEL_CURRENT_POWER).getConfiguration()
                                .get(TeleinfoBindingConstants.CHANNEL_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR);
                        updateState(TeleinfoBindingConstants.CHANNEL_CURRENT_POWER,
                                new DecimalType(hcFrame.getIntensiteInstantanee() * powerFactor.intValue()));

                        logger.debug("onFrameReceived(TeleinfoReader, Frame) [end]");
                    }
                };

                serialPortReader.addListener(listener);

                logger.info("Teleinfo serial port opening...");
                serialPortReader.open();
                logger.info("Teleinfo serial port opened");
                logger.info("Serial port initialized");

                updateStatus(ThingStatus.ONLINE);
            } catch (Throwable t) {
                final String errorMessage = "A fatal error occurred during '" + config.serialport
                        + "' Teleinfo serial port opening ('" + t.getMessage() + "')";
                logger.error(errorMessage, t);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, t.getMessage());
            }
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        // Runnable runnable = new Runnable() {
        // @Override
        // public void run() {
        // // execute some binding specific polling code
        //
        // }
        // };
        //
        // pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (serialPortReader != null) {
            try {
                if (listener != null && serialPortReader.getListeners().contains(listener)) {
                    serialPortReader.removeListener(listener);
                }
                serialPortReader.close();
                serialPortReader = null;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        dispose();
        super.handleRemoval();
    }

}

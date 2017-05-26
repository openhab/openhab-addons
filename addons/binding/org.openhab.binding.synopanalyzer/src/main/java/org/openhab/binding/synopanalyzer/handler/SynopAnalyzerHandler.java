/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer.handler;

import static org.openhab.binding.synopanalyzer.SynopAnalyzerBindingConstants.*;
import static org.openhab.binding.synopanalyzer.internal.UnitUtils.*;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.synopanalyzer.config.SynopAnalyzerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwpi.Constants;
import com.nwpi.synop.Synop;
import com.nwpi.synop.SynopLand;
import com.nwpi.synop.SynopMobileLand;
import com.nwpi.synop.SynopShip;

/**
 * The {@link SynopAnalyzerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SynopAnalyzerHandler extends BaseThingHandler {

    private static final String OGIMET_SYNOP_PATH = "/cgi-bin/getsynop";
    private static final String OGIMET_URL = "www.ogimet.com";

    private Logger logger = LoggerFactory.getLogger(SynopAnalyzerHandler.class);

    private ScheduledFuture<?> executionJob;
    protected SynopAnalyzerConfiguration configuration;

    public SynopAnalyzerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SynopAnalyzerConfiguration.class);

        logger.info("Scheduling Synop update thread to run every {} minute for Station '{}'",
                configuration.refreshInterval, configuration.stationId);

        executionJob = scheduler.scheduleWithFixedDelay(() -> {
            updateSynop();
        }, 1, configuration.refreshInterval, TimeUnit.MINUTES);
        super.initialize();

    }

    private Synop getLastAvailableSynop() {
        logger.debug("Retrieving last Synop message");
        Calendar observationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String message = "";

        for (int backInTime = 0; backInTime < 24 && !message.startsWith(configuration.stationId); backInTime++) {
            observationTime.roll(Calendar.HOUR, false);
            // Calendar observationTime = currentTime;
            // observationTime.ro.add(Calendar.HOUR, backInTime * -1);
            String url = forgeURL(observationTime);
            try {
                message = IOUtils.toString(new URL(url));
            } catch (IOException e) {
                logger.warn("Erroneous URL while preparing Synop request");
                updateStatus(ThingStatus.OFFLINE);
                return null;
            }
        }

        if (message.startsWith(configuration.stationId)) {
            logger.debug("Received a valid synop message");
            updateStatus(ThingStatus.ONLINE);

            String[] messageParts = message.split(",");
            String synopMessage = messageParts[messageParts.length - 1];

            Synop synopObject = createSynopObject(synopMessage);
            return synopObject;
        } else {
            logger.warn("No valid Synop for last 24h");
            updateStatus(ThingStatus.OFFLINE);
            return null;
        }
    }

    private void updateSynop() {
        logger.debug("Updating device channels");

        Synop synop = getLastAvailableSynop();
        if (synop != null) {
            for (Channel channel : getThing().getChannels()) {
                String channelId = channel.getUID().getId();
                State state = null;

                switch (channelId) {
                    case HORIZONTAL_VISIBILITY:
                        state = new StringType(synop.getHorizontalVisibility());
                        break;
                    case OCTA:
                        state = new DecimalType(synop.getOcta());
                        break;
                    case OVERCAST:
                        state = new StringType(synop.getOvercast());
                        break;
                    case PRESSURE:
                        state = new DecimalType(synop.getPressure());
                        break;
                    case TEMPERATURE:
                        state = new DecimalType(synop.getTemperature());
                        break;
                    case WIND_ANGLE:
                        state = new DecimalType(synop.getWindDirection());
                        break;
                    case WIND_DIRECTION:
                        int angle = synop.getWindDirection();
                        String direction = getWindDirection(angle);
                        state = new StringType(direction);
                        break;
                    case WIND_SPEED_MS:
                        if (synop.getWindUnit().equalsIgnoreCase("m/s")) {
                            state = new DecimalType(synop.getWindSpeed());
                        } else {
                            Double kmhSpeed = knotsToKmh(new Double(synop.getWindSpeed()));
                            state = new DecimalType(kmhToMps(kmhSpeed));
                        }
                        break;
                    case WIND_SPEED_KNOTS:
                        if (synop.getWindUnit().equalsIgnoreCase("knots")) {
                            state = new DecimalType(synop.getWindSpeed());
                        } else {
                            Double kmhSpeed = mpsToKmh(new Double(synop.getWindSpeed()));
                            Double knotSpeed = kmhToKnots(kmhSpeed);
                            state = new DecimalType(knotSpeed);
                        }
                        break;
                    case WIND_SPEED_BEAUFORT:
                        Double kmhSpeed;
                        if (synop.getWindUnit().equalsIgnoreCase("m/s")) {
                            kmhSpeed = mpsToKmh(new Double(synop.getWindSpeed()));
                        } else {
                            kmhSpeed = knotsToKmh(new Double(synop.getWindSpeed()));
                        }
                        Double beaufort = kmhToBeaufort(kmhSpeed);
                        state = new DecimalType(beaufort);
                        break;
                    case TIME_UTC:
                        Calendar observationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        observationTime.set(Calendar.DAY_OF_MONTH, synop.getDay());
                        observationTime.set(Calendar.HOUR_OF_DAY, synop.getHour());
                        observationTime.set(Calendar.MINUTE, 0);
                        observationTime.set(Calendar.SECOND, 0);
                        observationTime.set(Calendar.MILLISECOND, 0);
                        state = new DateTimeType(observationTime);
                        break;
                    default:
                        logger.debug("Unsupported channel Id '{}'", channelId);
                        break;
                }
                updateState(channel.getUID(), state);
            }
        }

    }

    private Synop createSynopObject(String synopMessage) {
        ArrayList<String> liste = new ArrayList<String>(Arrays.asList(synopMessage.split("\\s+")));
        if (synopMessage.startsWith(Constants.LAND_STATION_CODE)) {
            return new SynopLand(liste);
        } else if (synopMessage.startsWith(Constants.SHIP_STATION_CODE)) {
            return new SynopShip(liste);
        } else {
            return new SynopMobileLand(liste);
        }
    }

    private String forgeURL(Calendar currentTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH00");
        String beginDate = simpleDateFormat.format(currentTime.getTime());

        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("block", configuration.stationId));
        qparams.add(new BasicNameValuePair("begin", beginDate));

        URIBuilder builder = new URIBuilder().setScheme("http").setHost(OGIMET_URL).setPath(OGIMET_SYNOP_PATH)
                .setParameters(qparams);

        return builder.toString();
    }

    @Override
    public void dispose() {
        if (executionJob != null && !executionJob.isCancelled()) {
            executionJob.cancel(true);
            executionJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateSynop();
        }
    }

}

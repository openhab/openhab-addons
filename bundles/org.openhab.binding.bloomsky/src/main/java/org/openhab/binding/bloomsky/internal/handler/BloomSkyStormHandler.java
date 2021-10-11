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
package org.openhab.binding.bloomsky.internal.handler;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.*;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bloomsky.internal.bridge.BloomSkyBridge;
import org.openhab.binding.bloomsky.internal.config.BloomSkyBridgeConfiguration;
import org.openhab.binding.bloomsky.internal.connection.BloomSkyCommunicationException;
import org.openhab.binding.bloomsky.internal.dto.BloomSkyJsonSensorData;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link BloomSkyStormHandler} is responsible for handling updates to the STORM device channels.
 * There are no commands for these channels, they are all read-only. The STORM device is typically
 * associated with a SKY weather station and reports additional weather observations (Wind, Rain, UV).
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkyStormHandler extends BloomSkyAbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(BloomSkyStormHandler.class);

    private int refreshIntervalSeconds; // Specified in the bridge configuration

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_STORM);

    private @Nullable Future<?> refreshObservationsJob;

    private static final Map<String, Number> WIN_DIR_MAP;
    static {
        WIN_DIR_MAP = new HashMap<>();
        WIN_DIR_MAP.put("NE", 45);
        WIN_DIR_MAP.put("E", 90);
        WIN_DIR_MAP.put("SE", 135);
        WIN_DIR_MAP.put("S", 180);
        WIN_DIR_MAP.put("SW", 225);
        WIN_DIR_MAP.put("W", 270);
        WIN_DIR_MAP.put("NW", 315);
        WIN_DIR_MAP.put("N", 360);

    }

    /**
     * Runnable used to schedule background refresh of the Storm device details and observation channels
     */
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshStormObservations();
        }
    };

    /**
     * Constructor for the BloomSky Storm Handler.
     *
     * @param thing - the specific Storm device this handler will manage.
     * @param httpClient - common Http client used to connect to the BloomSky rest API
     * @param timeZoneProvider - used to determine time stamp relevant to users OH time zone.
     */
    public BloomSkyStormHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing, httpClient, timeZoneProvider);
    }

    /**
     * The initialize method starts a new Handler for a discovered STORM device.
     *
     * <ul>
     * <li>Retrieve the refresh interval from the bridge configuration.</li>
     * <li>Schedule the refresh job based on the interval.</li>
     * <li>Set the STORM status to ONLINE if the Bridge is Online, OFFLINE if not.</li>
     * </ul>
     */
    @Override
    public void initialize() {
        logger.debug("Initializing BloomSky Storm handler with bridge configuration display units: {}",
                getConfigAs(BloomSkyBridgeConfiguration.class).units);
        refreshIntervalSeconds = getConfigAs(BloomSkyBridgeConfiguration.class).refreshInterval * 60;
        weatherDataCache.clear();
        scheduleRefreshJob();
        updateStatus(isBridgeOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    /**
     * The dispose method removes the handler when the STORM thing is removed from OpenHab.
     */
    @Override
    public void dispose() {
        cancelRefreshJob();
        logger.debug("StormHandler - dispose: Handler = {}, Bridge UID = {}", getThing().getHandler(),
                getThing().getBridgeUID());
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * The handleCommand method to handles commands sent to a STORM channel. Only command is manual refresh, as all
     * channels are read only. The channel is only refreshed if it is linked to an item.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            State state = weatherDataCache.get(channelUID.getId());
            if (state != null) {
                updateChannel(channelUID, state);
                // updateChannel(channelUID.getId());
            }
        }
    }

    /**
     * The refreshStormObservations method ensures the bridge is ONLINE and configured to
     * successfully retrieve STORM data from the BloomSky rest API.
     * <ul>
     * <li>If Bridge is not ONLINE, do not continue and return.</li>
     * <li>If Bridge is ONLINE, and device information is returned from the API:</li>
     * <ul>
     * <li>Set STORM status to ONLINE.</li>
     * <li>Proceed to update the STORM channels that are linked to items.</li>
     * </ul>
     * </ul>
     */
    synchronized void refreshStormObservations() {
        if (!isBridgeOnline()) {
            // If bridge is not online, API has not been validated yet
            logger.debug("StormHandler: Can't refresh the Storm device observations because bridge is not online");
            return;
        }
        logger.debug("StormHandler: Requesting the weather observations from The BloomSky API");
        try {
            BloomSkyBridge bloomSkyBridge = getBloomSkyBridge();
            if (bloomSkyBridge != null) {
                BloomSkyJsonSensorData[] stormObservationsArray = bloomSkyBridge.getBloomSkyDevices();
                updateStatus(ThingStatus.ONLINE);
                updateStormObservations(stormObservationsArray);
            } else {
                logger.debug("BloomSky bridge did not return any results from the API.");
            }
        } catch (JsonSyntaxException e) {
            logger.debug("StormHandler: Error parsing BloomSky observations response object: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error parsing BloomSky observations");
            return;
        } catch (BloomSkyCommunicationException e) {
            logger.debug("StormHandler: Error communicating with the BloomSky API: {}", e.getLocalizedMessage());
        }
    }

    /**
     * The updateStormObservations method to update STORM observations. The response is searched for
     * a device that matches this specific STORM, if found then observations are updated for
     * channels that are linked to items.
     *
     * @param stormObservationsArray - DTO type {@link BloomSkyJsonSensorData} of JSON format arrays containing details
     *            of the response from the BloomSky API for one or more Storm devices.
     */
    private void updateStormObservations(BloomSkyJsonSensorData[] stormObservationsArray) {
        if (stormObservationsArray.length == 0) {
            logger.debug("StormHandler: The BloomSky API response contains zero(0) device observations!");
            return;
        }
        // Each array element in observations is a different device -
        // loop through observation list to find the correct device and its observations
        String thisDeviceId = this.getThing().getProperties().get(STORM_ASSOCIATED_WITH_SKY_DEVICE_ID);
        logger.debug("Get properties from StormHandler Thing - deviceId: {}", thisDeviceId);
        for (BloomSkyJsonSensorData obs : stormObservationsArray) {
            if (obs.getDeviceID().equals(thisDeviceId)) {
                logger.debug(
                        "Found observations for this Storm device {} in observation mathching device {}, detail observations follow: {}",
                        thisDeviceId, obs.getDeviceID(), obs.toString());
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup()
                            && channelUID.getGroupId() != null && isLinked(channelUID)) {
                        updateStormChannel(channelUID, obs);
                    } // end if channel is linked, update its state
                } // end for channel
            } // end if this sky handler device has observations returned from the BloomSky API
        } // end for loop parsing the Sky observations array (each array element represents 1 registered SKY device)
    } // end updatePwsObservations

    /**
     * The updateStormChannel method identifies the group that the channel belongs to, then calls that group's method to
     * update that specific channel.
     *
     * @param channelUID The channel full UID used to determine group and channel name
     * @param obs The single array string containing the details of the response from
     *            the BloomSky API for this specific Storm device thing.
     */
    private void updateStormChannel(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId == null) {
            logger.error("Channel group Id is null, unable to update Storm channel.");
        } else {
            switch (channelGroupId) {
                case CH_GROUP_STORM:
                    updateStormObservations(channelUID, obs);
                    break;
            }
        }
    }

    /**
     * the updateStormObservations method updates one channel using the ChannelID to identify the channel,
     * locate the specific observation in the {@link BloomSkyJsonSensorData} model, and update its value.
     *
     * @param channelUID - specific channel to be updated
     * @param obs - JSON object containing observations for this Specific STORM device
     */
    private void updateStormObservations(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        switch (channelId) {
            case CH_STORM_UV_INDEX:
                updateChannel(channelUID, undefOrDecimal(obs.getStorm().getuVIndex()));
                break;
            case CH_STORM_WIND_DIRECTION_COMPASS_ANGLE:
                Number windAngle = WIN_DIR_MAP.get(obs.getStorm().getWindDirection());
                updateChannel(channelUID, undefOrQuantity(windAngle, getWindAngleUnit()));
                break;
            case CH_STORM_WIND_DIRECTION_COMPASS_POINT:
                updateChannel(channelUID, undefOrString(obs.getStorm().getWindDirection()));
                break;
            case CH_STORM_RAIN_DAILY:
                updateChannel(channelUID, undefOrQuantity(obs.getStorm().getRainDaily(), getLengthUnit()));
                break;
            case CH_STORM_WIND_GUST:
                updateChannel(channelUID, undefOrQuantity(obs.getStorm().getWindGust(), getWindSpeedUnit()));
                break;
            case CH_STORM_SUSTAINED_WIND_SPEED:
                updateChannel(channelUID, undefOrQuantity(obs.getStorm().getSustainedWindSpeed(), getWindSpeedUnit()));
                break;
            case CH_STORM_RAIN_RATE:
                // Treating this double value as string to get the unit correct, OH is setting to MPH regardless type
                DecimalFormat df2 = new DecimalFormat("#.##");
                String rainRate = df2.format(obs.getStorm().getRainRate()) + " " + getRainRateUnit();
                updateChannel(channelUID, undefOrString(rainRate));
                logger.debug("Rain rate = {}, Rain Units = {}", obs.getStorm().getRainRate(), getRainRateUnit());
                // updateChannel(channelUID, undefOrQuantity(obs.getStorm().getRainRate(), getRainRateUnit()));
                break;
            case CH_STORM_RAIN_24H:
                updateChannel(channelUID, undefOrQuantity(obs.getStorm().getRain24h(), getLengthUnit()));
                break;
            case CH_CALCULATED_WIND_CHILL:
                updateChannel(channelUID,
                        undefOrWindChill(
                                calcWindChill(obs.getData().getTemperature(), obs.getStorm().getSustainedWindSpeed()),
                                getTempUnit()));
                break;
            case CH_STORM_TIME_STAMP:
                updateChannel(channelUID, undefOrDate(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
                break;
        }
    }

    /*
     * The refresh job updates the Storm current observations
     * on the refresh interval set in the bridge configuration
     */
    private void scheduleRefreshJob() {
        logger.debug("SKYHandler: Scheduling Storm observations refresh job in {} seconds",
                REFRESH_JOB_INITIAL_DELAY_SECONDS);
        cancelRefreshJob();
        refreshObservationsJob = scheduler.scheduleWithFixedDelay(refreshRunnable, REFRESH_JOB_INITIAL_DELAY_SECONDS,
                refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     *
     */
    private void cancelRefreshJob() {
        if (refreshObservationsJob != null) {
            refreshObservationsJob.cancel(true);
            logger.debug("StormHandler: Canceling Storm observations refresh job");
        }
    }

    /**
     * @return
     */
    @Nullable
    private BloomSkyBridge getBloomSkyBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof BloomSkyBridgeHandler) {
                BloomSkyBridgeHandler bridgeHandler = (BloomSkyBridgeHandler) handler;
                return bridgeHandler.getBloomSkyBridge();
            }
        }
        return null;
    }
}

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

import java.util.Collections;
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
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.unit.Units;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link BloomSkySKYHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkySKYHandler extends BloomSkyAbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(BloomSkySKYHandler.class);

    private int refreshIntervalSeconds;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SKY);

    private @Nullable Future<?> refreshObservationsJob;

    /**
     * Runnable used to schedule background refresh of the Sky device details and observation channels
     */
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshSkyObservations();
        }
    };

    /**
     * The constructor for {@link BloomSkySKYHandler}
     *
     * @param thing - Sky thing
     * @param httpClient - Common HTTP client
     * @param timeZoneProvider - to correctly list date and time stamps in local time
     */
    public BloomSkySKYHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing, httpClient, timeZoneProvider);
    }

    /**
     * The initialize method starts a new Handler for a discovered Sky device.
     *
     * <ul>
     * <li>Retrieve the refresh interval from the bridge configuration.</li>
     * <li>Schedule the refresh job based on the interval.</li>
     * <li>Set the Sky status to ONLINE if the Bridge is Online, OFFLINE if not.</li>
     * </ul>
     */
    @Override
    public void initialize() {
        logger.debug("Initializing BloomSky Sky handler with bridge configuration display units: {}",
                getConfigAs(BloomSkyBridgeConfiguration.class).units);
        refreshIntervalSeconds = getConfigAs(BloomSkyBridgeConfiguration.class).refreshInterval * 60;
        weatherDataCache.clear();
        scheduleRefreshJob();
        updateStatus(isBridgeOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    /**
     * The dispose method removes this Sky Handler, cancels the channel refresh and set the status to offline.
     */
    @Override
    public void dispose() {
        cancelRefreshJob();
        logger.debug("SkyHandler - dispose: Handler = {}, Bridge UID = {}", getThing().getHandler(),
                getThing().getBridgeUID());
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * Method to handles commands sent to a SKY channel. Only command is manual refresh, as all
     * channels are read only. The channel is only refreshed if it is linked to an item.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            State state = weatherDataCache.get(channelUID.getId());
            if (state != null) {
                updateChannel(channelUID, state);
            }
        }
    }

    /**
     * The refreshSkyObservations method ensures the bridge is ONLINE and configured to
     * successfully retrieve Sky data from the BloomSky rest API.
     * <ul>
     * <li>If Bridge is not ONLINE, do not continue and return.</li>
     * <li>If Bridge is ONLINE, and device information is returned from the API:</li>
     * <ul>
     * <li>Set Sky status to ONLINE.</li>
     * <li>Proceed to update the Sky channels that are linked to items.</li>
     * </ul>
     * </ul>
     */
    synchronized void refreshSkyObservations() {
        if (!isBridgeOnline()) {
            // If bridge is not online, API has not been validated yet
            logger.debug("SKYHandler: Can't refresh the SKY device observations because bridge is not online");
            return;
        }
        logger.debug("SKYHandler: Requesting the weather observations from The BloomSky API");
        try {
            BloomSkyBridge bloomSkyBridge = getBloomSkyBridge();
            if (bloomSkyBridge != null) {
                BloomSkyJsonSensorData[] skyObservationsArray = bloomSkyBridge.getBloomSkyDevices();
                updateStatus(ThingStatus.ONLINE);
                updateSkyObservations(skyObservationsArray);
            } else {
                logger.debug("BloomSky bridge did not return any results from the API.");
            }
        } catch (JsonSyntaxException e) {
            logger.debug("SKYHandler: Error parsing BloomSky observations response object: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error parsing BloomSky observations");
            return;
        } catch (BloomSkyCommunicationException e) {
            logger.debug("SKYHandler: Error communicating with the BloomSky API: {}", e.getLocalizedMessage());
        }
    }

    /**
     * The updateSkyObservations method ensures that at least one device was returned from the BloomSky API.
     * Loop through response to find the Sky device id that matches this Sky things Id that was stored in
     * properties. Once the device is found, loop through the channels and update the states of all
     * channels that are linked to an item.
     *
     * @param skyObservations - String List of JSON arrays containing details of the response from the
     *            BloomSky API for one or more SKY devices.
     */
    private void updateSkyObservations(BloomSkyJsonSensorData[] skyObservationsArray) {
        if (skyObservationsArray.length == 0) {
            logger.debug("SKYHandler: The BloomSky API response contains zero(0) device observations!");
            return;
        }

        logger.debug("Number of Devices found: '{}'", skyObservationsArray.length);
        String thisDeviceId = this.getThing().getProperties().get(SKY_DEVICE_ID);
        logger.debug("Get properties from SkyHandler Thing - deviceId: {}", thisDeviceId);
        for (BloomSkyJsonSensorData obs : skyObservationsArray) {
            if (obs.getDeviceID().equals(thisDeviceId)) {
                logger.debug(
                        "Found observations for this SKY device {} in observation device {}, detail observations follow: {}",
                        thisDeviceId, obs.getDeviceID(), obs.toString());
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup()
                            && channelUID.getGroupId() != null && isLinked(channelUID)) {
                        updateSkyChannel(channelUID, obs);
                    } // end if channel is linked, update its state
                } // end for channel
            } // end if this sky handler device has observations returned from the BloomSky API
        } // end for loop parsing the Sky observations array (each array element represents 1 registered SKY device)
    } // end updatePwsObservations

    /**
     * The updateSkyChannel method identifies the group that the channel belongs to, then calls that group's method to
     * update that specific channel.
     *
     * @param channelUID The channel full UID used to determine group and channel name
     * @param obs The single array string containing the details of the response from
     *            the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyChannel(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelGroupId = channelUID.getGroupId();
        if (channelGroupId == null) {
            logger.error("Channel group Id is null, unable to update SKY channels.");
        } else {
            switch (channelGroupId) {
                case CH_GROUP_SKY:
                    updateSkyDeviceInformation(channelUID, obs);
                    break;
                case CH_GROUP_SKY_OBSERVATIONS:
                    updateSkyObservations(channelUID, obs);
                    break;
                case CH_GROUP_SKY_PREVIEW_IMAGE_LIST:
                    if (obs.getPreviewImageList().size() <= 0) {
                        logger.error("Sky Preview Image list is empty, unable to update {} .",
                                CH_GROUP_SKY_PREVIEW_IMAGE_LIST);
                    } else {
                        updateSkyPreviewImageList(channelUID, obs);
                    }
                    break;
                case CH_GROUP_SKY_VIDEO_LIST:
                    if (obs.getVideoList().size() <= 0) {
                        logger.error("Sky Video list is empty, unable to update {} .", CH_GROUP_SKY_VIDEO_LIST);
                    } else {
                        updateSkyVideoList(channelUID, obs);
                    }
                    break;
                case CH_GROUP_SKY_VIDEO_LIST_C:
                    if (obs.getVideoListC().size() <= 0) {
                        logger.error("Sky Video Celsius list is empty, unable to update {} .",
                                CH_GROUP_SKY_VIDEO_LIST_C);
                    } else {
                        updateSkyVideoListCelsius(channelUID, obs);
                    }
                    break;
            }
        }
    }

    /**
     * The updateSkyDeviceInformation method updates channels associated with the Sky device. The channelId
     * is used to identify the correct channel to update. The channel is only updated if it is linked to
     * an item.
     *
     * @param channelUID - The channel full UID used to determine the channel name
     * @param obs - The single array string containing the details of the response from
     *            the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyDeviceInformation(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        switch (channelId) {
            case CH_SKY_UTC:
                updateChannel(channelUID, undefOrDecimal(obs.getuTC()));
                break;
            case CH_SKY_CITY_NAME:
                updateChannel(channelUID, undefOrString(obs.getCityName()));
                break;
            case CH_SKY_SEARCHABLE:
                updateChannel(channelUID, undefOrString(obs.getSearchable()));
                break;
            case CH_SKY_DEVICE_NAME:
                updateChannel(channelUID, undefOrString(obs.getDeviceName()));
                break;
            case CH_SKY_DST:
                updateChannel(channelUID, undefOrInteger(obs.getdST()));
                break;
            case CH_SKY_LON:
                updateChannel(channelUID, undefOrDecimal(obs.getlON()));
                break;
            case CH_SKY_VIDEO_LIST:
                updateChannel(channelUID, undefOrStringList(obs.getVideoList()));
                break;
            case CH_SKY_VIDEO_LIST_C:
                updateChannel(channelUID, undefOrStringList(obs.getVideoListC()));
                break;
            case CH_SKY_DEVICE_ID:
                updateChannel(channelUID, undefOrString(obs.getDeviceID()));
                break;
            case CH_SKY_NUM_OF_FOLLOWERS:
                updateChannel(channelUID, undefOrInteger(obs.getNumOfFollowers()));
                break;
            case CH_SKY_LAT:
                updateChannel(channelUID, undefOrDecimal(obs.getlAT()));
                break;
            case CH_SKY_ALT:
                updateChannel(channelUID, undefOrQuantity(obs.getaLT(), getDistanceUnit()));
                break;
            case CH_SKY_LOCATION:
                updateChannel(channelUID, undefOrPoint(obs.getlAT(), obs.getlON()));
                break;
            case CH_SKY_FULL_ADDRESS:
                updateChannel(channelUID, undefOrString(obs.getFullAddress()));
                break;
            case CH_SKY_STREET_NAME:
                updateChannel(channelUID, undefOrString(obs.getStreetName()));
                break;
            case CH_SKY_PREVIEW_IMAGE_LIST:
                updateChannel(channelUID, undefOrStringList(obs.getPreviewImageList()));
                break;
            case CH_SKY_REGISTER_TIME:
                updateChannel(channelUID, undefOrDate(obs.getRegisterTime()));
                break;
        }
    }

    /**
     * The updateSkyObservations method updates channels associated with the Sky weather observations. The channelId
     * is used to identify the correct channel to update. The channel is only updated if it is linked to
     * an item.
     *
     * @param channelUID - The channel full UID used to determine the channel name
     * @param obs - The single array string containing the details of the response from
     *            the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyObservations(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        switch (channelId) {
            case CH_SKY_LUMINANCE:
                updateChannel(channelUID, undefOrQuantity(obs.getData().getLuminance(), Units.CANDELA));
                break;
            case CH_SKY_TEMPERATURE:
                updateChannel(channelUID, undefOrQuantity(obs.getData().getTemperature(), getTempUnit()));
                break;
            case CH_CALCULATED_HEAT_INDEX:
                updateChannel(channelUID, undefOrQuantity(
                        calcHeatIndex(obs.getData().getTemperature(), obs.getData().getHumidity()), getTempUnit()));
                break;
            case CH_CALCULATED_DEW_POINT:
                updateChannel(channelUID, undefOrQuantity(
                        calcDewPoint(obs.getData().getTemperature(), obs.getData().getHumidity()), getTempUnit()));
                break;
            case CH_SKY_IMAGE_URL:
                updateChannel(channelUID, undefOrString(obs.getData().getImageURL()));
                break;
            case CH_SKY_CURRENT_IMAGE:
                String url = obs.getData().getImageURL();
                if (url != null) {
                    // Download the image from the URL in a different thread to not delay the other operations
                    scheduler.submit(() -> {
                        RawType image = HttpUtil.downloadImage(url, true, 500000);
                        updateChannel(channelUID, image != null ? image : UnDefType.UNDEF);
                    });
                } else {
                    updateChannel(channelUID, UnDefType.UNDEF);
                }
                break;
            case CH_SKY_TS:
                updateChannel(channelUID, undefOrDate(obs.getData().gettS()));
                break;
            case CH_SKY_RAIN:
                updateChannel(channelUID, undefOrString(obs.getData().getRain()));
                break;
            case CH_SKY_HUMIDITY:
                updateChannel(channelUID, undefOrQuantity(obs.getData().getHumidity(), Units.PERCENT));
                break;
            case CH_SKY_PRESSURE:
                updateChannel(channelUID, undefOrQuantity(obs.getData().getPressure(), getPressureUnit()));
                break;
            case CH_SKY_DEVICE_TYPE:
                updateChannel(channelUID, undefOrString(obs.getData().getDeviceType()));
                break;
            case CH_SKY_VOLTAGE:
                updateChannel(channelUID, undefOrQuantity(obs.getData().getVoltage(), getVoltageUnit()));

                break;
            case CH_SKY_NIGHT:
                updateChannel(channelUID, undefOrString(obs.getData().getNight()));
                break;
            case CH_SKY_UVINDEX:
                updateChannel(channelUID, undefOrString(obs.getData().getuVIndex()));
                break;
            case CH_SKY_IMAGE_TS:
                updateChannel(channelUID, undefOrDate(obs.getData().getImageTS()));
                break;
        }
    }

    /**
     * The updateSkyPreviewImageList method updates channels associated with the Sky weather time lapse video preview
     * images.
     * The channelId is used to identify the correct channel to update. The channel is only updated if it is linked to
     * an item.
     *
     * @param channelUID - The channel full UID used to determine the channel name
     * @param obs - The single array string containing the details of the response from
     *            the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyPreviewImageList(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        String url = null;
        int obsArraySize = obs.getPreviewImageList().size();
        // The most recent image (yesterday) is always last element in array: Image_1 is oldest, Image_5 is newest
        // These are associated with the time lapse videos.
        switch (channelId) {
            case CH_SKY_SKY_PREVIEW_IMAGE_1:
                if (obsArraySize >= 5) {
                    url = obs.getPreviewImageList().get(4);
                }
                break;
            case CH_SKY_SKY_PREVIEW_IMAGE_2:
                if (obsArraySize >= 4) {
                    url = obs.getPreviewImageList().get(3);
                }
                break;
            case CH_SKY_SKY_PREVIEW_IMAGE_3:
                if (obsArraySize >= 3) {
                    url = obs.getPreviewImageList().get(2);
                }
                break;
            case CH_SKY_SKY_PREVIEW_IMAGE_4:
                if (obsArraySize >= 2) {
                    url = obs.getPreviewImageList().get(1);
                }
                break;
            case CH_SKY_SKY_PREVIEW_IMAGE_5:
                if (obsArraySize >= 1) {
                    url = obs.getPreviewImageList().get(0);
                }
                break;
        }
        if (url != null) {
            // Download the image from the URL in a different thread to not delay the other operations
            String imageUrl = url;
            scheduler.submit(() -> {
                RawType image = HttpUtil.downloadImage(imageUrl, true, 500000);
                updateChannel(channelUID, image != null ? image : UnDefType.UNDEF);
            });
        } else {
            updateChannel(channelUID, UnDefType.UNDEF);
        }
    }

    /**
     * The updateSkyVideoList method updates channels associated with the Sky weather time lapse videos.
     * The channelId is used to identify the correct channel to update. The channel is only updated
     * if it is linked to an item.
     *
     * @param channelUID - The channel full UID used to determine the channel name
     * @param obs - DTO type {@link BloomSkyJsonSensorData} single array string containing the details of the response
     *            from the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyVideoList(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        int obsArraySize = obs.getVideoList().size();
        // The most recent video (yesterday) is always last element in array: Day_1 is oldest, Day_5 is newest
        // These are associated with the preview images.
        switch (channelId) {
            case CH_SKY_SKY_VIDEO_DAY_1:
                if (obsArraySize >= 5) {
                    updateChannel(channelUID, undefOrString(obs.getVideoList().get(4)));
                }
                break;
            case CH_SKY_SKY_VIDEO_DAY_2:
                if (obsArraySize >= 4) {
                    updateChannel(channelUID, undefOrString(obs.getVideoList().get(3)));
                }
                break;
            case CH_SKY_SKY_VIDEO_DAY_3:
                if (obsArraySize >= 3) {
                    updateChannel(channelUID, undefOrString(obs.getVideoList().get(2)));
                }
                break;
            case CH_SKY_SKY_VIDEO_DAY_4:
                if (obsArraySize >= 2) {
                    updateChannel(channelUID, undefOrString(obs.getVideoList().get(1)));
                }
                break;
            case CH_SKY_SKY_VIDEO_DAY_5:
                if (obsArraySize >= 1) {
                    updateChannel(channelUID, undefOrString(obs.getVideoList().get(0)));
                }
                break;
        }
    }

    /**
     * The updateSkyVideoListCelsius method updates channels associated with the Sky weather time lapse videos.
     * The channelId is used to identify the correct channel to update. The channel is only updated
     * if it is linked to an item.
     *
     * @param channelUID - The channel full UID used to determine the channel name
     * @param obs - The single array string containing the details of the response from
     *            the BloomSky API for this specific Sky device thing.
     */
    private void updateSkyVideoListCelsius(ChannelUID channelUID, BloomSkyJsonSensorData obs) {
        String channelId = channelUID.getIdWithoutGroup();
        int obsArraySize = obs.getVideoListC().size();
        // The most recent video (yesterday) is always last element in array: Day_1 is oldest, Day_5 is newest
        // These are associated with the preview images.
        switch (channelId) {
            case CH_SKY_SKY_VIDEO_C_DAY_1:
                if (obsArraySize >= 5) {
                    updateChannel(channelUID, undefOrString(obs.getVideoListC().get(4)));
                }
                break;
            case CH_SKY_SKY_VIDEO_C_DAY_2:
                if (obsArraySize >= 4) {
                    updateChannel(channelUID, undefOrString(obs.getVideoListC().get(3)));
                }
                break;
            case CH_SKY_SKY_VIDEO_C_DAY_3:
                if (obsArraySize >= 3) {
                    updateChannel(channelUID, undefOrString(obs.getVideoListC().get(2)));
                }
                break;
            case CH_SKY_SKY_VIDEO_C_DAY_4:
                if (obsArraySize >= 2) {
                    updateChannel(channelUID, undefOrString(obs.getVideoListC().get(1)));
                }
                break;
            case CH_SKY_SKY_VIDEO_C_DAY_5:
                if (obsArraySize >= 1) {
                    updateChannel(channelUID, undefOrString(obs.getVideoListC().get(0)));
                }
                break;
        }
    }

    /*
     * The scheduleRefreshJob method updates the the weather stations details/current observations
     * on the refresh interval set in the Bridge configuration.
     */
    private void scheduleRefreshJob() {
        logger.debug("SKYHandler: Scheduling SKY observations refresh job in {} seconds",
                REFRESH_JOB_INITIAL_DELAY_SECONDS);
        cancelRefreshJob();
        refreshObservationsJob = scheduler.scheduleWithFixedDelay(refreshRunnable, REFRESH_JOB_INITIAL_DELAY_SECONDS,
                refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * The cancelRefreshJob method is used to stop the refresh job in situations where a new device has been added,
     * removed or changed.
     */
    private void cancelRefreshJob() {
        if (refreshObservationsJob != null) {
            refreshObservationsJob.cancel(true);
            logger.debug("SKYHandler: Canceling SKY observations refresh job");
        }
    }

    /**
     * The getBloomSkyBridge method is used to identify the bridge handler for this thing.
     *
     * @return The bridge handler
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

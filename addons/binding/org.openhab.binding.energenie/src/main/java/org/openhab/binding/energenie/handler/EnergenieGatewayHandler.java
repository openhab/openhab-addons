/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonResponseUtil;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulHttpResponseException;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulJsonResponseException;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handler for the Mi|Home Gateway (MIHO001). The gateway is a central communication hub between the individual devices.
 *
 * @author Mihaela Memova - Initial contribution
 * @author Svilen Valkanov - Added methods to get information about subdevices
 */
public class EnergenieGatewayHandler extends BaseBridgeHandler {

    // Warning messages used when some unexpected behavior is encountered
    private static final String NOT_EXISTING_GATEWAY_MESSAGE = "The gateway can not be found. Please make sure it is registered in the https://mihome4u.co.uk/ ";
    private static final String NOT_CONFIGURED_BINDING_MESSAGE = "Your binding is not configured yet. Please set the credentials first. ";
    private static final String NOT_ACTIVE_GATEWAY_MESSAGE = "The gateway has not been active in the last two minutes. Please check your connection.";
    private static final String INVALID_USER_MESSAGE = "Invalid email address. Please enter the email address that you used when registering in https://mihome4u.co.uk/";
    private static final String REJECTED_EXECUTION_OF_UPDATE_TASK_MESSAGE = "The update task cannot be scheduled for execution.";
    private static final String UNSUCCESSFUL_DATE_PARSING_MESSAGE = "Could not get the 'inactive' time interval of the gateway. Something went wrong with the parsing.";
    private static final String ERROR_IN_JSON_RESPONSE_ERROR_MESSAGE = "The JSON response status is: %s and it contains the following error: %s";
    private static final int INITIAL_REFRESH_DELAY_SECONDS = 10;

    /**
     * The longest allowed inactive time interval (in seconds) of the gateway
     * device. After that interval, if the gateway is still inactive, the thing
     * goes offline. If a gateway is active or not is determined by its
     * 'last_seen' property.
     */
    private static final int MAXIMUM_INACTIVE_PERIOD_SECONDS = 120;

    /** Pattern used to check if a given String represents an email address */
    public static final String EMAIL_PATTERN = ".+@.+(\\.[-A-Za-z]{2,})";

    /** Default update interval in seconds */
    public static final BigDecimal DEFAULT_UPDATE_INTERVAL = new BigDecimal(30);

    private EnergenieApiConfiguration accountCredentials;

    private RestClient restClient;

    /** ID of the gateway device (received after registration) */
    private int gatewayId;

    /** Instance of the helper class used for the requests */
    private EnergenieApiManager apiManager;

    /** Update interval in seconds */
    private long updateInterval;

    /** The result of scheduling the refresh thread */
    private ScheduledFuture<?> updateTaskResult;

    /** Used for logging */
    private final Logger logger = LoggerFactory.getLogger(EnergenieGatewayHandler.class);

    public EnergenieGatewayHandler(Bridge bridge, EnergenieApiConfiguration accountCredentials, RestClient restClient) {
        super(bridge);
        this.accountCredentials = accountCredentials;
        this.restClient = restClient;
        logger.debug("Created EnergenieGatewayHandler for thing {}", getThing().getUID());
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EnergenieGatewayHandler...");
        Configuration configuration = getConfig();
        String email = accountCredentials.getUserName();
        String password = accountCredentials.getPassword();
        BigDecimal interval = (BigDecimal) configuration.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL);
        this.updateInterval = interval.longValue();

        if (email == null || password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, NOT_CONFIGURED_BINDING_MESSAGE);
            return;
        }

        if (!isTextMatchingPattern(email, EMAIL_PATTERN)) {
            logger.debug(INVALID_USER_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, INVALID_USER_MESSAGE);
            return;
        }
        configureApiManager(email, password);

        Map<String, String> properties = thing.getProperties();
        if (properties.containsKey(EnergenieBindingConstants.PROPERTY_DEVICE_ID)) {
            Double gatewayID = Double.valueOf(properties.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));
            gatewayId = gatewayID.intValue();
            updateStatus(ThingStatus.ONLINE);
            scheduleUpdateTask(this.updateInterval);
        }
    }

    private void scheduleUpdateTask(long updateInterval) {
        if (updateTaskResult != null) {
            this.updateTaskResult.cancel(true);
            this.updateTaskResult = null;
        }
        /**
         * The refresh thread which is used for checking the "last_seen_at"
         * property of the gateway
         */
        Runnable refreshRunnable = () -> {
            logger.debug("Executing the refresh thread...");
            checkGateway();
        };
        try {
            updateTaskResult = scheduler.scheduleWithFixedDelay(refreshRunnable, INITIAL_REFRESH_DELAY_SECONDS,
                    updateInterval, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    REJECTED_EXECUTION_OF_UPDATE_TASK_MESSAGE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * No command to handle
         */
    }

    /**
     * Checks when the gateway was last active and updates the thing's channel.
     * This method is called periodically by a refresh thread.
     */
    public void checkGateway() {
        /*
         * Mi|Home REST API does not provide a method for showing a gateway's
         * information. So in order to check when was the last time it was seen
         * you need to list all gateways and find the one you want by its id.
         */
        JsonGateway gateway = null;
        try {
            gateway = getGatewayById(gatewayId);
        } catch (IOException e) {
            logger.error("The gateway with ID {} cannot be found. Changing thing status to offline", gatewayId, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        } catch (UnsuccessfulHttpResponseException e) {
            logger.error(
                    "EnergenieApiManager returned unsuccessful response: {} while trying to find gateway with ID {}. Changing thing status to offline",
                    e.getResponse().getReason(), gatewayId, e);
            logger.error("Changing thing status to offline:  {}", e.getResponse().getReason());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getResponse().getReason());
            return;
        } catch (UnsuccessfulJsonResponseException e) {
            String responseStatus = JsonResponseUtil.getResponseStatus(e.getResponse());
            JsonObject responseData = e.getResponse().get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
            String jsonErrorMessage = JsonResponseUtil.getErrorMessageFromResponse(responseData);
            String detailedErrorMessage = String.format(ERROR_IN_JSON_RESPONSE_ERROR_MESSAGE, responseStatus,
                    jsonErrorMessage);
            switch (responseStatus) {
                case JsonResponseConstants.RESPONSE_ACCESS_DENIED:
                case JsonResponseConstants.RESPONSE_INTERNAL_SERVER_ERROR:
                case JsonResponseConstants.RESPONSE_MAINTENANCE:
                case JsonResponseConstants.RESPONSE_PARAMETER_ERROR:
                case JsonResponseConstants.RESPONSE_VALIDATION_ERROR:
                    logger.error(detailedErrorMessage);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detailedErrorMessage);
                    break;
                case JsonResponseConstants.RESPONSE_NOT_FOUND:
                    logger.warn("{}", jsonErrorMessage);
                    break;
            }
            return;
        }

        if (gateway != null) {
            setProperties(gateway);

            String lastSeen = gateway.getLastSeenAt();
            Channel channel = thing.getChannel(EnergenieBindingConstants.CHANNEL_LAST_SEEN);

            if (lastSeen != null && channel != null) {
                ChannelUID channelUID = channel.getUID();

                String lastSeenProperTimeZone = getLastSeenDateWithProperTimeZone(lastSeen);
                State lastSeenState = DateTimeType.valueOf(lastSeenProperTimeZone);

                updateState(channelUID, lastSeenState);
                handleLastSeenProperty(lastSeenProperTimeZone);
            }
        }
    }

    /**
     * Mi|Home REST API provides "last_seen_at" property, which represents the day and the time when the gateway was
     * last seen connected to the server. The time zone is UTC (Universal Coordinated Time) so in order for the user to
     * see it in present time we convert it to the user's time zone
     *
     * @param lastSeenTime timestamp representing the day and the time when the gateway was last seen connected to the
     *            server
     * @return the last seen property with the proper time zone as String
     */
    private String getLastSeenDateWithProperTimeZone(String lastSeenTime) {
        /*
         * The Z at end represents the UTC time zone. In order to parse it properly we replace it with +0000
         */
        String lastSeen = lastSeenTime.replace("Z", "+0000");
        String timeZone = TimeZone.getDefault().getID();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern((EnergenieBindingConstants.DATE_TIME_PATTERN));
        OffsetDateTime offsetDateTime = ZonedDateTime.parse(lastSeen, sdf).toOffsetDateTime();
        ZonedDateTime dateProperTimeZone = offsetDateTime.atZoneSameInstant(ZoneId.of(timeZone));
        lastSeen = dateProperTimeZone.format(sdf);
        return lastSeen;
    }

    /**
     * Searches in the list of all registered gateways and finds the one with the given id
     *
     * @param id the id of the searched gateway
     * @return the gateway with the given id if it is registered or null if it is not
     * @throws UnsuccessfulJsonResponseException
     * @throws IOException
     * @throws UnsuccessfulHttpResponseException
     */
    private JsonGateway getGatewayById(int searchedId)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        JsonGateway[] gateways = apiManager.listGateways();

        for (JsonGateway gateway : gateways) {
            int id = gateway.getID();
            if (id == searchedId) {
                String firmwareVersion = apiManager.getFirmwareInformation(id);
                if (firmwareVersion != null) {
                    gateway.setFirmwareVersionID(firmwareVersion);
                }
                return gateway;
            }
        }

        logger.debug(NOT_EXISTING_GATEWAY_MESSAGE);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, NOT_EXISTING_GATEWAY_MESSAGE);
        return null;
    }

    private void handleLastSeenProperty(String lastSeenString) {
        Long timeInterval = null;
        ZonedDateTime curentLocalDateTime = ZonedDateTime.now();
        String currentDateTimeString = DateTimeFormatter.ofPattern(EnergenieBindingConstants.DATE_TIME_PATTERN)
                .format(curentLocalDateTime);

        try {
            timeInterval = getTimeInterval(currentDateTimeString, lastSeenString);
        } catch (ParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, UNSUCCESSFUL_DATE_PARSING_MESSAGE);
        }
        if (timeInterval != null) {
            if (timeInterval >= MAXIMUM_INACTIVE_PERIOD_SECONDS && thing.getStatus().equals(ThingStatus.ONLINE)) {
                logger.debug(NOT_ACTIVE_GATEWAY_MESSAGE);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, NOT_ACTIVE_GATEWAY_MESSAGE);
            } else if (thing.getStatus().equals(ThingStatus.OFFLINE)
                    && timeInterval < MAXIMUM_INACTIVE_PERIOD_SECONDS) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Calculates the difference (in seconds) between two datetime stamps given as Strings
     *
     * @param currentTime current datetime stamp
     * @param lastSeenTime datetime stamp which shows when was the last time the gateway
     * @return the time that has elapsed between previousDateTime and currentDateTime (in seconds)
     * @throws ParseException if any of the date Strings cannot be parsed correctly
     */
    private Long getTimeInterval(String currentTime, String lastSeenTime) throws ParseException {
        /*
         * The Z at end represents the UTC time zone. In order to parse it properly we replace it with +0000
         */
        lastSeenTime = lastSeenTime.replace("Z", "+0000");
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnergenieBindingConstants.DATE_TIME_PATTERN);
        ZonedDateTime currentDateTime = ZonedDateTime.parse(currentTime, sdf);
        ZonedDateTime lastSeenDateTime = ZonedDateTime.parse(lastSeenTime, sdf);
        return Duration.between(lastSeenDateTime, currentDateTime).getSeconds();
    }

    /**
     * Configures the {@link #apiManager}
     */
    private void configureApiManager(String username, String password) {
        logger.debug("Configuring the API Manager for user: {}", username);

        if (restClient != null) {
            EnergenieApiConfiguration restConfig = new EnergenieApiConfiguration(username, password);
            restClient.setConnectionTimeout(RestClient.DEFAULT_REQUEST_TIMEOUT);

            apiManager = new EnergenieApiManagerImpl(restConfig, restClient);
        }
    }

    public EnergenieApiManager getEnergenieApiManager() {
        return apiManager;
    }

    /**
     * Verify whether a given text matches a certain pattern
     *
     * @param text string to check
     * @param patternToMatch pattern to match
     * @return true if the text matches the pattern, false - otherwise
     */
    private boolean isTextMatchingPattern(String text, String patternToMatch) {
        Pattern pattern = Pattern.compile(patternToMatch);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    /**
     * Sets the thing properties
     *
     * @param dataObj JSON object containing all the needed information
     */
    private void setProperties(JsonGateway gateway) {
        logger.debug("Setting the thing properties...");
        Map<String, String> props = editProperties();
        props.put(EnergenieBindingConstants.PROPERTY_PORT, Integer.toString(gateway.getPort()));
        props.put(EnergenieBindingConstants.PROPERTY_IP_ADDRESS, gateway.getIpAddress());
        props.put(EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION, gateway.getFirmwareVersionID());
        props.put(EnergenieBindingConstants.PROPERTY_TYPE, gateway.getType().toString());
        props.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(gateway.getID()));
        props.put(EnergenieBindingConstants.PROPERTY_USER_ID, Integer.toString(gateway.getUserID()));
        props.put(EnergenieBindingConstants.PROPERTY_MAC_ADDRESS, gateway.getMacAddress());
        updateProperties(props);
    }

    @Override
    public void thingUpdated(Thing thing) {
        /*
         * The default behavior is not wanted here because disposing and
         * initializing the handler would result in a new device id.
         */
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Editing the thing configuration...");
        Configuration config = editConfiguration();
        BigDecimal updateInterval = ((BigDecimal) configurationParameters
                .get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
        config.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, updateInterval);
        this.updateInterval = updateInterval.longValue();
        updateConfiguration(config);
        scheduleUpdateTask(this.updateInterval);
    }

    public int getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public void handleRemoval() {
        logger.debug("About to remove gateway with id : {}", gatewayId);
        super.handleRemoval();
        for (Thing thing : getThing().getThings()) {
            // inform the subdevices about removed bridge
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler != null) {
                thingHandler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED).build());
            }
        }
    }

    /**
     * Lists all available subdevices registered on the Mi|Home servers
     *
     * @return array with subdevices or null if no devices are registered or the
     *         request isn't successful
     * @throws UnsuccessfulJsonResponseException
     * @throws IOException
     * @throws UnsuccessfulHttpResponseException
     */
    public JsonSubdevice[] listSubdevices()
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        return apiManager.listSubdevices();
    }

    /**
     * Gets data for a subdevice
     *
     * @param subdeviceID the ID of the subdevice
     * @return JsonObject containing the requested data or null if the request
     *         isn't successful
     * @throws UnsuccessfulJsonResponseException
     * @throws IOException
     * @throws UnsuccessfulHttpResponseException
     */
    public JsonSubdevice getSubdeviceData(int subdeviceID)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        return apiManager.showSubdeviceInfo(subdeviceID);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for the Mi|Home gateway with id: {}", gatewayId);
        if (updateTaskResult != null) {
            logger.debug("Stopping update task for gateway with ID {}", gatewayId);
            this.updateTaskResult.cancel(true);
            this.updateTaskResult = null;
        }
    }

}

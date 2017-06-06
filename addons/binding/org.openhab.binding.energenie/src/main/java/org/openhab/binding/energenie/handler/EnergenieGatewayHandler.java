/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.handler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonDevice;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandler;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandlerImpl;
import org.openhab.binding.energenie.internal.api.manager.ThingCallback;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the Mi|Home Gateway (MIHO001). The gateway is a central
 * communication hub between the individual devices.
 *
 * @author Mihaela Memova - Initial contribution
 * @author Svilen Valkanov - Added methods to get information about subdevices
 */
public class EnergenieGatewayHandler extends BaseBridgeHandler {

    // Warning messages used when some unexpected behavior is encountered
    private static final String UNREGISTERED_GATEWAY_MESSAGE = "The gateway has been unregistered";
    private static final String ALREADY_REGISTERED_GATEWAY_MESSAGE = "The gateway has already been registered";
    private static final String NOT_SUCCESSFULL_REGISTRATION_MESSAGE = "Registering new gateway was not successfull";
    private static final String NOT_ACTIVE_GATEWAY_MESSAGE = "The gateway has not been active in the last two minutes. Please check your connection.";
    private static final String CHANGING_LABEL_MESSAGE = "Mi|Home REST API does not allow to change gateway's label. Changing the thing's label will not affect the device label.";
    private static final String INVALID_USER_MESSAGE = "Invalid username. Email address expected.";
    private static final String INVALID_GATEWAY_CODE_MESSAGE = "Invalid gateway code. 10 capital letters and numbers expected";
    private static final String REJECTED_EXECUTION_OF_UPDATE_TASK_MESSAGE = "The update task cannot be scheduled for execution";
    private static final String UNSUCCESSFUL_DATE_PARSING_MESSAGE = "Could not get the 'inactive' time interval of the gateway. Something went wrong with the parsing.";
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

    /** Pattern used to check if a given String is valid gateway code */
    public static final String GATEWAY_CODE_PATTERN = "^[A-Z0-9]{10}$";

    /**
     * Default update interval in seconds
     */
    public static final BigDecimal DEFAULT_UPDATE_INTERVAL = new BigDecimal(30);

    private EnergenieApiConfiguration accountCredentials;

    /**
     * Gateway code (10 capital letters and numbers) written at the bottom of
     * the gateway device
     */
    private String gatewayCode;

    /** ID of the gateway device (received after registration) */
    private int gatewayId;

    /** Label of the gateway device */
    private String gatewayLabel;

    /** Instance of the helper class used for the requests */
    private EnergenieApiManager apiManager;

    /** Update interval in seconds */
    private long updateInterval;

    /** The result of scheduling the refresh thread */
    private ScheduledFuture<?> updateTaskResult;

    /** Determines if the gateway is unregistered using the Mi|Home API */
    private boolean gatewayAlreadyUnregistered;

    /** Used for logging */
    private final Logger logger = LoggerFactory.getLogger(EnergenieGatewayHandler.class);

    public EnergenieGatewayHandler(Bridge bridge, EnergenieApiConfiguration accountCredentials) {
        super(bridge);
        this.accountCredentials = accountCredentials;
        logger.debug("Created bridge for thing {}", getThing().getUID());
    }

    public boolean isGatewayAlreadyUnregistered() {
        return gatewayAlreadyUnregistered;
    }

    public void setGatewayAlreadyUnregistered(boolean gatewayAlreadyUnregistered) {
        this.gatewayAlreadyUnregistered = gatewayAlreadyUnregistered;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing EnergenieGatewayHandler...");
        gatewayLabel = thing.getLabel();
        Configuration config = getConfig();
        String registrationEmailAddress = accountCredentials.getUserName();
        gatewayCode = (String) config.get(EnergenieBindingConstants.CONFIG_GATEWAY_CODE);
        String password = accountCredentials.getPassword();
        BigDecimal interval = (BigDecimal) config.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL);
        this.updateInterval = interval.longValue();

        if (!isTextMatchingPattern(registrationEmailAddress, EMAIL_PATTERN)) {
            logger.warn(INVALID_USER_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, INVALID_USER_MESSAGE);
            return;
        }
        configureApiManager(registrationEmailAddress, password);

        if (gatewayCode != null && !isTextMatchingPattern(gatewayCode, GATEWAY_CODE_PATTERN)) {
            logger.warn(INVALID_GATEWAY_CODE_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, INVALID_GATEWAY_CODE_MESSAGE);
            return;
        }
        if (!isGatewayComingFromDiscoveryService(thing)) {
            if (!isGatewayThingAlreadyRegistered(thing)) {
                logger.debug("About to register a new gateway");
                registerNewGateway();
            } else {
                logger.warn(ALREADY_REGISTERED_GATEWAY_MESSAGE);
                updateStatus(ThingStatus.REMOVED);
            }
        } else {
            gatewayId = Integer.parseInt(thing.getProperties().get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));
        }
        updateStatus(ThingStatus.ONLINE);
        scheduleUpdateTask(this.updateInterval);
    }

    private boolean isGatewayComingFromDiscoveryService(Thing gatewayThing) {
        Map<String, String> props = gatewayThing.getProperties();
        return props.containsKey(EnergenieBindingConstants.PROPERTY_AUTH_CODE);
    }

    /**
     * Checks if there is already a registered thing for a given gateway device
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean isGatewayThingAlreadyRegistered(Thing thing) {
        ServiceReference ref = bundleContext.getServiceReference(ThingRegistry.class.getName());
        thingRegistry = (ThingRegistry) bundleContext.getService(ref);
        Collection<Thing> things = thingRegistry.getAll();
        for (Thing registeredThing : things) {
            ThingUID registeredThingUID = registeredThing.getUID();
            ThingTypeUID registeredThingTypeUID = registeredThing.getThingTypeUID();
            if (registeredThingTypeUID != null
                    && registeredThingTypeUID.equals(EnergenieBindingConstants.THING_TYPE_GATEWAY)) {
                Configuration config = registeredThing.getConfiguration();
                String code = (String) config.get(EnergenieBindingConstants.CONFIG_GATEWAY_CODE);
                if (gatewayCode != null && gatewayCode.equals(code) && (!thing.getUID().equals(registeredThingUID))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void registerNewGateway() {
        JsonGateway gateway = apiManager.registerGateway(getThing().getLabel(), gatewayCode);
        if (gateway != null) {
            gatewayId = gateway.getID();
            setProperties(gateway);
        } else {
            logger.warn(NOT_SUCCESSFULL_REGISTRATION_MESSAGE);
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
            logger.debug("Starting the refresh thread...");
            checkGateway();
        };
        try {
            scheduler.scheduleWithFixedDelay(refreshRunnable, INITIAL_REFRESH_DELAY_SECONDS, updateInterval,
                    TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    REJECTED_EXECUTION_OF_UPDATE_TASK_MESSAGE);
        }
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
        JsonGateway gateway = getGatewayById(gatewayId);
        if (gateway != null) {
            setProperties(gateway);
            gatewayLabel = gateway.getLabel();
            thing.setLabel(gatewayLabel);

            String lastSeen = gateway.getLastSeenAt();
            if (lastSeen != null) {
                handleCommand(thing.getChannel(EnergenieBindingConstants.CHANNEL_LAST_SEEN).getUID(),
                        DateTimeType.valueOf(lastSeen));
                handleLastSeenProperty(lastSeen);
            }
        }
    }

    /**
     * Searches in the list of all registered gateways and find the one with the
     * given id
     *
     * @param id
     *            - the id of the searched gateway
     * @return the gateway with the given id if it is registered or null if it
     *         is not
     */
    private JsonGateway getGatewayById(int searchedId) {
        JsonGateway[] gateways = apiManager.listGateways();
        if (gateways != null) {
            for (JsonGateway gateway : gateways) {
                int id = gateway.getID();
                if (id == searchedId) {
                    return gateway;
                }
            }
            logger.warn(UNREGISTERED_GATEWAY_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, UNREGISTERED_GATEWAY_MESSAGE);
            setGatewayAlreadyUnregistered(true);
            return null;
        }
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
                if (!isGatewayComingFromDiscoveryService(thing)) {
                    registerNewGateway();
                }
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    /**
     * Calculates the difference (in seconds) between two datetime stamps given
     * as Strings
     *
     * @param currentTimeStamp
     *            - current datetime stamp
     * @param previousTimeStamp
     *            - previous datetime stamp
     * @return the time that has elapsed between previousDateTime and
     *         currentDateTime (in seconds)
     * @throws ParseException
     *             - if any of the date Strings cannot be parsed correctly
     */
    private Long getTimeInterval(String currentTimeStamp, String previousTimeStamp) throws ParseException {
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnergenieBindingConstants.DATE_TIME_PATTERN);
        ZonedDateTime currentTime = ZonedDateTime.parse(currentTimeStamp, sdf);
        ZonedDateTime previousTime = ZonedDateTime.parse(previousTimeStamp, sdf);
        return Duration.between(previousTime, currentTime).getSeconds();
    }

    /**
     * Configures the {@link #apiManager}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void configureApiManager(String username, String password) {
        logger.debug("Configuring the API Manager for user: {}", username);
        ServiceReference ref = bundleContext.getServiceReference(RestClient.class.getName());
        RestClient client = (RestClient) bundleContext.getService(ref);

        EnergenieApiConfiguration restConfig = new EnergenieApiConfiguration(username, password);
        client.setConnectionTimeout(RestClient.DEFAULT_REQUEST_TIMEOUT);

        ThingCallback callback = new ThingCallback() {

            @Override
            public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
                updateStatus(status, statusDetail, description);
            }

            @Override
            public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail) {
                updateStatus(status, statusDetail);
            }

            @Override
            public void updateThingStatus(ThingStatus status) {
                updateStatus(status);
            }
        };
        FailingRequestHandler handler = new FailingRequestHandlerImpl(callback);
        apiManager = new EnergenieApiManagerImpl(restConfig, client, handler);
    }

    public EnergenieApiManager getEnergenieApiManager() {
        return apiManager;
    }

    /**
     * Verify whether a given text matches a certain pattern
     *
     * @param text
     *            - string to check
     * @param patternToMatch
     *            - pattern to match
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
     * @param dataObj
     *            - JSON object containing all the needed information
     */
    private void setProperties(JsonGateway gateway) {
        logger.debug("Setting the thing properties...");
        Map<String, String> props = editProperties();
        props.put(EnergenieBindingConstants.PROPERTY_PORT, Integer.toString(gateway.getPort()));
        props.put(EnergenieBindingConstants.PROPERTY_IP_ADDRESS, gateway.getIpAddress());
        props.put(EnergenieBindingConstants.PROPERTY_FIRMWARE_VERSION,
                Integer.toString(gateway.getFirmwareVersionID()));
        props.put(EnergenieBindingConstants.PROPERTY_TYPE, gateway.getType().toString());
        props.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(gateway.getID()));
        props.put(EnergenieBindingConstants.PROPERTY_USER_ID, Integer.toString(gateway.getUserID()));
        props.put(EnergenieBindingConstants.PROPERTY_MAC_ADDRESS, gateway.getMacAddress());
        updateProperties(props);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(EnergenieBindingConstants.CHANNEL_LAST_SEEN)) {
            if (command instanceof DateTimeType) {
                State lastSeenState = DateTimeType.valueOf(command.toString());
                updateState(channelUID, lastSeenState);
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        /*
         * The default behavior is not wanted here because disposing and
         * initializing the handler would result in a new device id.
         */
        String newLabel = thing.getLabel();
        if (!gatewayLabel.equals(newLabel)) {
            this.thing.setLabel(newLabel);
            logger.warn(CHANGING_LABEL_MESSAGE);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Editing the thing configuration...");
        Configuration config = editConfiguration();
        gatewayCode = (String) configurationParameters.get(EnergenieBindingConstants.CONFIG_GATEWAY_CODE);
        config.put(EnergenieBindingConstants.CONFIG_GATEWAY_CODE, gatewayCode);
        BigDecimal updateInterval = ((BigDecimal) configurationParameters
                .get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
        config.put(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL, updateInterval);
        this.updateInterval = updateInterval.longValue();
        updateConfiguration(config);

        if (gatewayCode != null && !isTextMatchingPattern(gatewayCode, GATEWAY_CODE_PATTERN)) {
            logger.warn(INVALID_GATEWAY_CODE_MESSAGE);
            System.out.println("Goes onffline");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, INVALID_GATEWAY_CODE_MESSAGE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        scheduleUpdateTask(this.updateInterval);
    }

    public int getGatewayId() {
        return gatewayId;
    }

    @Override
    public void handleRemoval() {
        logger.debug("About to unregister gateway with id : {}", gatewayId);
        JsonGateway deletionResponse = apiManager.unregisterGateway(gatewayId);
        if (deletionResponse == null && !isGatewayAlreadyUnregistered()) {
            logger.debug(
                    "Cannot unregister gateway with id: {}. Please try to unregister the device from the Mi|Home server and force remove the thing.",
                    gatewayId);
        } else {
            super.handleRemoval();
            for (Thing thing : getThing().getThings()) {
                // inform the subdevices about removed bridge
                thing.getHandler().bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build());
            }
        }
    }

    /**
     * Lists all available subdevices registered on the Mi|Home servers
     *
     * @return array with subdevices or null if no devices are registered or the
     *         request isn't successful
     */
    public JsonSubdevice[] listSubdevices() {
        return apiManager.listSubdevices();
    }

    /**
     * Initializes the pairing process between subdevice and a gateway.
     * Depending on the device the pairing could be initialized before or after
     * pushing the pairing button on the device.
     *
     * @param gatewayID
     *            - the ID of the gateway
     * @param deviceType
     *            - the device type, see {@link EnergenieDeviceTypes}
     * @return - true if the paring was initialized or false if the request
     *         isn't successful
     */
    public boolean initializePairing(int gatewayID, EnergenieDeviceTypes deviceType) {
        JsonDevice response = apiManager.registerSubdevice(gatewayID, deviceType);
        return response != null;

    }

    /**
     * Sends a request to the Mi|Home server to unregister a subdevice. A
     * success unfortunately doesn't guarantee that the device will be
     * unregistered. If the connection to the gateway is lost the request will
     * be successful, but it won't be completed when the connection is resumed
     *
     * @param subdeviceID
     *            - the ID of the subdevice to unregister
     * @return - true if the request is sent or false if it has failed
     */
    public boolean unregisterSubdevice(int subdeviceID) {
        JsonSubdevice response = apiManager.unregisterSubdevice(subdeviceID);
        return response != null;
    }

    /**
     * Gets a data for a subdevice
     *
     * @param subdeviceID
     *            - the ID of the subdevice
     * @return JsonObject containing the requested data or null if the request
     *         isn't successful
     */
    public JsonSubdevice getSubdeviceData(int subdeviceID) {
        return apiManager.showSubdeviceInfo(subdeviceID);
    }

    /**
     * Updates a label of the subdevice in the Mi|Home API
     *
     * @param subdeviceID
     *            - the ID of the subdevice to update
     * @param newLabel
     *            - the new label
     * @return null if the request was not successful, otherwise device data
     */
    public JsonSubdevice updateSubdevice(int subdeviceID, String newLabel) {
        return apiManager.updateSubdevice(subdeviceID, newLabel);
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

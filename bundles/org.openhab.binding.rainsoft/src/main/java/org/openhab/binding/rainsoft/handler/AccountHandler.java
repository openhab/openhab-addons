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
package org.openhab.binding.rainsoft.handler;

import static org.openhab.binding.rainsoft.RainSoftBindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.eclipse.jdt.annotation.Nullable;
import org.json.simple.parser.ParseException;
import org.openhab.binding.rainsoft.internal.data.WCS;
import org.openhab.binding.rainsoft.internal.ApiConstants;
import org.openhab.binding.rainsoft.internal.RestClient;
import org.openhab.binding.rainsoft.internal.RainSoftAccount;
import org.openhab.binding.rainsoft.internal.RainSoftDeviceRegistry;
import org.openhab.binding.rainsoft.internal.data.RainSoftDevice;
import org.openhab.binding.rainsoft.internal.data.RainSoftDevices;
import org.openhab.binding.rainsoft.internal.errors.AuthenticationException;
import org.openhab.binding.rainsoft.internal.errors.DuplicateIdException;
import org.openhab.binding.rainsoft.internal.utils.RainSoftUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;

/**
 * The {@link RainSoftDoorbellHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ben Rosenblum - Initial contribution
 */

public class AccountHandler extends AbstractRainSoftHandler implements RainSoftAccount {

    private ScheduledFuture<?> jobTokenRefresh = null;
    private ScheduledFuture<?> eventRefresh = null;
    private Runnable runnableToken = null;
    private Runnable runnableEvent = null;
    private @Nullable HttpService httpService;

    private String customerId = "";
    private String locations = "";
    /**
     * The auth token retrieved when authenticating.
     */
    private String authToken = "";
    /**
     * The registry.
     */
    private RainSoftDeviceRegistry registry;
    /**
     * The RestClient is used to connect to the RainSoft Account.
     */
    private RestClient restClient;

    private NetworkAddressService networkAddressService;

    private int httpPort;

    public AccountHandler(Thing thing, NetworkAddressService networkAddressService, HttpService httpService,
            int httpPort) {
        super(thing);
        this.httpPort = httpPort;
        this.networkAddressService = networkAddressService;
        this.httpService = httpService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
    }

    /**
     * Refresh the state of channels that may have changed by (re-)initialization.
     */
    @Override
    protected void refreshState() {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RainSoft Account handler");
        super.initialize();

        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        Integer refreshInterval = config.refreshInterval;
        String username = config.username;
        String password = config.password;

        try {
            Configuration updatedConfiguration = getThing().getConfiguration();

            restClient = new RestClient();
            logger.debug("Logging in with credentials: U:{} P:{}", RainSoftUtils.sanitizeData(username), RainSoftUtils.sanitizeData(password));
            this.authToken = restClient.getAuthenticatedProfile(username, password);
            this.customerId = restClient.getCustomerId(this.authToken);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");

            // Note: When initialization can NOT be done set the status with more details for further
            // analysis. See also class ThingStatusDetail for all available status details.
            // Add a description to give user information to understand why thing does not work
            // as expected. E.g.
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
            // "Can not access device as username and/or password are invalid");
            startAutomaticRefresh(refreshInterval);
            startSessionRefresh(refreshInterval);
        } catch (AuthenticationException ex) {
            logger.debug("AuthenticationException when initializing RainSoft Account handler{}", ex.getMessage());
            if (ex.getMessage().startsWith("Two factor")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        } catch (ParseException e) {
            logger.debug("Invalid response from rainsoft.com when initializing RainSoft Account handler{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from rainsoft.com");
        } catch (Exception e) {
            logger.debug("Initialization failed when initializing RainSoft Account handler{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Initialization failed: " + e.getMessage());
        }
    }

    private void refreshRegistry() throws ParseException, AuthenticationException, DuplicateIdException {
        int count = 0;
        String locations = restClient.getLocations(this.customerId,this.authToken);
        JSONArray array = ((JSONArray) new JSONParser().parse(locations));
        for (Object obj : array) {
                String id = ((JSONObject) obj).get("id").toString();
                String devices = ((JSONObject) obj).get("devices").toString();
                JSONArray devicesArray = ((JSONArray) new JSONParser().parse(devices));
                logger.debug("refreshRegistry - found location: {} devices: {}",id,devices);
                registry = RainSoftDeviceRegistry.getInstance();
                for(Object deviceObject : devicesArray) {
                      registry.addRainSoftDevice(new WCS((JSONObject) deviceObject));
                      count++;
                }
        }
        if ( count > 0 ) {
                registry.setInitialized(true);
        }
    }

    private void updateDevices() throws ParseException, AuthenticationException {
        logger.debug("AccountHandler - refreshDevices");
        for (RainSoftDevice device : registry.getRainSoftDevices(RainSoftDeviceRegistry.Status.CONFIGURED)) {
            String id = device.getId();
            String deviceInfo = restClient.getDevice(id,this.authToken);
            String waterUsage = restClient.getWaterUsage(id,this.authToken);
            String saltUsage = restClient.getSaltUsage(id,this.authToken);
            logger.trace("Account Handler - updateDevices - ID: {} Info: {} Water: {} Salt: {}",id,deviceInfo,waterUsage,saltUsage);
        }
    }

    @Override
    protected void minuteTick() {
        try {
            // Init the devices
            refreshRegistry();
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException | ParseException e) {
            logger.debug(
                    "AuthenticationException in AccountHandler.minuteTick() when trying refreshRegistry, attempting to reconnect {}",
                    e.getMessage());
            AccountConfiguration config = getConfigAs(AccountConfiguration.class);
            String username = config.username;
            String password = config.password;

            try {
                if ((authToken == null) || (authToken.equals(""))) {
                authToken = restClient.getAuthenticatedProfile(username, password);
                }
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
            } catch (AuthenticationException ex) {
                logger.debug("RestClient reported AuthenticationException trying getAuthenticatedProfile: {}",
                        ex.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid credentials");
            } catch (ParseException e1) {
                logger.debug("RestClient reported ParseException trying getAuthenticatedProfile: {}", e1.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from api.ring.com");
            } finally {
                try {
                    refreshRegistry();
                    updateStatus(ThingStatus.ONLINE);
                } catch (DuplicateIdException ignored) {
                    updateStatus(ThingStatus.ONLINE);
                } catch (AuthenticationException ae) {
                    registry = null;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "AuthenticationException response from rainsoft.com");
                    logger.debug("RestClient reported AuthenticationException in finally block: {}", ae.getMessage());
                } catch (ParseException pe1) {
                    registry = null;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "ParseException response from rainsoft.com");
                    logger.debug("RestClient reported ParseException in finally block: {}", pe1.getMessage());
                }
            }
        } catch (DuplicateIdException ignored) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Refresh the profile every 20 minutes
     */
    protected void startSessionRefresh(int refreshInterval) {
        logger.debug("startSessionRefresh {}", refreshInterval);
        runnableToken = new Runnable() {
            @Override
            public void run() {
                try {
                    if (restClient != null) {
                        if (registry != null) {
                            refreshRegistry();
                        }
                    }
                } catch (Exception e) {
                    logger.debug(
                            "AccountHandler - startSessionRefresh - Exception occurred during execution of refreshRegistry(): {}",
                            e.getMessage(), e);
                }
            }
        };

        runnableEvent = new Runnable() {
            @Override
            public void run() {
                try {
                    updateDevices();
                } catch (final Exception e) {
                    logger.debug(
                            "AccountHandler - startSessionRefresh - Exception occurred during execution of eventTick(): {}",
                            e.getMessage(), e);
                }
            }
        };

        jobTokenRefresh = scheduler.scheduleWithFixedDelay(runnableToken, 90, 600, TimeUnit.SECONDS);
        eventRefresh = scheduler.scheduleWithFixedDelay(runnableEvent, refreshInterval, refreshInterval,
                TimeUnit.SECONDS);
    }

    protected void stopSessionRefresh() {
        if (jobTokenRefresh != null) {
            jobTokenRefresh.cancel(true);
            jobTokenRefresh = null;
        }
        if (eventRefresh != null) {
            eventRefresh.cancel(true);
            eventRefresh = null;
        }
    }

    @Override
    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * Dispose off the refreshJob nicely.
     */
    @Override
    public void dispose() {
        stopSessionRefresh();
        super.dispose();
    }
}

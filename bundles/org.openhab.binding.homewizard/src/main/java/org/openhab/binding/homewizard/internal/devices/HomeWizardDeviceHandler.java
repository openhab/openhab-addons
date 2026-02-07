/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices;

import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.HomeWizardConfiguration;
import org.openhab.binding.homewizard.internal.devices.water_meter.HomeWizardWaterMeterMeasurementPayload;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomeWizardDeviceHandler} is a base class for all
 * HomeWizard devices. It provides configuration and polling of
 * data from a device. It also processes common data.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - changes to API calls and support for APi v2 (beta).
 *
 */
@NonNullByDefault
public abstract class HomeWizardDeviceHandler extends BaseThingHandler {

    private final String SYSTEM_URL = "system";
    private final String BATTERIES_URL = "batteries";

    private static final String BEARER = "Bearer";
    private static final String API_VERSION_HEADER = "X-Api-Version";
    private static final String CERTIFICATE_ALIAS = "caCert";
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String PRODUCT_NAME = "productName";
    private static final String PRODUCT_TYPE = "productType";
    private static final String FIRMWARE_VERSION = "firmwareVersion";
    private static final String API_VERSION = "apiVersion";

    protected static final int API_V1 = 1;
    protected static final int API_V2 = 2;

    protected final Logger logger = LoggerFactory.getLogger(HomeWizardDeviceHandler.class);
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected ScheduledExecutorService executorService = this.scheduler;
    protected HomeWizardConfiguration config = new HomeWizardConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;
    private HttpClient httpClient = new HttpClient();

    protected List<String> supportedTypes = new ArrayList<String>();
    protected List<Integer> supportedApiVersions = Arrays.asList(API_V1);
    private String apiURL = "";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardDeviceHandler(Thing thing) {
        super(thing);
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(HomeWizardConfiguration.class);

        if (config.isUsingApiVersion2()) {
            String caCertPath = "homewizard-ca-cert.pem";

            // Create an SSL context factory and set the CA certificate
            KeyStore keyStore = null;
            ClassLoader classloader = this.getClass().getClassLoader();
            if (classloader != null) {
                try {
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry(CERTIFICATE_ALIAS, CertificateFactory.getInstance(CERTIFICATE_TYPE)
                            .generateCertificate(classloader.getResourceAsStream(caCertPath)));
                } catch (Exception ex) {
                }

                SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
                sslContextFactory.setTrustStore(keyStore);
                sslContextFactory.setEndpointIdentificationAlgorithm(null);
                sslContextFactory.setHostnameVerifier((hostname, sslSession) -> true);
                httpClient = new HttpClient(sslContextFactory);
            }
        }

        if (configure() && processDeviceInformation()) {
            updateStatus(ThingStatus.UNKNOWN);
            pollingJob = executorService.scheduleWithFixedDelay(this::retrieveData, 0, config.refreshDelay,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-missing-host");
            return false;
        }
        if (!supportedApiVersions.contains(config.apiVersion)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-api-version");
            return false;
        }
        if (config.isUsingApiVersion2() && config.bearerToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-missing-bearer-token");
            return false;
        }

        if (config.isUsingApiVersion2()) {
            apiURL = String.format("https://%s/api/", config.ipAddress.trim());
        } else {
            apiURL = String.format("http://%s/api/", config.ipAddress.trim());
        }

        try {
            httpClient.setConnectTimeout(30000);
            httpClient.start();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-device-offline");
            logger.debug("Unable to reach device", ex);
            return false;
        }

        return true;
    }

    /**
     * Listening to commands for the system api.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        var cmd = "";

        switch (channelUID.getIdWithoutGroup()) {

            case HomeWizardBindingConstants.CHANNEL_SYSTEM_CLOUD_ENABLED: {
                boolean onOff = command.equals(OnOffType.ON);
                cmd = String.format("{\"cloud_enabled\": %b}", onOff);
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_SYSTEM_STATUS_LED_BRIGHTNESS: {
                cmd = String.format("{\"status_led_brightness_pct\": %s}", command.toFullString());
                break;
            }
            default: {
                logger.warn("Unhandled command for channel: {} command: {}", channelUID.getIdWithoutGroup(), command);
                return;
            }
        }

        sendSystemCommand(cmd);
    }

    /**
     * The actual polling loop
     */
    protected void retrieveData() {
        try {
            handleSystemData(getSystemData());
            handleMeasurementData(getMeasurementData());
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-device-offline");
            logger.debug("Unable to get data from the API", ex);
            return;
        }
    }

    protected String getApiUrl() {
        if (config.isUsingApiVersion2()) {
            return apiURL;
        } else {
            return apiURL + "v1/";
        }
    }

    private boolean processDeviceInformation() {
        String deviceInformation = "";

        try {
            deviceInformation = getDeviceInformationData();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-device-offline");
            logger.debug("Unable to get device information", ex);
            return false;
        }

        if (deviceInformation.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-no-data");
            return false;
        }

        var payload = gson.fromJson(deviceInformation, HomeWizardDeviceInformationPayload.class);

        if (payload == null) {
            return false;
        } else {
            if ("".equals(payload.getProductType())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-no-data");
                return false;
            }

            if (!supportedTypes.contains(payload.getProductType().toLowerCase(Locale.ROOT))) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "@text/offline.comm-error-device-not-compatible");
                return false;
            }

            updateProperty(PRODUCT_NAME, payload.getProductName());
            updateProperty(PRODUCT_TYPE, payload.getProductType());
            updateProperty(FIRMWARE_VERSION, payload.getFirmwareVersion());
            updateProperty(API_VERSION, payload.getApiVersion());

            return true;
        }
    }

    /**
     * dispose: stop the poller
     */
    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
        try {
            httpClient.stop();
        } catch (Exception ex) {
            logger.debug("Error stopping the http client: {}", ex.getMessage());
        }
    }

    /**
     *
     * Updates the state of the thing.
     *
     * @param groupID id of the channel, which was updated
     * @param channelID id of the channel, which was updated
     * @param state new state
     */
    public void updateState(String groupID, String channelID, State state) {
        if (!groupID.isEmpty()) {
            updateState(groupID + "#" + channelID, state);
        } else {
            updateState(channelID, state);
        }
    }

    /**
     * Device specific handling of the returned measurement data.
     *
     * @param payload The data obtained from the API call
     */
    protected void handleMeasurementData(String data) {
        if (!config.isUsingApiVersion2()) {
            // We're only interested in the Wi-Fi data and the water meter payload processes these data.
            HomeWizardWaterMeterMeasurementPayload payload = null;
            try {
                payload = gson.fromJson(data, HomeWizardWaterMeterMeasurementPayload.class);
            } catch (JsonSyntaxException ex) {
                logger.warn("Wi-Fi data is not available");
            }

            if (payload != null) {
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_WIFI_SSID, new StringType(payload.getWifiSsid()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_WIFI_RSSI, new DecimalType(payload.getWifiRssi()));
            }
        }
    }

    /**
     * Device specific handling of the returned batteries data.
     *
     * @param data The data obtained from the API call
     */
    protected void handleBatteriesData(String data) {
    }

    /**
     * Device specific handling of the returned system data.
     *
     * @param data The data obtained from the API call
     */
    protected void handleSystemData(String data) {
        HomeWizardSystemPayload payload = null;
        try {
            payload = gson.fromJson(data, HomeWizardSystemPayload.class);
        } catch (JsonSyntaxException ex) {
            logger.warn("No System data available");
        }
        if (payload != null) {
            if (config.isUsingApiVersion2()) {
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_WIFI_SSID, new StringType(payload.getWifiSsid()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_WIFI_RSSI, new DecimalType(payload.getWifiRssi()));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_UPTIME,
                        new QuantityType<>(payload.getUptime(), Units.SECOND));
                updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                        HomeWizardBindingConstants.CHANNEL_SYSTEM_STATUS_LED_BRIGHTNESS,
                        new DecimalType(payload.getStatusLedBrightness()));
            }
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                    HomeWizardBindingConstants.CHANNEL_SYSTEM_CLOUD_ENABLED, OnOffType.from(payload.isCloudEnabled()));
        }
    }

    protected ContentResponse putDataTo(String url, String data)
            throws InterruptedException, TimeoutException, ExecutionException {
        var request = httpClient.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(data));

        return sendRequest(request);
    }

    public ContentResponse getResponseFrom(String url)
            throws InterruptedException, TimeoutException, ExecutionException {
        return sendRequest(httpClient.newRequest(url));
    }

    private ContentResponse sendRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException {
        if (config.isUsingApiVersion2()) {
            request.header(HttpHeader.AUTHORIZATION, BEARER + " " + config.bearerToken);
            request.header(API_VERSION_HEADER, "2");
        }
        return request.timeout(20, TimeUnit.SECONDS).send();
    }

    /**
     * @return json response from the device information api
     * @throws InterruptedException, TimeoutException, ExecutionException, SecurityException
     */
    public String getDeviceInformationData()
            throws InterruptedException, TimeoutException, ExecutionException, SecurityException {
        var response = getResponseFrom(apiURL);
        if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            throw new SecurityException("Bearer token is invalid.");
        }
        return response.getContentAsString();
    }

    /**
     * @return json response from the system api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getSystemData() throws InterruptedException, TimeoutException, ExecutionException {
        var response = getResponseFrom(getApiUrl() + SYSTEM_URL);
        if (response.getStatus() == HttpStatus.OK_200) {
            return response.getContentAsString();
        } else {
            logger.warn("No System data available");
            return "";
        }
    }

    public void sendSystemCommand(String command) {
        var url = getApiUrl() + SYSTEM_URL;
        try {
            var response = putDataTo(url, command);
            if (response.getStatus() == HttpStatus.OK_200) {
                handleSystemData(response.getContentAsString());
            } else {
                logger.warn("Failed to send command {} to {}", command, url);
            }
        } catch (Exception ex) {
            logger.warn("Failed to send command {} to {}", command, url);
            logger.debug("Error sending command", ex);
        }
    }

    /**
     * @return json response from the measurement api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getMeasurementData() throws InterruptedException, TimeoutException, ExecutionException {
        var url = getApiUrl();
        if (config.isUsingApiVersion2()) {
            url += "measurement";
        } else {
            url += "data";
        }
        return getResponseFrom(url).getContentAsString();
    }

    /**
     * @return json response from the batteries api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getBatteriesData() throws InterruptedException, TimeoutException, ExecutionException {
        var response = getResponseFrom(getApiUrl() + BATTERIES_URL);
        if (response.getStatus() == HttpStatus.OK_200) {
            return response.getContentAsString();
        } else {
            logger.warn("No Batteries data available");
            return "";
        }
    }

    protected void sendBatteriesCommand(String command) {
        var url = getApiUrl() + BATTERIES_URL;
        try {
            var response = putDataTo(url, command);
            if (response.getStatus() == HttpStatus.OK_200) {
                handleBatteriesData(response.getContentAsString());
            } else {
                logger.warn("Failed to send command {} to {}", command, url);
            }
        } catch (Exception ex) {
            logger.debug("Failed to send command {} to {}", command, url);
            logger.debug("Error sending command", ex);
        }
    }
}

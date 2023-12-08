package org.openhab.binding.salus.internal.handler;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.JettyHttpClient;
import org.openhab.binding.salus.internal.rest.SalusApi;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.SortedSet;
import java.util.concurrent.ScheduledFuture;

import static java.util.Collections.emptySortedSet;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.RefreshType.REFRESH;

public final class CloudBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class.getName());
    private final HttpClientFactory httpClientFactory;
    private String username;
    private char[] password;
    private String url;
    private long refreshInterval;
    private SalusApi salusApi;
    private ScheduledFuture<?> scheduledFuture;

    public CloudBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = requireNonNull(httpClientFactory, "httpClientFactory");
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
            updateStatus(ONLINE);
        } catch (Exception ex) {
            logger.error("Cannot start server!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cannot start server! " + ex.getMessage());
        }
    }

    private void internalInitialize() {
        loadConfigs();
        var missingUsername = StringUtils.isEmpty(username);
        var missingPassword = password == null || password.length == 0;
        var missingUrl = StringUtils.isEmpty(url);
        if (missingUsername || missingPassword || missingUrl) {
            var sb = new StringBuilder();
            sb.append("Missing configuration!\n");
            sb.append(missingUsername ? "❌" : "✅").append(" username\n");
            sb.append(missingPassword ? "❌" : "✅").append(" password\n");
            sb.append(missingUrl ? "❌" : "✅").append(" url\n");
            sb.append("Please check your configuration!\n");
            logger.error(sb.toString());
            updateStatus(OFFLINE, CONFIGURATION_ERROR, sb.toString());
            return;
        }
        var httpClient = new JettyHttpClient(httpClientFactory.getCommonHttpClient());
        if (salusApi != null) {
            logger.warn("At this point SalusApi should be null!");
        }
        salusApi = new SalusApi(username, password, url, httpClient, new Gson());
        logger = LoggerFactory.getLogger(CloudBridgeHandler.class.getName() + "[" + username.replaceAll("\\.", "_") + "]");
        try {
            var devices = salusApi.findDevices();
        } catch (Exception ex) {
            var msg = "Cannot connect to Salus Cloud! Probably username/password mismatch!";
            logger.error(msg, ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, msg + " " + ex.getMessage());
            return;
        }
        var scheduledPool = ThreadPoolManager.getScheduledPool("Salus");
        this.scheduledFuture = scheduledPool.scheduleWithFixedDelay(
                this::refreshCloudDevices,
                refreshInterval * 2,
                refreshInterval,
                SECONDS);

        // done
        updateStatus(ONLINE);
    }

    private void loadConfigs() {
        var config = this.getConfig();
        username = (String) config.get("username");
        password = ((String) config.get("password")).toCharArray();
        url = (String) config.get("url");
        if (StringUtils.isEmpty(url)) {
            url = "https://eu.salusconnect.io";
        }
        refreshInterval = ((BigDecimal) config.get("refreshInterval")).longValue();
    }

    private void refreshCloudDevices() {
        logger.debug("Refreshing devices from CloudBridgeHandler");

        if (!(thing instanceof Bridge bridge)) {
            logger.debug("No bridge, refresh cancelled");
            return;
        }
        var things = bridge.getThings();
        for (var thing : things) {
            if (!thing.isEnabled()) {
                logger.debug("Thing {} is disabled, refresh cancelled", thing.getUID());
                continue;
            }
            var handler = thing.getHandler();
            if (handler == null) {
                logger.debug("No handler for thing {} refresh cancelled", thing.getUID());
                continue;
            }
            var channels = thing.getChannels();
            for (var channel : channels) {
                handler.handleCommand(channel.getUID(), REFRESH);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands in this bridge
        logger.debug("Bridge does not support any commands to any channels. channelUID={}, command={}", channelUID, command);
    }

    @Override
    public void dispose() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.dispose();
    }

    public SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn) {
        if (salusApi == null) {
            logger.error("Cannot find properties for device {} because salusClient is null", dsn);
            return emptySortedSet();
        }
        logger.debug("Finding properties for device {} using salusClient", dsn);
        var response = salusApi.findDeviceProperties(dsn);
        if (response.failed()) {
            logger.error("Cannot find properties for device {} using salusClient\n{}", dsn, response.error());
            return emptySortedSet();
        }
        return response.body();
    }

    public SalusApi getSalusApi() {
        return salusApi;
    }
}

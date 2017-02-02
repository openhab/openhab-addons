package org.openhab.binding.nest.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.handler.NestThermostatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestBridgeHandler extends BaseBridgeHandler {
    /** The url to use to connect to nest with. */
    private final static String NEST_URL = "https://developer-api.nest.com/";

    private Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);

    // Will refresh the data each time it runs.
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };

    private ScheduledFuture<?> pollingJob;

    public NestBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize the Nest bridge handler");

        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        startAutomaticRefresh(config.refreshInterval);
    }

    @Override
    public void dispose() {
        logger.debug("Nest bridge disposed");
        stopAutomaticRefresh();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            refreshData();
        }
    }

    /**
     * Read the data from nest and then parse it into something useful.
     */
    private void refreshData() {
        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        try {
            String uri = buildQueryString(config);
            String data = jsonFromGetUrl(uri);
            // Now convert the incoming data into something more useful.

        } catch (URIException e) {
            logger.error("Error parsing nest url", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error connecting to nest", e);
        }

    }

    private String buildQueryString(NestBridgeConfiguration config) throws URIException {
        StringBuilder urlBuilder = new StringBuilder(NEST_URL);
        urlBuilder.append("?auth=");
        urlBuilder.append(config.pincode);
        return URIUtil.encodeQuery(urlBuilder.toString());
    }

    private String jsonFromGetUrl(final String url) throws IOException {
        logger.debug("connecting to " + url);
        return HttpUtil.executeUrl(HttpMethod.GET.toString(), url, 120);
    }

    private synchronized void startAutomaticRefresh(int refreshInterval) {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }
}
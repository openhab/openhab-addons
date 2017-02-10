package org.openhab.binding.nest.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.discovery.NestDiscoveryService;
import org.openhab.binding.nest.internal.NestAccessToken;
import org.openhab.binding.nest.internal.NestDeviceAddedListener;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.NestDevices;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NestBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private List<NestDeviceAddedListener> listeners = new ArrayList<NestDeviceAddedListener>();

    // Will refresh the data each time it runs.
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };

    private ScheduledFuture<?> pollingJob;
    private NestAccessToken accessToken;
    private List<NestUpdateRequest> nestUpdateRequests = new ArrayList<>();
    private TopLevelData lastDataQuery;

    public NestBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize the Nest bridge handler");

        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        startAutomaticRefresh(config.refreshInterval);
        accessToken = new NestAccessToken(config);
        logger.debug("Client Id       {}.", config.clientId);
        logger.debug("Client Secret   {}.", config.clientSecret);
        logger.debug("Pincode         {}.", config.pincode);
        try {
            logger.debug("Access Token    {}.", accessToken.getAccessToken());
        } catch (IOException e) {
            logger.debug("Error getting Access Token.", e);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Starting poll query");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.info("Config update");
        super.handleConfigurationUpdate(configurationParameters);
        accessToken = new NestAccessToken(getConfigAs(NestBridgeConfiguration.class));

        try {
            logger.debug("New Access Token {}.", accessToken.getAccessToken());
        } catch (IOException e) {
            logger.debug("Error getting Access Token.", e);
        }
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

    @Override
    public void childHandlerInitialized(ThingHandler handler, Thing thing) {
        // Called when a new thing is created.
    }

    @Override
    public void childHandlerDisposed(ThingHandler handler, Thing thing) {
        // Called when a thing is disposed.
    }

    /**
     * Read the data from nest and then parse it into something useful.
     */
    private void refreshData() {
        String uri = "unknown";
        logger.debug("starting refreshData");
        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        try {
            uri = buildQueryString(config);
            String data = jsonFromGetUrl(uri, config);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Received update from nest");
            // Now convert the incoming data into something more useful.
            GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Gson gson = builder.create();
            TopLevelData newData = gson.fromJson(data, TopLevelData.class);
            if (newData != null) {
                lastDataQuery = newData;
            } else {
                newData = lastDataQuery;
            }
            // Turn this new data into things and stuff.
            compareThings(newData.getDevices());
            compareStructure(newData.getStructures().values());
        } catch (URIException e) {
            logger.error("Error parsing nest url", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error parsing nest url");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error connecting to nest " + uri, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error connecting to nest");
        } catch (Exception e) {
            logger.error("Error parsing data " + uri, e);
        }

    }

    private Thing getDevice(String deviceId, List<Thing> things) {
        for (Thing thing : things) {
            String thingDeviceId = thing.getUID().getId();
            if (thingDeviceId.equals(deviceId)) {
                return thing;
            }
        }
        return null;
    }

    private void compareThings(NestDevices devices) {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();

        for (Thermostat thermostat : devices.getThermostats().values()) {
            Thing thingThermostat = getDevice(thermostat.getDeviceId(), things);
            if (thingThermostat != null) {
                NestThermostatHandler handler = (NestThermostatHandler) thingThermostat.getHandler();
                handler.updateThermostat(thermostat);
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.info("Found new thermostat " + thermostat.getDeviceId());
                    listener.onThermostatAdded(thermostat);
                }
            }
        }
        for (Camera camera : devices.getCameras().values()) {
            Thing thingCamera = getDevice(camera.getDeviceId(), things);
            if (thingCamera != null) {
                NestCameraHandler handler = (NestCameraHandler) thingCamera.getHandler();
                handler.updateCamera(camera);
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.info("Found new camera." + camera.getDeviceId());
                    listener.onCameraAdded(camera);
                }
            }
        }
    }

    private void compareStructure(Collection<Structure> structures) {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();

        for (Structure struct : structures) {
            Thing thingStructure = getDevice(struct.getStructureId(), things);
            if (thingStructure != null) {
                NestStructureHandler handler = (NestStructureHandler) thingStructure.getHandler();
                handler.updateStructure(struct);
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.info("Found new structure " + struct.getStructureId());
                    listener.onStructureAdded(struct);
                }
            }

        }
    }

    private String buildQueryString(NestBridgeConfiguration config) throws URIException, IOException {
        logger.info("Making url " + config.accessToken == null ? "null" : config.accessToken);
        StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_URL);
        urlBuilder.append("?auth=");
        String stringAccessToken;
        if (config.accessToken == null) {
            stringAccessToken = accessToken.getAccessToken();
            // Update the configuration and persist to the database.
            Configuration configuration = editConfiguration();
            configuration.put("accessToken", stringAccessToken);
            updateConfiguration(configuration);
        } else {
            stringAccessToken = config.accessToken;
        }
        urlBuilder.append(stringAccessToken);
        logger.info("Made url " + urlBuilder.toString());
        return URIUtil.encodeQuery(urlBuilder.toString());
    }

    private String jsonFromGetUrl(final String url, NestBridgeConfiguration config) throws IOException {
        logger.info("connecting to " + url);
        return HttpUtil.executeUrl("GET", url, 30000 /* (config.refreshInterval - 10) * 1000 */);
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

    public void addDeviceAddedListener(NestDeviceAddedListener nestDiscoveryService) {
        this.listeners.add(nestDiscoveryService);
    }

    public void removeDeviceAddedListener(NestDiscoveryService nestDiscoveryService) {
        this.listeners.remove(nestDiscoveryService);
    }

    /** Adds the update request into the queue for doing something with, send immediately if the queue is empty. */
    public void addUpdateRequest(NestUpdateRequest request) {
        nestUpdateRequests.add(request);
    }

    public void startDiscoveryScan() {
        refreshData();
    }
}

package org.openhab.binding.homepilot.handler;

import java.util.Hashtable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.homepilot.discovery.HomePilotDeviceDiscoveryService;
import org.openhab.binding.homepilot.internal.HomePilotConfig;
import org.openhab.binding.homepilot.internal.HomePilotGateway;
import org.openhab.binding.homepilot.internal.HomePilotGatewayFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePilotBridgeHandler extends BaseBridgeHandler {

    private static final Logger logger = LoggerFactory.getLogger(HomePilotBridgeHandler.class);

    private HomePilotConfig config;
    private HomePilotGateway gateway;

    private HomePilotDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    private ScheduledFuture<?> refreshJob;

    public HomePilotBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.info("initialize");
        // try {
        String id = getThing().getUID().getId();
        config = createConfig();

        gateway = HomePilotGatewayFactory.createGateway(id, config, this);
        gateway.initialize();

        registerDeviceDiscoveryService();
        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();
                updateStatus(ThingStatus.ONLINE);
                for (Thing hmThing : getThing().getThings()) {
                    hmThing.getHandler().thingUpdated(hmThing);
                }
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    for (Thing thing : getThing().getThings()) {
                        ((HomePilotThingHandler) thing.getHandler()).refresh();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        refreshJob = scheduler.scheduleAtFixedRate(runnable, 10, 10, TimeUnit.SECONDS);
        //
        // } catch (IOException ex) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        // dispose();
        // // scheduleReinitialize();
        // }
        super.initialize();
    }

    private void registerDeviceDiscoveryService() {
        if (bundleContext != null) {
            logger.info("Registering HomePilotDeviceDiscoveryService for bridge '{}'", getThing().getUID().getId());
            discoveryService = new HomePilotDeviceDiscoveryService(this);
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<String, Object>());
            discoveryService.activate();
        }
    }

    private HomePilotConfig createConfig() {
        HomePilotConfig config = getThing().getConfiguration().as(HomePilotConfig.class);
        return config;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("handleCommand " + channelUID + "; " + command);
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        super.handleUpdate(channelUID, newState);
        logger.info("handleUpdate " + channelUID + "; " + newState);
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
        logger.info("thingUpdated " + thing);
    }

    public HomePilotGateway getGateway() {
        return gateway;
    }

    public void setOfflineStatus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        if (discoveryServiceRegistration != null && bundleContext != null) {
            bundleContext.ungetService(discoveryServiceRegistration.getReference());
        }
        super.dispose();
    }

}

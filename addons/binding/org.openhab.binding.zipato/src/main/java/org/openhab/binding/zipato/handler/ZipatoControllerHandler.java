package org.openhab.binding.zipato.handler;

import static org.openhab.binding.zipato.ZipatoBindingConstants.*;

import java.util.Hashtable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zipato.discovery.ZipatoDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itth.zipatoclient.Zipato;
import com.itth.zipatoclient.zipatoResponse.UserSession;

public class ZipatoControllerHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(ZipatoControllerHandler.class);
    private ZipatoDeviceDiscoveryService discoveryService;
    private ServiceRegistration discoveryRegistration;
    private Zipato zipato;
    private ScheduledFuture<?> mediumPriorityTasks;
    private final AtomicBoolean checkInProgress = new AtomicBoolean(false);

    public Zipato getZipato() {
        return zipato;
    }

    public void setZipato(Zipato zipato) {
        this.zipato = zipato;
    }

    public ZipatoControllerHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    private void checkConnection() {
        if (checkInProgress.compareAndSet(false, true)) {
            try {
                if (zipato == null || !zipato.isConnected()) {
                    try {
                        if (zipato != null) {
                            zipato.doLogout();
                        }
                    } catch (Exception ignored) {
                    }
                    updateStatus(ThingStatus.INITIALIZING);
                    logger.debug("Initializing Zipato Controller.");
                    Object param;
                    param = getConfig().get("username");
                    String username = String.valueOf(param);
                    param = getConfig().get("password");
                    String password = String.valueOf(param);
                    param = getConfig().get("url");
                    String url = String.valueOf(param);
                    param = getConfig().get("path");
                    String path = String.valueOf(param);
                    try {
                        param = getConfig().get("poll");
                        CONFIGURATION_POLLTIME_S = (int) Double.parseDouble(String.valueOf(param));
                    } catch (Exception e1) {
                        logger.warn("could not read poll time", e1);
                    }
                    logger.info("Zipato initializer: " + username + "@" + url + "/" + path);
                    try {
                        Zipato zipato = new Zipato(username, password, url, path);
                        UserSession userSession = zipato.doLogin().get();
                        if (userSession.isSuccess()) {
                            updateStatus(ThingStatus.ONLINE);
                            this.zipato = zipato;
                        } else {
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    } catch (Exception e) {
                        logger.error("failed create connection to Zipato device", e);
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } finally {
                checkInProgress.set(false);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Schedule update at fixed rate {} s.", CONFIGURATION_CONNECT_TIME_S);
        mediumPriorityTasks = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkConnection();
            }
        }, 10, CONFIGURATION_CONNECT_TIME_S, TimeUnit.SECONDS);
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    discoveryService = new ZipatoDeviceDiscoveryService(ZipatoControllerHandler.this);
                    bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                            new Hashtable<String, Object>());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    logger.error("could not create discovery service", e);
                }
            }
        }.start();
    }

    @Override
    public void dispose() {
        super.dispose();
        mediumPriorityTasks.cancel(true);
        updateStatus(ThingStatus.OFFLINE);
        new Thread() {
            @Override
            public void run() {
                if (zipato != null) {
                    zipato.doLogout();
                }
            }
        }.start();
    }

}

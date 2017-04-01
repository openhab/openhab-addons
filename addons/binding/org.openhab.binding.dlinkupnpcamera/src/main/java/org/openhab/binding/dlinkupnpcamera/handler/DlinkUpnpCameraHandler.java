/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinkupnpcamera.handler;

import static org.openhab.binding.dlinkupnpcamera.DlinkUpnpCameraBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.dlinkupnpcamera.config.DlinkUpnpCameraConfiguration;
import org.openhab.io.net.actions.Ping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DlinkUpnpCameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Yacine Ndiaye
 * @author Antoine Blanc
 * @author Christopher Law
 */
public class DlinkUpnpCameraHandler extends BaseThingHandler implements UpnpIOParticipant {

    private static final int DEFAULT_REFRESH_INTERVAL = 30;
    private static final int PING_TIMEOUT = 20;

    private String hostname;

    private Logger logger = LoggerFactory.getLogger(DlinkUpnpCameraHandler.class);

    private UpnpIOService service;
    private ScheduledFuture<?> pollingJob;

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                logger.debug("Polling...");

                // First check if the camera is set in the UPnP service registry
                // If not, set the thing state to OFFLINE and wait for the next poll
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }

                // Check if the camera can be joined
                // If not, set the thing state to OFFLINE and do nothing else
                updatePlayerState();
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    return;
                }
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    @Override
    public void initialize() {

        logger.debug("initializing handler for thing {}", getThing().getUID());

        if (getUDN() != null) {
            updateStatus(ThingStatus.ONLINE);
            onUpdate();
            hostname = getThing().getProperties().get(DlinkUpnpCameraConfiguration.IP);
            super.initialize();
            logger.debug("Camera initialized.");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            logger.warn("Cannot initalize the camera. UDN not set.");
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    public DlinkUpnpCameraHandler(Thing thing, UpnpIOService upnpIOService,
            DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);

        logger.debug("Creating a DlinkUpnpCameraHandler for thing '{}'", getThing().getUID());
        if (upnpIOService != null) {
            this.service = upnpIOService;
        }
    }

    @Override
    public String getUDN() {
        return getConfigAs(DlinkUpnpCameraConfiguration.class).udn;
    }

    private String getUsername() {
        return getConfigAs(DlinkUpnpCameraConfiguration.class).username;
    }

    private String getPassword() {
        return getConfigAs(DlinkUpnpCameraConfiguration.class).password;
    }

    private String getCommandRequest() {
        return getConfigAs(DlinkUpnpCameraConfiguration.class).commandRequest;
    }

    private String getImageRequest() {
        return getConfigAs(DlinkUpnpCameraConfiguration.class).imageRequest;
    }

    private void updatePlayerState() {
        try {
            refreshImage();
            boolean isAlive = Ping.checkVitality(hostname, 0, PING_TIMEOUT);
            if (isAlive) {
                if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                    logger.debug("Camera {} is connected to local network", getUDN());
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                if (!ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                    logger.debug("Camera {} is disconnected from local network", getUDN());
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        } catch (IOException e) {
            logger.debug("couldn't establish network connection [host '{}']", new Object[] { hostname });
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case IMAGE:
                refreshImage(command);
                break;
            case PAN:
                try {
                    sendControlCommand(command, 20, 0);
                } catch (FileNotFoundException e) {
                    logger.debug("Command not supported by the camera");
                }
                break;
            case TILT:
                try {
                    sendControlCommand(command, 0, 10);
                } catch (FileNotFoundException e) {
                    logger.debug("Command not supported by the camera");
                }
                break;
            default:
                // Nothing is done
        }
    }

    private void sendControlCommand(Command command, int x, int y) throws FileNotFoundException {
        if (command instanceof NextPreviousType || command instanceof PlayPauseType) {
            switch (command.toString()) {
                case "NEXT":
                    sendHttpRequest(buildCommandUrl(hostname, x, y));
                    break;
                case "PREVIOUS":
                    sendHttpRequest(buildCommandUrl(hostname, -x, -y));
                    break;
                case "PLAY":
                    sendHttpRequest(buildPatrolUrl(hostname));
                    break;
                case "PAUSE":
                    sendHttpRequest(buildStopUrl(hostname));
                    break;
                default:
                    // Nothing is done
            }
        }
    }

    private void refreshImage() {
        URL url = null;
        try {
            url = new URL(buildImageUrl(hostname));
        } catch (MalformedURLException e) {
            logger.debug("Can't create the URL");
        }
        try {
            updateState(IMAGE, new RawType(readImage(url).toByteArray()));
        } catch (IOException e) {
            logger.debug("Can't update the image");
        }
    }

    private void refreshImage(Command command) {
        if (command instanceof RefreshType) {
            refreshImage();
        }
    }

    private ByteArrayOutputStream readImage(URL url) throws IOException {
        // authentication for network connection
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword().toCharArray());
            }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;

        is = url.openStream();
        byte[] byteArray = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(byteArray)) > 0) {
            baos.write(byteArray, 0, bytesRead);
        }
        if (is != null) {
            is.close();
        }

        return baos;
    }

    private void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            DlinkUpnpCameraConfiguration config = getConfigAs(DlinkUpnpCameraConfiguration.class);
            // use default if not specified
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            if (config.connectionRefresh != null) {
                refreshInterval = config.connectionRefresh.intValue();
            }
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(boolean status) {
        // TODO Auto-generated method stub

    }

    private void sendHttpRequest(String string_url) {
        // authentication for network connection
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword().toCharArray());
            }
        });

        URL url = null;
        try {
            url = new URL(String.format(string_url));
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.getInputStream();
        } catch (IOException e) {
            logger.debug("Request failed");
        }
    }

    private String buildCommandUrl(String hostname, int x, int y) {
        return "http://" + hostname + getCommandRequest() + "=set_relative_pos&posX=" + x + "&posY=" + y;
    }

    private String buildPatrolUrl(String hostname) {
        return "http://" + hostname + getCommandRequest() + "=pan_patrol";
    }

    private String buildStopUrl(String hostname) {
        return "http://" + hostname + getCommandRequest() + "=stop";
    }

    private String buildImageUrl(String hostname) {
        return "http://" + hostname + getImageRequest();
    }
}
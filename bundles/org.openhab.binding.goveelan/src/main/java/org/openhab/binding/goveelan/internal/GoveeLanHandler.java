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
package org.openhab.binding.goveelan.internal;

import static org.openhab.binding.goveelan.internal.GoveeLanBindingConstants.BRIGHTNESS;
import static org.openhab.binding.goveelan.internal.GoveeLanBindingConstants.COLOR;
import static org.openhab.binding.goveelan.internal.GoveeLanBindingConstants.SWITCH;
import static org.openhab.binding.goveelan.internal.GoveeLanBindingConstants.TEMPERATUR_ABS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.goveelan.internal.model.Color;
import org.openhab.binding.goveelan.internal.model.StatusMessage;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link GoveeLanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Any device has its own job that triggers a refresh of retrieving the external state from the device.
 * However, there must be only one job that listens for all devices in a singleton thread because
 * all devices send their udp packet response to the same port on openHAB. Based on the sender IP address
 * of the device we can detect to which thing the status answer needs to be assigned to and updated.
 *
 * <ul>
 * <li>The job per thing that triggers a new update is called <i>triggerStatusJob</i>. There are as many instances
 * as things.</li>
 * <li>The job that receives the answers and applies that to the respective thing is called <i>refreshStatusJob</i> and
 * there is only one for all instances. It may be stopped and restarted by the DiscoveryService (see below).</li>
 * </ul>
 *
 * The other topic that needs to be managed is that device discovery responses are also sent to openHAB at the same port
 * like status updates. Therefore, when scanning new devices that job that listens to status devices must
 * be stopped while scanning new devices. Otherwise, the status job will receive the scan discover UDB packages.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeLanHandler extends BaseThingHandler {

    /*
     * Messages to be sent to the Govee Devices
     */
    private static final String LIGHT_OFF = "{\"msg\": {\"cmd\": \"turn\", \"data\": {\"value\": \"0\"}}}";
    // turning on via cmd-turn and value = 1 doesn't work, so let's use the brightness command
    private static final String LIGHT_ON = """
            {"msg": {"cmd": "brightness", "data": {"value": "100"}}}
            """;
    private static final String LIGHT_COLOR = """
            {
              "msg" : {
                "cmd":"colorwc",
                "data": {
                  "color" : {
                    "r" : %d,
                    "g" : %d,
                    "b":  %d
                  },
                  "colorTemInKelvin" : %d
                }
              }
            }
            """;
    private static final String LIGHT_BRIGHTNESS = """
            {
              "msg":{
                "cmd":"brightness",
                "data":{
                  "value": %d
                }
              }
            }
            """;

    private static final String QUERY_STATUS = """
            {
              "msg":{
                "cmd":"devStatus",
                "data":{
                }
              }
            }
            """;

    // Holds a list of all thing handlers to send them thing updates via the receiver-Thread
    private static final Map<String, GoveeLanHandler> THING_HANDLERS = new HashMap<>();

    private final Logger LOGGER = LoggerFactory.getLogger(GoveeLanHandler.class);
    private static final int SENDTODEVICE_PORT = 4003;
    private static final int RECEIVEFROMDEVICE_PORT = 4002;

    // Semaphores to suppress further processing if already running
    private static boolean refreshJobRunning = false;
    private static boolean refreshRunning = false;

    @Nullable
    private static ScheduledFuture<?> refreshStatusJob; // device response receiver job
    @Nullable
    private ScheduledFuture<?> triggerStatusJob; // send device status update job

    /*
     * Common Receiver job for the status answers of the devices
     */
    public static boolean isRefreshJobRunning() {
        return refreshJobRunning && THING_HANDLERS.isEmpty();
    }

    private static final Runnable REFRESH_STATUS_RECEIVER = () -> {
        final Logger LOGGER = LoggerFactory.getLogger(GoveeLanHandler.class);
        /**
         * This thread receives an answer from any device.
         * Therefore it needs to apply it to the right thing
         */
        // Discovery uses the same response code, so we must not refresh the status during discovery
        if (GoveeLanDiscoveryService.isDiscoveryActive()) {
            LOGGER.debug("Not running refresh as Scan is currently active");
        }

        refreshJobRunning = true;
        LOGGER.trace("REFRESH: running refresh cycle for {} devices", THING_HANDLERS.size());

        if (THING_HANDLERS.isEmpty()) {
            return;
        }

        try (MulticastSocket socket = new MulticastSocket(RECEIVEFROMDEVICE_PORT)) {
            byte[] buffer = new byte[10240];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.setReuseAddress(true);
            LOGGER.debug("waiting for Status");
            socket.receive(packet);

            String response = new String(packet.getData()).trim();
            String deviceIPAddress = packet.getAddress().toString().replace("/", "");
            LOGGER.trace("received = {} from {}", response, deviceIPAddress);

            GoveeLanHandler thingHandler = THING_HANDLERS.get(deviceIPAddress);
            if (thingHandler == null) {
                LOGGER.warn("thing Handler for {} couldn't be found.", deviceIPAddress);
                return;
            }

            LOGGER.debug("updating status for thing {} ", thingHandler.getThing().getLabel());
            LOGGER.trace("Response from {} = {}", deviceIPAddress, response);

            Gson gson = new Gson();
            StatusMessage statusMessage = gson.fromJson(response, StatusMessage.class);
            if (statusMessage != null) {
                thingHandler.updateDeviceState(statusMessage);
            } else {
                LOGGER.warn("status message is null");
            }
        } catch (IOException e) {
            LOGGER.error("exception when receiving status packet {}", Arrays.toString(e.getStackTrace()));
        } finally {
            refreshJobRunning = false;
        }
    };

    /**
     * This thing related job <i>thingRefreshSender</i> triggers an update to the Govee device.
     * The device sends it back to the common port and the response is
     * then received by the common #refreshStatusReceiver
     */
    private final Runnable thingRefreshSender = () -> {
        try {
            triggerDeviceStatusRefresh();
            if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control/query device at IP address "
                            + thing.getConfiguration().getProperties().get(GoveeLanConfiguration.IPADDRESS));
        }
    };

    public GoveeLanHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        GoveeLanConfiguration goveeLanConfiguration = getConfigAs(GoveeLanConfiguration.class);
        updateStatus(ThingStatus.ONLINE);

        String ipAddress = thing.getConfiguration().getProperties().get(GoveeLanConfiguration.IPADDRESS).toString();
        if (ipAddress != null) {
            THING_HANDLERS.put(ipAddress, this);
        } else {
            LOGGER.warn("Handler for thing {} could not be added to list because ipaddress == null", thing.getLabel());
        }

        if (!THING_HANDLERS.isEmpty()) {
            startRefreshStatusJob();
        }

        if (triggerStatusJob == null) {
            LOGGER.debug("REFRESH: Starting refresh trigger job for thing {} ", thing.getLabel());
            triggerStatusJob = scheduler.scheduleAtFixedRate(thingRefreshSender, 100,
                    goveeLanConfiguration.refreshInterval * 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stop the Refresh Status Job, so the same socket can be used for something else (like discovery)
     */
    public static void stopRefreshStatusJob() {
        refreshStatusJob.cancel(true);
        refreshStatusJob = null;
        refreshJobRunning = false;
    }

    /**
     * (re)start the refresh status job
     */
    public static void startRefreshStatusJob() {
        if (refreshStatusJob == null) {
            refreshStatusJob = ThreadPoolManager.getScheduledPool("thingHandler")
                    .scheduleAtFixedRate(REFRESH_STATUS_RECEIVER, 100, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        String ipAddress = thing.getConfiguration().getProperties().get(GoveeLanConfiguration.IPADDRESS).toString();
        triggerStatusJob.cancel(true);
        triggerStatusJob = null;
        THING_HANDLERS.remove(ipAddress);
        if (THING_HANDLERS.isEmpty()) {
            stopRefreshStatusJob();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                // we are refreshing all channels at once, as we get all information at the same time
                triggerDeviceStatusRefresh();
            } else {
                switch (channelUID.getId()) {
                    case SWITCH:
                        if (command instanceof OnOffType) {
                            send(command.equals(OnOffType.ON) ? LIGHT_ON : LIGHT_OFF);
                        }
                        break;
                    case COLOR:
                        if (command instanceof HSBType hsbCommand) {
                            int[] rgb = ColorUtil.hsbToRgb(hsbCommand);
                            send(String.format(GoveeLanHandler.LIGHT_COLOR, rgb[0], rgb[1], rgb[2], 0));
                        }
                        break;
                    case TEMPERATUR_ABS:
                        if (command instanceof QuantityType quantity) {
                            send(String.format(GoveeLanHandler.LIGHT_COLOR, 0, 0, 0, quantity.longValue()));
                        }
                        break;
                    case BRIGHTNESS:
                        if (command instanceof PercentType percent) {
                            send(String.format(GoveeLanHandler.LIGHT_BRIGHTNESS, percent.intValue()));
                        }
                        break;
                }
            }
            if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control/query device at IP address "
                            + thing.getConfiguration().getProperties().get(GoveeLanConfiguration.IPADDRESS));
        }
    }

    /**
     * Initiate a refresh to our thing devicee
     *
     */
    private void triggerDeviceStatusRefresh() throws IOException {
        if (refreshRunning) {
            return;
        }
        if (GoveeLanDiscoveryService.isDiscoveryActive()) {
            LOGGER.debug("Not triggering refresh as Scan is currently active");
            return;
        }
        refreshRunning = true;

        LOGGER.debug("trigger Refresh Status of device {}", thing.getLabel());

        try {
            send(QUERY_STATUS);
        } finally {
            refreshRunning = false;
        }
    }

    public void send(String message) throws IOException {
        DatagramSocket socket;
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
        byte[] data = message.getBytes();

        final String hostname = thing.getConfiguration().getProperties().get(GoveeLanConfiguration.IPADDRESS)
                .toString();
        LOGGER.trace("Sending {} to {}", message, hostname);
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, SENDTODEVICE_PORT);
        socket.send(packet);
        socket.close();
    }

    public void updateDeviceState(StatusMessage message) {
        int lastOnOff = message.msg().data().onOff();
        int lastBrightness = message.msg().data().brightness();
        Color lastColor = message.msg().data().color();
        int lastColorTemperature = message.msg().data().colorTemInKelvin();

        updateState(SWITCH, OnOffType.from(lastOnOff == 1));
        updateState(COLOR, ColorUtil.rgbToHsb(new int[] { lastColor.r(), lastColor.g(), lastColor.b() }));
        updateState(TEMPERATUR_ABS, new QuantityType(lastColorTemperature, Units.KELVIN));
        updateState(BRIGHTNESS, new PercentType(lastBrightness));
    }
}

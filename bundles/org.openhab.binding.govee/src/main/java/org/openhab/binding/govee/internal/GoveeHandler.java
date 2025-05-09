/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.govee.internal;

import static org.openhab.binding.govee.internal.GoveeBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.govee.internal.model.Color;
import org.openhab.binding.govee.internal.model.ColorData;
import org.openhab.binding.govee.internal.model.EmptyValueQueryStatusData;
import org.openhab.binding.govee.internal.model.GenericGoveeData;
import org.openhab.binding.govee.internal.model.GenericGoveeMsg;
import org.openhab.binding.govee.internal.model.GenericGoveeRequest;
import org.openhab.binding.govee.internal.model.StatusResponse;
import org.openhab.binding.govee.internal.model.ValueIntData;
import org.openhab.core.library.types.DecimalType;
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
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GoveeHandler} is responsible for handling commands, which are
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
 * as status updates. Therefore, when scanning new devices that job that listens to status devices must
 * be stopped while scanning new devices. Otherwise, the status job will receive the scan discover UDB packages.
 *
 * Controlling the lights is done via the Govee LAN API (cloud is not supported):
 * https://app-h5.govee.com/user-manual/wlan-guide
 *
 * @author Stefan Höhn - Initial contribution
 * @author Andrew Fiddian-Green - Added sequential task processing
 */
@NonNullByDefault
public class GoveeHandler extends BaseThingHandler {

    private static final Gson GSON = new Gson();
    private static final int REFRESH_SECONDS_MIN = 2;
    private static final int INTER_COMMAND_DELAY_MILLISEC = 100;

    private final Logger logger = LoggerFactory.getLogger(GoveeHandler.class);

    protected ScheduledExecutorService executorService = scheduler;
    private @Nullable ScheduledFuture<?> thingTaskSenderTask;
    private GoveeConfiguration goveeConfiguration = new GoveeConfiguration();

    private final CommunicationManager communicationManager;
    private final GoveeStateDescriptionProvider stateDescriptionProvider;
    private final List<Callable<Boolean>> taskQueue = new ArrayList<>();

    private OnOffType lastSwitch = OnOffType.OFF;
    private HSBType lastColor = new HSBType();

    private int lastKelvin;
    private int minKelvin;
    private int maxKelvin;

    private int refreshIntervalSeconds;
    private Instant nextRefreshDueTime = Instant.EPOCH;

    /**
     * This thing related job <i>thingTaskSender</i> sends the next queued command (if any)
     * to the Govee device. If there is no queued command and a regular refresh is due then
     * sends the command to trigger a status refresh.
     *
     * The device may send a reply to the common port and if so the response is received by
     * the refresh status receiver.
     */
    private final Runnable thingTaskSender = () -> {
        synchronized (taskQueue) {
            if (taskQueue.isEmpty() && Instant.now().isBefore(nextRefreshDueTime)) {
                return; // no queued command nor pending refresh
            }
            if (taskQueue.isEmpty()) {
                taskQueue.add(() -> triggerDeviceStatusRefresh());
                nextRefreshDueTime = Instant.now().plusSeconds(refreshIntervalSeconds);
            } else if (taskQueue.size() > 20) {
                logger.info("Command task queue size:{} exceeds limit:20", taskQueue.size());
            }
            try {
                if (taskQueue.remove(0).call()) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (IndexOutOfBoundsException e) {
                logger.warn("Unexpected List.remove() exception:{}", e.getMessage());
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.could-not-query-device [\"" + goveeConfiguration.hostname
                                + "\"]");
            }
        }
    };

    public GoveeHandler(Thing thing, CommunicationManager communicationManager,
            GoveeStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.communicationManager = communicationManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    public String getHostname() {
        return goveeConfiguration.hostname;
    }

    @Override
    public void initialize() {
        goveeConfiguration = getConfigAs(GoveeConfiguration.class);

        final String ipAddress = goveeConfiguration.hostname;
        if (ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.ip-address.missing");
            return;
        }

        minKelvin = Objects.requireNonNullElse(goveeConfiguration.minKelvin, COLOR_TEMPERATURE_MIN_VALUE.intValue());
        maxKelvin = Objects.requireNonNullElse(goveeConfiguration.maxKelvin, COLOR_TEMPERATURE_MAX_VALUE.intValue());
        if ((minKelvin < COLOR_TEMPERATURE_MIN_VALUE) || (maxKelvin > COLOR_TEMPERATURE_MAX_VALUE)
                || (minKelvin >= maxKelvin)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-color-temperature-range");
            return;
        }

        thing.setProperty(PROPERTY_COLOR_TEMPERATURE_MIN, Integer.toString(minKelvin));
        thing.setProperty(PROPERTY_COLOR_TEMPERATURE_MAX, Integer.toString(maxKelvin));
        stateDescriptionProvider.setMinMaxKelvin(new ChannelUID(thing.getUID(), CHANNEL_COLOR_TEMPERATURE_ABS),
                minKelvin, maxKelvin);

        refreshIntervalSeconds = goveeConfiguration.refreshInterval;
        if (refreshIntervalSeconds < REFRESH_SECONDS_MIN) {
            logger.warn("Config Param refreshInterval={} too low, minimum={}", refreshIntervalSeconds,
                    REFRESH_SECONDS_MIN);
            refreshIntervalSeconds = REFRESH_SECONDS_MIN;
        }

        updateStatus(ThingStatus.UNKNOWN);
        communicationManager.registerHandler(this);

        if (thingTaskSenderTask == null) {
            logger.debug("Starting refresh trigger job for thing {} ", thing.getLabel());
            thingTaskSenderTask = executorService.scheduleWithFixedDelay(thingTaskSender, INTER_COMMAND_DELAY_MILLISEC,
                    INTER_COMMAND_DELAY_MILLISEC, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        taskQueue.clear();
        ScheduledFuture<?> job = thingTaskSenderTask;
        if (job != null) {
            job.cancel(true);
            thingTaskSenderTask = null;
        }
        communicationManager.unregisterHandler(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command commandParam) {
        Command command = commandParam;

        synchronized (taskQueue) {
            logger.debug("handleCommand({}, {})", channelUID, command);

            if (command instanceof RefreshType) {
                taskQueue.add(() -> triggerDeviceStatusRefresh());
            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_COLOR:
                        if (command instanceof HSBType hsb) {
                            taskQueue.add(() -> sendColor(hsb));
                            command = hsb.getBrightness(); // fall through
                        }
                        if (command instanceof PercentType percent) {
                            taskQueue.add(() -> sendBrightness(percent));
                            command = OnOffType.from(percent.intValue() > 0); // fall through
                        }
                        if (command instanceof OnOffType onOff) {
                            taskQueue.add(() -> sendOnOff(onOff));
                            taskQueue.add(() -> triggerDeviceStatusRefresh());
                        }
                        break;

                    case CHANNEL_COLOR_TEMPERATURE:
                        if (command instanceof PercentType percent) {
                            taskQueue.add(() -> sendKelvin(percentToKelvin(percent)));
                            taskQueue.add(() -> triggerDeviceStatusRefresh());
                        }
                        break;

                    case CHANNEL_COLOR_TEMPERATURE_ABS:
                        if (command instanceof QuantityType<?> genericQuantity) {
                            QuantityType<?> kelvin = genericQuantity.toInvertibleUnit(Units.KELVIN);
                            if (kelvin == null) {
                                logger.warn("handleCommand() invalid QuantityType:{}", genericQuantity);
                                break;
                            }
                            taskQueue.add(() -> sendKelvin(kelvin.intValue()));
                            taskQueue.add(() -> triggerDeviceStatusRefresh());
                        } else if (command instanceof DecimalType kelvin) {
                            taskQueue.add(() -> sendKelvin(kelvin.intValue()));
                            taskQueue.add(() -> triggerDeviceStatusRefresh());
                        }
                        break;
                }
            }
        }
    }

    /**
     * Initiate a refresh to our thing device
     */
    private boolean triggerDeviceStatusRefresh() throws IOException {
        logger.debug("triggerDeviceStatusRefresh() to {}", thing.getUID());
        GenericGoveeData data = new EmptyValueQueryStatusData();
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("devStatus", data));
        communicationManager.sendRequest(this, request);
        return true;
    }

    /**
     * Send the normalized RGB color parameters.
     */
    public boolean sendColor(HSBType color) throws IOException {
        logger.debug("sendColor({}) to {}", color, thing.getUID());
        int[] normalRGB = ColorUtil.hsbToRgb(new HSBType(color.getHue(), color.getSaturation(), PercentType.HUNDRED));
        GenericGoveeData data = new ColorData(new Color(normalRGB[0], normalRGB[1], normalRGB[2]), 0);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("colorwc", data));
        communicationManager.sendRequest(this, request);
        return true;
    }

    /**
     * Send the brightness parameter.
     */
    public boolean sendBrightness(PercentType brightness) throws IOException {
        logger.debug("sendBrightness({}) to {}", brightness, thing.getUID());
        GenericGoveeData data = new ValueIntData(brightness.intValue());
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("brightness", data));
        communicationManager.sendRequest(this, request);
        return true;
    }

    /**
     * Send the on-off parameter.
     */
    private boolean sendOnOff(OnOffType onOff) throws IOException {
        logger.debug("sendOnOff({}) to {}", onOff, thing.getUID());
        GenericGoveeData data = new ValueIntData(onOff == OnOffType.ON ? 1 : 0);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("turn", data));
        communicationManager.sendRequest(this, request);
        return true;
    }

    /**
     * Set the color temperature (Kelvin) parameter.
     */
    private boolean sendKelvin(int kelvin) throws IOException {
        logger.debug("sendKelvin({}) to {}", kelvin, thing.getUID());
        GenericGoveeData data = new ColorData(new Color(0, 0, 0), kelvin);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("colorwc", data));
        communicationManager.sendRequest(this, request);
        return true;
    }

    /**
     * Build an {@link HSBType} from the given normalized {@link Color} RGB parameters, brightness, and on-off state
     * parameters. If the on parameter is true then use the brightness parameter, otherwise use a brightness of zero.
     *
     * @param normalRgbParams record containing the lamp's normalized RGB parameters (0..255)
     * @param brightnessParam the lamp brightness in range 0..100
     * @param onParam the lamp on-off state
     *
     * @return the respective HSBType
     */
    private static HSBType buildHSB(Color normalRgbParams, int brightnessParam, boolean onParam) {
        HSBType normalColor = ColorUtil
                .rgbToHsb(new int[] { normalRgbParams.r(), normalRgbParams.g(), normalRgbParams.b() });
        PercentType brightness = onParam ? new PercentType(brightnessParam) : PercentType.ZERO;
        return new HSBType(normalColor.getHue(), normalColor.getSaturation(), brightness);
    }

    public void handleIncomingStatus(String response) {
        if (response.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.empty-response");
            return;
        }

        try {
            StatusResponse statusMessage = GSON.fromJson(response, StatusResponse.class);
            if (statusMessage != null) {
                updateDeviceState(statusMessage);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (JsonSyntaxException jse) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, jse.getMessage());
        }
    }

    public void updateDeviceState(@Nullable StatusResponse message) {
        if (message == null) {
            return;
        }

        logger.debug("updateDeviceState() for {}", thing.getUID());

        OnOffType sw = OnOffType.from(message.msg().data().onOff() == 1);
        int brightness = message.msg().data().brightness();
        Color normalRGB = message.msg().data().color();
        int kelvin = message.msg().data().colorTemInKelvin();

        logger.trace("Update values: switch:{}, brightness:{}, normalRGB:{}, kelvin:{}", sw, brightness, normalRGB,
                kelvin);

        HSBType color = buildHSB(normalRGB, brightness, true);

        logger.trace("Compare hsb old:{} to new:{}, switch old:{} to new:{}", lastColor, color, lastSwitch, sw);
        if ((sw != lastSwitch) || !color.equals(lastColor)) {
            logger.trace("Update hsb old:{} to new:{}, switch old:{} to new:{}", lastColor, color, lastSwitch, sw);
            updateState(CHANNEL_COLOR, buildHSB(normalRGB, brightness, sw == OnOffType.ON));
            lastSwitch = sw;
            lastColor = color;
        }

        logger.trace("Compare kelvin old:{} to new:{}", lastKelvin, kelvin);
        if (kelvin != lastKelvin) {
            logger.trace("Update kelvin old:{} to new:{}", lastKelvin, kelvin);
            if (kelvin != 0) {
                kelvin = Math.round(Math.min(maxKelvin, Math.max(minKelvin, kelvin)));
                updateState(CHANNEL_COLOR_TEMPERATURE, kelvinToPercent(kelvin));
                updateState(CHANNEL_COLOR_TEMPERATURE_ABS, QuantityType.valueOf(kelvin, Units.KELVIN));
            } else {
                updateState(CHANNEL_COLOR_TEMPERATURE, UnDefType.UNDEF);
                updateState(CHANNEL_COLOR_TEMPERATURE_ABS, UnDefType.UNDEF);
            }
            lastKelvin = kelvin;
        }
    }

    /**
     * Convert PercentType to Kelvin.
     */
    private int percentToKelvin(PercentType percent) {
        return (int) Math.round((((maxKelvin - minKelvin) * percent.doubleValue() / 100.0) + minKelvin));
    }

    /**
     * Convert Kelvin to PercentType.
     */
    private PercentType kelvinToPercent(int kelvin) {
        return new PercentType((int) Math.round((kelvin - minKelvin) * 100.0 / (maxKelvin - minKelvin)));
    }
}

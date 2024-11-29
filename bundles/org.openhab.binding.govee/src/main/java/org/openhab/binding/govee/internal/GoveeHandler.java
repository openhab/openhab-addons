/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
 */
@NonNullByDefault
public class GoveeHandler extends BaseThingHandler {

    /*
     * Messages to be sent to the Govee devices
     */
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(GoveeHandler.class);
    protected ScheduledExecutorService executorService = scheduler;
    @Nullable
    private ScheduledFuture<?> triggerStatusJob; // send device status update job
    private GoveeConfiguration goveeConfiguration = new GoveeConfiguration();

    private final CommunicationManager communicationManager;
    private final GoveeStateDescriptionProvider stateDescriptionProvider;

    private OnOffType lastSwitch = OnOffType.OFF;
    private HSBType lastColor = new HSBType();
    private int lastKelvin;

    private int minKelvin;
    private int maxKelvin;

    private static final int INTER_COMMAND_DELAY_MILLISEC = 100;

    /**
     * This thing related job <i>thingRefreshSender</i> triggers an update to the Govee device.
     * The device sends it back to the common port and the response is
     * then received by the common #refreshStatusReceiver
     */
    private final Runnable thingRefreshSender = () -> {
        try {
            triggerDeviceStatusRefresh();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.could-not-query-device [\"" + goveeConfiguration.hostname
                            + "\"]");
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
        stateDescriptionProvider.setMinMaxKelvin(new ChannelUID(thing.getUID(), CHANNEL_COLOR_TEMPERATURE_ABS),
                minKelvin, maxKelvin);

        updateStatus(ThingStatus.UNKNOWN);
        communicationManager.registerHandler(this);
        if (triggerStatusJob == null) {
            logger.debug("Starting refresh trigger job for thing {} ", thing.getLabel());
            triggerStatusJob = executorService.scheduleWithFixedDelay(thingRefreshSender, 100,
                    goveeConfiguration.refreshInterval * 1000L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        ScheduledFuture<?> triggerStatusJobFuture = triggerStatusJob;
        if (triggerStatusJobFuture != null) {
            triggerStatusJobFuture.cancel(true);
            triggerStatusJob = null;
        }
        communicationManager.unregisterHandler(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command commandParam) {
        Command command = commandParam;

        logger.debug("handleCommand({}, {})", channelUID, command);

        List<Callable<?>> communicationTasks = new ArrayList<>();
        boolean mustRefresh = false;

        if (command instanceof RefreshType) {
            // we are refreshing all channels at once, as we get all information at the same time
            mustRefresh = true;
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_COLOR:
                    if (command instanceof HSBType hsb) {
                        communicationTasks.add(() -> {
                            sendColor(hsb);
                            return true;
                        });
                        command = hsb.getBrightness(); // fall through
                    }
                    if (command instanceof PercentType percent) {
                        communicationTasks.add(() -> {
                            sendBrightness(percent);
                            return true;
                        });
                        command = OnOffType.from(percent.intValue() > 0); // fall through
                    }
                    if (command instanceof OnOffType onOff) {
                        communicationTasks.add(() -> {
                            sendOnOff(onOff);
                            return true;
                        });
                    }
                    break;

                case CHANNEL_COLOR_TEMPERATURE:
                    if (command instanceof PercentType percent) {
                        communicationTasks.add(() -> {
                            sendKelvin(percentToKelvin(percent));
                            return true;
                        });
                    }
                    break;

                case CHANNEL_COLOR_TEMPERATURE_ABS:
                    if (command instanceof QuantityType<?> genericQuantity) {
                        QuantityType<?> kelvin = genericQuantity.toInvertibleUnit(Units.KELVIN);
                        if (kelvin == null) {
                            logger.warn("handleCommand() invalid QuantityType:{}", genericQuantity);
                            break;
                        }
                        communicationTasks.add(() -> {
                            sendKelvin(kelvin.intValue());
                            return true;
                        });
                    }
                    break;
            }

            if (mustRefresh || !communicationTasks.isEmpty()) {
                communicationTasks.add(() -> {
                    triggerDeviceStatusRefresh();
                    return true;
                });
            }

            if (!communicationTasks.isEmpty()) {
                scheduler.submit(() -> runCallableTasks(communicationTasks));
            }
        }
    }

    /**
     * Iterate over the given list of {@link Callable} tasks and run them with a delay between each. Reason for the
     * delay is that Govee lamps cannot process multiple commands in short succession. Update the thing status depending
     * on whether any of the tasks encountered an exception.
     *
     * @param callables a list of Callable tasks (which shall not be empty)
     */
    @SuppressWarnings("null")
    private void runCallableTasks(List<Callable<?>> callables) {
        boolean exception = false;
        ListIterator<Callable<?>> tasks = callables.listIterator();
        while (true) { // don't check hasNext() (we do it below)
            try {
                tasks.next().call();
            } catch (Exception e) {
                exception = true;
                break;
            }
            if (!tasks.hasNext()) {
                break;
            }
            try {
                Thread.sleep(INTER_COMMAND_DELAY_MILLISEC);
            } catch (InterruptedException e) {
                return;
            }
        }
        if (!exception) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.could-not-query-device [\"" + goveeConfiguration.hostname
                            + "\"]");
        }
    }

    /**
     * Initiate a refresh to our thing device
     */
    private void triggerDeviceStatusRefresh() throws IOException {
        logger.debug("triggerDeviceStatusRefresh() on {}", thing.getUID());
        GenericGoveeData data = new EmptyValueQueryStatusData();
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("devStatus", data));
        communicationManager.sendRequest(this, request);
    }

    /**
     * Send the normalized RGB color parameters.
     */
    public void sendColor(HSBType color) throws IOException {
        logger.debug("sendColor({}) to {}", color, thing.getUID());
        int[] normalRGB = ColorUtil.hsbToRgb(new HSBType(color.getHue(), color.getSaturation(), PercentType.HUNDRED));
        GenericGoveeData data = new ColorData(new Color(normalRGB[0], normalRGB[1], normalRGB[2]), 0);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("colorwc", data));
        communicationManager.sendRequest(this, request);
    }

    /**
     * Send the brightness parameter.
     */
    public void sendBrightness(PercentType brightness) throws IOException {
        logger.debug("sendBrightness({}) to {}", brightness, thing.getUID());
        GenericGoveeData data = new ValueIntData(brightness.intValue());
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("brightness", data));
        communicationManager.sendRequest(this, request);
    }

    /**
     * Send the on-off parameter.
     */
    private void sendOnOff(OnOffType onOff) throws IOException {
        logger.debug("sendOnOff({}) to {}", onOff, thing.getUID());
        GenericGoveeData data = new ValueIntData(onOff == OnOffType.ON ? 1 : 0);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("turn", data));
        communicationManager.sendRequest(this, request);
    }

    /**
     * Set the color temperature (Kelvin) parameter.
     */
    private void sendKelvin(int kelvin) throws IOException {
        logger.debug("sendKelvin({}) to {}", kelvin, thing.getUID());
        GenericGoveeData data = new ColorData(new Color(0, 0, 0), kelvin);
        GenericGoveeRequest request = new GenericGoveeRequest(new GenericGoveeMsg("colorwc", data));
        communicationManager.sendRequest(this, request);
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

    void handleIncomingStatus(String response) {
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
        logger.trace("- switch:{}", sw);

        int brightness = message.msg().data().brightness();
        logger.trace("- brightness:{}", brightness);

        Color normalRGB = message.msg().data().color();
        logger.trace("- normalRGB:{}", normalRGB);

        int kelvin = message.msg().data().colorTemInKelvin();
        logger.trace("- kelvin:{}", kelvin);

        HSBType color = buildHSB(normalRGB, brightness, true);

        logger.trace("Compare color old:{} to new:{}, on-state old:{} to new:{}", lastColor, color, lastSwitch, sw);
        if ((sw != lastSwitch) || !color.equals(lastColor)) {
            logger.trace("Update color old:{} to new:{}, on-state old:{} to new:{}", lastColor, color, lastSwitch, sw);
            updateState(CHANNEL_COLOR, buildHSB(normalRGB, brightness, sw == OnOffType.ON));
            lastSwitch = sw;
            lastColor = color;
        }

        logger.trace("Compare color temperature old:{} to new:{}", lastKelvin, kelvin);
        if (kelvin != lastKelvin) {
            logger.trace("Update color temperature old:{} to new:{}", lastKelvin, kelvin);
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

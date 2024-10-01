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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.govee.internal.model.Color;
import org.openhab.binding.govee.internal.model.ColorData;
import org.openhab.binding.govee.internal.model.EmptyValueQueryStatusData;
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

    private CommunicationManager communicationManager;

    private int lastOnOff;
    private int lastBrightness;
    private HSBType lastColor = new HSBType();
    private int lastColorTempInKelvin = COLOR_TEMPERATURE_MIN_VALUE.intValue();

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

    public GoveeHandler(Thing thing, CommunicationManager communicationManager) {
        super(thing);
        this.communicationManager = communicationManager;
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                // we are refreshing all channels at once, as we get all information at the same time
                triggerDeviceStatusRefresh();
                logger.debug("Triggering Refresh");
            } else {
                logger.debug("Channel ID {} type {}", channelUID.getId(), command.getClass());
                switch (channelUID.getId()) {
                    case CHANNEL_COLOR:
                        if (command instanceof HSBType hsbCommand) {
                            int[] rgb = ColorUtil.hsbToRgb(hsbCommand);
                            sendColor(new Color(rgb[0], rgb[1], rgb[2]));
                        } else if (command instanceof PercentType percent) {
                            sendBrightness(percent.intValue());
                        } else if (command instanceof OnOffType onOffCommand) {
                            sendOnOff(onOffCommand);
                        }
                        break;
                    case CHANNEL_COLOR_TEMPERATURE:
                        if (command instanceof PercentType percent) {
                            logger.debug("COLOR_TEMPERATURE: Color Temperature change with Percent Type {}", command);
                            Double colorTemp = (COLOR_TEMPERATURE_MIN_VALUE + percent.intValue()
                                    * (COLOR_TEMPERATURE_MAX_VALUE - COLOR_TEMPERATURE_MIN_VALUE) / 100.0);
                            lastColorTempInKelvin = colorTemp.intValue();
                            logger.debug("lastColorTempInKelvin {}", lastColorTempInKelvin);
                            sendColorTemp(lastColorTempInKelvin);
                        }
                        break;
                    case CHANNEL_COLOR_TEMPERATURE_ABS:
                        if (command instanceof QuantityType<?> quantity) {
                            logger.debug("Color Temperature Absolute change with Percent Type {}", command);
                            lastColorTempInKelvin = quantity.intValue();
                            logger.debug("COLOR_TEMPERATURE_ABS: lastColorTempInKelvin {}", lastColorTempInKelvin);
                            int lastColorTempInPercent = ((Double) ((lastColorTempInKelvin
                                    - COLOR_TEMPERATURE_MIN_VALUE)
                                    / (COLOR_TEMPERATURE_MAX_VALUE - COLOR_TEMPERATURE_MIN_VALUE) * 100.0)).intValue();
                            logger.debug("computed lastColorTempInPercent {}", lastColorTempInPercent);
                            sendColorTemp(lastColorTempInKelvin);
                        }
                        break;
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.could-not-query-device [\"" + goveeConfiguration.hostname
                            + "\"]");
        }
    }

    /**
     * Initiate a refresh to our thing devicee
     *
     */
    private void triggerDeviceStatusRefresh() throws IOException {
        logger.debug("trigger Refresh Status of device {}", thing.getLabel());
        GenericGoveeRequest lightQuery = new GenericGoveeRequest(
                new GenericGoveeMsg("devStatus", new EmptyValueQueryStatusData()));
        communicationManager.sendRequest(this, lightQuery);
    }

    public void sendColor(Color color) throws IOException {
        lastColor = ColorUtil.rgbToHsb(new int[] { color.r(), color.g(), color.b() });

        GenericGoveeRequest lightColor = new GenericGoveeRequest(
                new GenericGoveeMsg("colorwc", new ColorData(color, 0)));
        communicationManager.sendRequest(this, lightColor);
    }

    public void sendBrightness(int brightness) throws IOException {
        lastBrightness = brightness;
        GenericGoveeRequest lightBrightness = new GenericGoveeRequest(
                new GenericGoveeMsg("brightness", new ValueIntData(brightness)));
        communicationManager.sendRequest(this, lightBrightness);
    }

    private void sendOnOff(OnOffType switchValue) throws IOException {
        lastOnOff = (switchValue == OnOffType.ON) ? 1 : 0;
        GenericGoveeRequest switchLight = new GenericGoveeRequest(
                new GenericGoveeMsg("turn", new ValueIntData(lastOnOff)));
        communicationManager.sendRequest(this, switchLight);
    }

    private void sendColorTemp(int colorTemp) throws IOException {
        lastColorTempInKelvin = colorTemp;
        logger.debug("sendColorTemp {}", colorTemp);
        GenericGoveeRequest lightColor = new GenericGoveeRequest(
                new GenericGoveeMsg("colorwc", new ColorData(new Color(0, 0, 0), colorTemp)));
        communicationManager.sendRequest(this, lightColor);
    }

    /**
     * Creates a Color state by using the last color information from lastColor
     * The brightness is overwritten either by the provided lastBrightness
     * or if lastOnOff = 0 (off) then the brightness is set 0
     *
     * @see #lastColor
     * @see #lastBrightness
     * @see #lastOnOff
     *
     * @return the computed state
     */
    private HSBType getColorState(Color color, int brightness) {
        PercentType computedBrightness = lastOnOff == 0 ? new PercentType(0) : new PercentType(brightness);
        int[] rgb = { color.r(), color.g(), color.b() };
        HSBType hsb = ColorUtil.rgbToHsb(rgb);
        return new HSBType(hsb.getHue(), hsb.getSaturation(), computedBrightness);
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

        logger.trace("Receiving Device State");
        int newOnOff = message.msg().data().onOff();
        logger.trace("newOnOff = {}", newOnOff);
        int newBrightness = message.msg().data().brightness();
        logger.trace("newBrightness = {}", newBrightness);
        Color newColor = message.msg().data().color();
        logger.trace("newColor = {}", newColor);
        int newColorTempInKelvin = message.msg().data().colorTemInKelvin();
        logger.trace("newColorTempInKelvin = {}", newColorTempInKelvin);

        newColorTempInKelvin = (newColorTempInKelvin < COLOR_TEMPERATURE_MIN_VALUE)
                ? COLOR_TEMPERATURE_MIN_VALUE.intValue()
                : newColorTempInKelvin;
        newColorTempInKelvin = (newColorTempInKelvin > COLOR_TEMPERATURE_MAX_VALUE)
                ? COLOR_TEMPERATURE_MAX_VALUE.intValue()
                : newColorTempInKelvin;

        int newColorTempInPercent = ((Double) ((newColorTempInKelvin - COLOR_TEMPERATURE_MIN_VALUE)
                / (COLOR_TEMPERATURE_MAX_VALUE - COLOR_TEMPERATURE_MIN_VALUE) * 100.0)).intValue();

        HSBType adaptedColor = getColorState(newColor, newBrightness);

        logger.trace("HSB old: {} vs adaptedColor: {}", lastColor, adaptedColor);
        // avoid noise by only updating if the value has changed on the device
        if (!adaptedColor.equals(lastColor)) {
            logger.trace("UPDATING HSB old: {} != {}", lastColor, adaptedColor);
            updateState(CHANNEL_COLOR, adaptedColor);
        }

        // avoid noise by only updating if the value has changed on the device
        logger.trace("Color-Temperature Status: old: {} K {}% vs new: {} K", lastColorTempInKelvin,
                newColorTempInPercent, newColorTempInKelvin);
        if (newColorTempInKelvin != lastColorTempInKelvin) {
            logger.trace("Color-Temperature Status: old: {} K {}% vs new: {} K", lastColorTempInKelvin,
                    newColorTempInPercent, newColorTempInKelvin);
            updateState(CHANNEL_COLOR_TEMPERATURE_ABS, new QuantityType<>(lastColorTempInKelvin, Units.KELVIN));
            updateState(CHANNEL_COLOR_TEMPERATURE, new PercentType(newColorTempInPercent));
        }

        lastOnOff = newOnOff;
        lastColor = adaptedColor;
        lastBrightness = newBrightness;
    }
}

/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hue.internal.FullHueObject;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.State;
import org.openhab.binding.hue.internal.State.ColorMode;
import org.openhab.binding.hue.internal.StateUpdate;
import org.openhab.binding.hue.internal.action.LightActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueLightHandler} is the handler for a hue light. It uses the {@link HueClient} to execute the actual
 * command.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - stabilized code
 * @author Andre Fuechsel - implemented switch off when brightness == 0, changed to support generic thing types, changed
 *         the initialization of properties
 * @author Thomas Höfer - added thing properties
 * @author Jochen Hiller - fixed status updates for reachable=true/false
 * @author Markus Mazurczak - added code for command handling of OSRAM PAR16 50
 *         bulbs
 * @author Yordan Zhelev - added alert and effect functions
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 * @author Jochen Leopold - Added support for custom fade times
 */
@NonNullByDefault
public class HueLightHandler extends BaseThingHandler implements LightStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_COLOR_LIGHT,
            THING_TYPE_COLOR_TEMPERATURE_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_ON_OFF_PLUG, THING_TYPE_DIMMABLE_PLUG).collect(Collectors.toSet());

    // @formatter:off
    private static final Map<String, List<String>> VENDOR_MODEL_MAP = Stream.of(
            new SimpleEntry<>("Philips",
                    Arrays.asList("LCT001", "LCT002", "LCT003", "LCT007", "LLC001", "LLC006", "LLC007", "LLC010",
                            "LLC011", "LLC012", "LLC013", "LLC020", "LST001", "LST002", "LWB004", "LWB006", "LWB007",
                            "LWL001")),
            new SimpleEntry<>("OSRAM",
                    Arrays.asList("Classic_A60_RGBW", "PAR16_50_TW", "Surface_Light_TW", "Plug_01")))
        .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    // @formatter:on

    private static final String OSRAM_PAR16_50_TW_MODEL_ID = "PAR16_50_TW";

    @NonNullByDefault({})
    private String lightId;

    private @Nullable Integer lastSentColorTemp;
    private @Nullable Integer lastSentBrightness;

    private final Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    // Flag to indicate whether the bulb is of type Osram par16 50 TW or not
    private boolean isOsramPar16 = false;

    private boolean propertiesInitializedSuccessfully = false;
    private long defaultFadeTime = 400;

    private @Nullable HueClient hueClient;

    @Nullable
    ScheduledFuture<?> scheduledFuture;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            BigDecimal time = (BigDecimal) getConfig().get(FADETIME);
            if (time != null) {
                defaultFadeTime = time.longValueExact();
            }

            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getHueClient() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    initializeProperties();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-light-id");
        }
    }

    private synchronized void initializeProperties() {
        if (!propertiesInitializedSuccessfully) {
            FullHueObject fullLight = getLight();
            if (fullLight != null) {
                String softwareVersion = fullLight.getSoftwareVersion();
                if (softwareVersion != null) {
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, softwareVersion);
                }
                String modelId = fullLight.getNormalizedModelID();
                if (modelId != null) {
                    updateProperty(Thing.PROPERTY_MODEL_ID, modelId);
                    String vendor = getVendor(modelId);
                    if (vendor != null) {
                        updateProperty(Thing.PROPERTY_VENDOR, vendor);
                    }
                } else {
                    updateProperty(Thing.PROPERTY_VENDOR, fullLight.getManufacturerName());
                }
                String uniqueID = fullLight.getUniqueID();
                if (uniqueID != null) {
                    updateProperty(UNIQUE_ID, uniqueID);
                }
                isOsramPar16 = OSRAM_PAR16_50_TW_MODEL_ID.equals(modelId);
                propertiesInitializedSuccessfully = true;
            }
        }
    }

    private @Nullable String getVendor(String modelId) {
        for (String vendor : VENDOR_MODEL_MAP.keySet()) {
            if (VENDOR_MODEL_MAP.get(vendor).contains(modelId)) {
                return vendor;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Hue light handler disposes. Unregistering listener.");
        if (lightId != null) {
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterLightStatusListener(this);
                hueClient = null;
            }
            lightId = null;
        }
    }

    private @Nullable FullLight getLight() {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler != null) {
            return bridgeHandler.getLightById(lightId);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommand(channelUID.getId(), command, defaultFadeTime);
    }

    public void handleCommand(String channel, Command command, long fadeTime) {
        HueClient hueBridge = getHueClient();
        if (hueBridge == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        FullLight light = getLight();
        if (light == null) {
            logger.debug("hue light not known on bridge. Cannot handle command.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-wrong-light-id");
            return;
        }

        StateUpdate lightState = null;
        switch (channel) {
            case CHANNEL_COLORTEMPERATURE:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toColorTemperatureLightState((PercentType) command);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertColorTempChangeToStateUpdate((IncreaseDecreaseType) command, light);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                }

                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                }
                if (lightState != null && lastSentColorTemp != null) {
                    // make sure that the light also has the latest color temp
                    // this might not have been yet set in the light, if it was off
                    lightState.setColorTemperature(lastSentColorTemp);
                    lightState.setTransitionTime(fadeTime);
                }
                break;
            case CHANNEL_SWITCH:
                logger.trace("CHANNEL_SWITCH handling command {}", command);
                if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                }
                if (lightState != null && lastSentColorTemp != null) {
                    // make sure that the light also has the latest color temp
                    // this might not have been yet set in the light, if it was off
                    lightState.setColorTemperature(lastSentColorTemp);
                    lightState.setTransitionTime(fadeTime);
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        lightState = LightStateConverter.toOnOffLightState(OnOffType.OFF);
                    } else {
                        lightState = LightStateConverter.toColorLightState(hsbCommand, light.getState());
                        if (lightState != null) {
                            lightState.setTransitionTime(fadeTime);
                        }
                    }
                } else if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
                    if (lightState != null) {
                        lightState.setTransitionTime(fadeTime);
                    }
                }
                break;
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    lightState = LightStateConverter.toAlertState((StringType) command);
                    if (lightState == null) {
                        // Unsupported StringType is passed. Log a warning
                        // message and return.
                        logger.warn("Unsupported String command: {}. Supported commands are: {}, {}, {} ", command,
                                LightStateConverter.ALERT_MODE_NONE, LightStateConverter.ALERT_MODE_SELECT,
                                LightStateConverter.ALERT_MODE_LONG_SELECT);
                        return;
                    } else {
                        scheduleAlertStateRestore(command);
                    }
                }
                break;
            case CHANNEL_EFFECT:
                if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffEffectState((OnOffType) command);
                }
                break;
        }
        if (lightState != null) {
            // Cache values which we have sent
            Integer tmpBrightness = lightState.getBrightness();
            if (tmpBrightness != null) {
                lastSentBrightness = tmpBrightness;
            }
            Integer tmpColorTemp = lightState.getColorTemperature();
            if (tmpColorTemp != null) {
                lastSentColorTemp = tmpColorTemp;
            }
            hueBridge.updateLightState(light, lightState);
        } else {
            logger.warn("Command sent to an unknown channel id: {}:{}", getThing().getUID(), channel);
        }
    }

    /*
     * Applies additional {@link StateUpdate} commands as a workaround for Osram
     * Lightify PAR16 TW firmware bug. Also see
     * http://www.everyhue.com/vanilla/discussion/1756/solved-lightify-turning-off
     */
    private StateUpdate addOsramSpecificCommands(StateUpdate lightState, OnOffType actionType) {
        if (actionType.equals(OnOffType.ON)) {
            lightState.setBrightness(254);
        } else {
            lightState.setTransitionTime(0);
        }
        return lightState;
    }

    private @Nullable StateUpdate convertColorTempChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        StateUpdate stateUpdate = null;
        Integer currentColorTemp = getCurrentColorTemp(light.getState());
        if (currentColorTemp != null) {
            int newColorTemp = LightStateConverter.toAdjustedColorTemp(command, currentColorTemp);
            stateUpdate = new StateUpdate().setColorTemperature(newColorTemp);
        }
        return stateUpdate;
    }

    private @Nullable Integer getCurrentColorTemp(@Nullable State lightState) {
        Integer colorTemp = lastSentColorTemp;
        if (colorTemp == null && lightState != null) {
            colorTemp = lightState.getColorTemperature();
        }
        return colorTemp;
    }

    private @Nullable StateUpdate convertBrightnessChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        StateUpdate stateUpdate = null;
        Integer currentBrightness = getCurrentBrightness(light.getState());
        if (currentBrightness != null) {
            int newBrightness = LightStateConverter.toAdjustedBrightness(command, currentBrightness);
            stateUpdate = createBrightnessStateUpdate(currentBrightness, newBrightness);
        }
        return stateUpdate;
    }

    private @Nullable Integer getCurrentBrightness(@Nullable State lightState) {
        Integer brightness = lastSentBrightness;
        if (brightness == null && lightState != null) {
            if (!lightState.isOn()) {
                brightness = 0;
            } else {
                brightness = lightState.getBrightness();
            }
        }
        return brightness;
    }

    private StateUpdate createBrightnessStateUpdate(int currentBrightness, int newBrightness) {
        StateUpdate lightUpdate = new StateUpdate();
        if (newBrightness == 0) {
            lightUpdate.turnOff();
        } else {
            lightUpdate.setBrightness(newBrightness);
            if (currentBrightness == 0) {
                lightUpdate.turnOn();
            }
        }
        return lightUpdate;
    }

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueClient) {
                hueClient = (HueClient) handler;
                hueClient.registerLightStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void onLightStateChanged(@Nullable HueBridge bridge, FullLight fullLight) {
        logger.trace("onLightStateChanged() was called");

        if (!fullLight.getId().equals(lightId)) {
            logger.trace("Received state change for another handler's light ({}). Will be ignored.", fullLight.getId());
            return;
        }

        initializeProperties();

        lastSentColorTemp = null;
        lastSentBrightness = null;

        // update status (ONLINE, OFFLINE)
        if (fullLight.getState().isReachable()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            // we assume OFFLINE without any error (NONE), as this is an
            // expected state (when bulb powered off)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-not-reachable");
        }

        HSBType hsbType = LightStateConverter.toHSBType(fullLight.getState());
        if (!fullLight.getState().isOn()) {
            hsbType = new HSBType(hsbType.getHue(), hsbType.getSaturation(), new PercentType(0));
        }
        updateState(CHANNEL_COLOR, hsbType);

        ColorMode colorMode = fullLight.getState().getColorMode();
        if (ColorMode.CT.equals(colorMode)) {
            PercentType colorTempPercentType = LightStateConverter.toColorTemperaturePercentType(fullLight.getState());
            updateState(CHANNEL_COLORTEMPERATURE, colorTempPercentType);
        } else {
            updateState(CHANNEL_COLORTEMPERATURE, UnDefType.NULL);
        }

        PercentType brightnessPercentType = LightStateConverter.toBrightnessPercentType(fullLight.getState());
        if (!fullLight.getState().isOn()) {
            brightnessPercentType = new PercentType(0);
        }
        updateState(CHANNEL_BRIGHTNESS, brightnessPercentType);

        if (fullLight.getState().isOn()) {
            updateState(CHANNEL_SWITCH, OnOffType.ON);
        } else {
            updateState(CHANNEL_SWITCH, OnOffType.OFF);
        }

        StringType stringType = LightStateConverter.toAlertStringType(fullLight.getState());
        if (!stringType.toString().equals("NULL")) {
            updateState(CHANNEL_ALERT, stringType);
            scheduleAlertStateRestore(stringType);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueClient handler = getHueClient();
        if (handler != null) {
            FullLight light = handler.getLightById(lightId);
            if (light != null) {
                onLightStateChanged(null, light);
            }
        }
    }

    @Override
    public void onLightRemoved(@Nullable HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-removed");
        }
    }

    @Override
    public void onLightGone(@Nullable HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/offline.light-not-reachable");
        }
    }

    @Override
    public void onLightAdded(@Nullable HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            onLightStateChanged(bridge, light);
        }
    }

    /**
     * Schedules restoration of the alert item state to {@link LightStateConverter#ALERT_MODE_NONE} after a given time.
     * <br>
     * Based on the initial command:
     * <ul>
     * <li>For {@link LightStateConverter#ALERT_MODE_SELECT} restoration will be triggered after <strong>2
     * seconds</strong>.
     * <li>For {@link LightStateConverter#ALERT_MODE_LONG_SELECT} restoration will be triggered after <strong>15
     * seconds</strong>.
     * </ul>
     * This method also cancels any previously scheduled restoration.
     *
     * @param command The {@link Command} sent to the item
     */
    private void scheduleAlertStateRestore(Command command) {
        cancelScheduledFuture();
        int delay = getAlertDuration(command);

        if (delay > 0) {
            scheduledFuture = scheduler.schedule(() -> {
                updateState(CHANNEL_ALERT, new StringType(LightStateConverter.ALERT_MODE_NONE));
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * This method will cancel previously scheduled alert item state
     * restoration.
     */
    private void cancelScheduledFuture() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * This method returns the time in <strong>milliseconds</strong> after
     * which, the state of the alert item has to be restored to {@link LightStateConverter#ALERT_MODE_NONE}.
     *
     * @param command The initial command sent to the alert item.
     * @return Based on the initial command will return:
     *         <ul>
     *         <li><strong>2000</strong> for {@link LightStateConverter#ALERT_MODE_SELECT}.
     *         <li><strong>15000</strong> for {@link LightStateConverter#ALERT_MODE_LONG_SELECT}.
     *         <li><strong>-1</strong> for any command different from the previous two.
     *         </ul>
     */
    private int getAlertDuration(Command command) {
        int delay;
        switch (command.toString()) {
            case LightStateConverter.ALERT_MODE_LONG_SELECT:
                delay = 15000;
                break;
            case LightStateConverter.ALERT_MODE_SELECT:
                delay = 2000;
                break;
            default:
                delay = -1;
                break;
        }

        return delay;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(LightActions.class);
    }
}

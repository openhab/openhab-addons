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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ButtonEventType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ContactStateType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.EffectType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SceneRecallAction;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SmartSceneRecallAction;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SmartSceneState;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.TamperStateType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.openhab.core.util.ColorUtil.Gamut;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Complete Resource information DTO for CLIP 2.
 *
 * Note: all fields are @Nullable because some cases do not (must not) use them.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Resource {

    public static final MathContext PERCENT_MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);

    /**
     * The SSE event mechanism sends resources in a sparse (skeleton) format that only includes state fields whose
     * values have changed. A sparse resource does not contain the full state of the resource. And the absence of any
     * field from such a resource does not indicate that the field value is UNDEF, but rather that the value is the same
     * as what it was previously set to by the last non-sparse resource.
     */
    private transient boolean hasSparseData;

    private @Nullable String type;
    private @Nullable String id;
    private @Nullable @SerializedName("bridge_id") String bridgeId;
    private @Nullable @SerializedName("id_v1") String idV1;
    private @Nullable ResourceReference owner;
    private @Nullable MetaData metadata;
    private @Nullable @SerializedName("product_data") ProductData productData;
    private @Nullable List<ResourceReference> services;
    private @Nullable OnState on;
    private @Nullable Dimming dimming;
    private @Nullable @SerializedName("color_temperature") ColorTemperature colorTemperature;
    private @Nullable ColorXy color;
    private @Nullable Alerts alert;
    private @Nullable Effects effects;
    private @Nullable @SerializedName("timed_effects") TimedEffects timedEffects;
    private @Nullable ResourceReference group;
    private @Nullable List<ActionEntry> actions;
    private @Nullable Recall recall;
    private @Nullable Boolean enabled;
    private @Nullable LightLevel light;
    private @Nullable Button button;
    private @Nullable Temperature temperature;
    private @Nullable Motion motion;
    private @Nullable @SerializedName("power_state") Power powerState;
    private @Nullable @SerializedName("relative_rotary") RelativeRotary relativeRotary;
    private @Nullable List<ResourceReference> children;
    private @Nullable JsonElement status;
    private @Nullable Dynamics dynamics;
    private @Nullable @SerializedName("contact_report") ContactReport contactReport;
    private @Nullable @SerializedName("tamper_reports") List<TamperReport> tamperReports;
    private @Nullable String state;

    /**
     * Constructor
     *
     * @param resourceType
     */
    public Resource(@Nullable ResourceType resourceType) {
        if (Objects.nonNull(resourceType)) {
            setType(resourceType);
        }
    }

    /**
     * Check if <code>light</code> or <code>grouped_light</code> resource contains any
     * relevant fields to process according to its type.
     *
     * As an example, {@link #colorTemperature} is relevant for a <code>light</code>
     * resource because it's needed for updating the color-temperature channels.
     *
     * @return true is resource contains any relevant field
     */
    public boolean hasAnyRelevantField() {
        return switch (getType()) {
            // https://developers.meethue.com/develop/hue-api-v2/api-reference/#resource_light_get
            case LIGHT -> hasHSBField() || colorTemperature != null || dynamics != null || effects != null
                    || timedEffects != null;
            // https://developers.meethue.com/develop/hue-api-v2/api-reference/#resource_grouped_light_get
            case GROUPED_LIGHT -> on != null || dimming != null || alert != null;
            default -> throw new IllegalStateException(type + " is not supported by hasAnyRelevantField()");
        };
    }

    /**
     * Check if resource contains any field which is needed to represent an HSB value
     * (<code>on</code>, <code>dimming</code> or <code>color</code>).
     *
     * @return true if resource has any HSB field
     */
    public boolean hasHSBField() {
        return on != null || dimming != null || color != null;
    }

    public @Nullable List<ActionEntry> getActions() {
        return actions;
    }

    public @Nullable Alerts getAlerts() {
        return alert;
    }

    public State getAlertState() {
        Alerts alerts = this.alert;
        if (Objects.nonNull(alerts)) {
            if (!alerts.getActionValues().isEmpty()) {
                ActionType alertType = alerts.getAction();
                if (Objects.nonNull(alertType)) {
                    return new StringType(alertType.name());
                }
                return new StringType(ActionType.NO_ACTION.name());
            }
        }
        return UnDefType.NULL;
    }

    public String getArchetype() {
        MetaData metaData = getMetaData();
        if (Objects.nonNull(metaData)) {
            return metaData.getArchetype().toString();
        }
        return getType().toString();
    }

    public State getBatteryLevelState() {
        Power powerState = this.powerState;
        return Objects.nonNull(powerState) ? powerState.getBatteryLevelState() : UnDefType.NULL;
    }

    public State getBatteryLowState() {
        Power powerState = this.powerState;
        return Objects.nonNull(powerState) ? powerState.getBatteryLowState() : UnDefType.NULL;
    }

    public @Nullable String getBridgeId() {
        String bridgeId = this.bridgeId;
        return Objects.isNull(bridgeId) || bridgeId.isBlank() ? null : bridgeId;
    }

    /**
     * Get the brightness as a PercentType. If off the brightness is 0, otherwise use dimming value.
     *
     * @return a PercentType with the dimming state, or UNDEF, or NULL
     */
    public State getBrightnessState() {
        Dimming dimming = this.dimming;
        if (Objects.nonNull(dimming)) {
            try {
                // if off the brightness is 0, otherwise it is the larger of dimming value or minimum dimming level
                OnState on = this.on;
                double brightness;
                if (Objects.nonNull(on) && !on.isOn()) {
                    brightness = 0f;
                } else {
                    Double minimumDimmingLevel = dimming.getMinimumDimmingLevel();
                    brightness = Math.max(Objects.nonNull(minimumDimmingLevel) ? minimumDimmingLevel
                            : Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL, Math.min(100f, dimming.getBrightness()));
                }
                return new PercentType(new BigDecimal(brightness, PERCENT_MATH_CONTEXT));
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public @Nullable Button getButton() {
        return button;
    }

    /**
     * Get the state corresponding to a button's last event value multiplied by the controlId found for it in the given
     * controlIds map. States are decimal values formatted like '1002' where the first digit is the button's controlId
     * and the last digit is the ordinal value of the button's last event.
     *
     * @param controlIds the map of control ids to be referenced.
     * @return the state.
     */
    public State getButtonEventState(Map<String, Integer> controlIds) {
        Button button = this.button;
        if (button == null) {
            return UnDefType.NULL;
        }
        ButtonEventType event;
        ButtonReport buttonReport = button.getButtonReport();
        if (buttonReport == null) {
            event = button.getLastEvent();
        } else {
            event = buttonReport.getLastEvent();
        }
        if (event == null) {
            return UnDefType.NULL;
        }
        return new DecimalType((controlIds.getOrDefault(getId(), 0).intValue() * 1000) + event.ordinal());
    }

    public State getButtonLastUpdatedState(ZoneId zoneId) {
        Button button = this.button;
        if (button == null) {
            return UnDefType.NULL;
        }
        ButtonReport buttonReport = button.getButtonReport();
        if (buttonReport == null) {
            return UnDefType.UNDEF;
        }
        Instant lastChanged = buttonReport.getLastChanged();
        if (Instant.EPOCH.equals(lastChanged)) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(lastChanged, zoneId));
    }

    public List<ResourceReference> getChildren() {
        List<ResourceReference> children = this.children;
        return Objects.nonNull(children) ? children : List.of();
    }

    /**
     * Get the color as an HSBType. This returns an HSB that is based on an amalgamation of the color xy, dimming, and
     * on/off JSON elements. It takes its 'H' and 'S' parts from the 'ColorXy' JSON element, and its 'B' part from the
     * on/off resp. dimming JSON elements. If off the B part is 0, otherwise it is the dimming element value. Note: this
     * method is only to be used on cached state DTOs which already have a defined color gamut.
     *
     * @return an HSBType containing the current color and brightness level, or UNDEF or NULL.
     */
    public State getColorState() {
        ColorXy color = this.color;
        if (Objects.nonNull(color)) {
            try {
                HSBType hsb = ColorUtil.xyToHsb(color.getXY());
                OnState on = this.on;
                Dimming dimming = this.dimming;
                double brightness = Objects.nonNull(on) && !on.isOn() ? 0
                        : Objects.nonNull(dimming) ? Math.max(0, Math.min(100, dimming.getBrightness())) : 50;
                return new HSBType(hsb.getHue(), hsb.getSaturation(),
                        new PercentType(new BigDecimal(brightness, PERCENT_MATH_CONTEXT)));
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public @Nullable ColorTemperature getColorTemperature() {
        return colorTemperature;
    }

    public State getColorTemperatureAbsoluteState() {
        ColorTemperature colorTemp = colorTemperature;
        if (Objects.nonNull(colorTemp)) {
            try {
                QuantityType<?> colorTemperature = colorTemp.getAbsolute();
                if (Objects.nonNull(colorTemperature)) {
                    return colorTemperature;
                }
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    /**
     * Get the colour temperature in percent. Note: this method is only to be used on cached state DTOs which already
     * have a defined mirek schema.
     *
     * @return a PercentType with the colour temperature percentage.
     */
    public State getColorTemperaturePercentState() {
        ColorTemperature colorTemperature = this.colorTemperature;
        if (Objects.nonNull(colorTemperature)) {
            try {
                Double percent = colorTemperature.getPercent();
                if (Objects.nonNull(percent)) {
                    return new PercentType(new BigDecimal(percent, PERCENT_MATH_CONTEXT));
                }
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public @Nullable ColorXy getColorXy() {
        return color;
    }

    /**
     * Return an HSB where the HS part is derived from the color xy JSON element (only), so the B part is 100%
     *
     * @return an HSBType.
     */
    public State getColorXyState() {
        ColorXy color = this.color;
        if (Objects.nonNull(color)) {
            try {
                HSBType hsb = ColorUtil.xyToHsb(color.getXY());
                return new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public State getContactLastUpdatedState(ZoneId zoneId) {
        ContactReport contactReport = this.contactReport;
        return Objects.nonNull(contactReport)
                ? new DateTimeType(ZonedDateTime.ofInstant(contactReport.getLastChanged(), zoneId))
                : UnDefType.NULL;
    }

    public State getContactState() {
        ContactReport contactReport = this.contactReport;
        return Objects.isNull(contactReport) ? UnDefType.NULL
                : ContactStateType.CONTACT == contactReport.getContactState() ? OpenClosedType.CLOSED
                        : OpenClosedType.OPEN;
    }

    public int getControlId() {
        MetaData metadata = this.metadata;
        return Objects.nonNull(metadata) ? metadata.getControlId() : 0;
    }

    public @Nullable Dimming getDimming() {
        return dimming;
    }

    /**
     * Return a PercentType which is derived from the dimming JSON element (only).
     *
     * @return a PercentType.
     */
    public State getDimmingState() {
        Dimming dimming = this.dimming;
        if (Objects.nonNull(dimming)) {
            try {
                double dimmingValue = Math.max(0f, Math.min(100f, dimming.getBrightness()));
                return new PercentType(new BigDecimal(dimmingValue, PERCENT_MATH_CONTEXT));
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public @Nullable Effects getFixedEffects() {
        return effects;
    }

    /**
     * Get the amalgamated effect state. The result may be either from an 'effects' field or from a 'timedEffects'
     * field. If both fields are missing it returns UnDefType.NULL, otherwise if either field is present and has an
     * active value (other than EffectType.NO_EFFECT) it returns a StringType of the name of the respective active
     * effect; and if none of the above apply, it returns a StringType of 'NO_EFFECT'.
     *
     * @return either a StringType value or UnDefType.NULL
     */
    public State getEffectState() {
        Effects effects = this.effects;
        TimedEffects timedEffects = this.timedEffects;
        if (Objects.isNull(effects) && Objects.isNull(timedEffects)) {
            return UnDefType.NULL;
        }
        EffectType effect = Objects.nonNull(effects) ? effects.getStatus() : null;
        if (Objects.nonNull(effect) && effect != EffectType.NO_EFFECT) {
            return new StringType(effect.name());
        }
        EffectType timedEffect = Objects.nonNull(timedEffects) ? timedEffects.getStatus() : null;
        if (Objects.nonNull(timedEffect) && timedEffect != EffectType.NO_EFFECT) {
            return new StringType(timedEffect.name());
        }
        return new StringType(EffectType.NO_EFFECT.name());
    }

    public @Nullable Boolean getEnabled() {
        return enabled;
    }

    public State getEnabledState() {
        Boolean enabled = this.enabled;
        return Objects.nonNull(enabled) ? OnOffType.from(enabled.booleanValue()) : UnDefType.NULL;
    }

    public @Nullable Gamut getGamut() {
        ColorXy color = this.color;
        return Objects.nonNull(color) ? color.getGamut() : null;
    }

    public @Nullable ResourceReference getGroup() {
        return group;
    }

    public String getId() {
        String id = this.id;
        return Objects.nonNull(id) ? id : "";
    }

    public String getIdV1() {
        String idV1 = this.idV1;
        return Objects.nonNull(idV1) ? idV1 : "";
    }

    public @Nullable LightLevel getLightLevel() {
        return light;
    }

    public State getLightLevelState() {
        LightLevel lightLevel = this.light;
        if (lightLevel == null) {
            return UnDefType.NULL;
        }
        LightLevelReport lightLevelReport = lightLevel.getLightLevelReport();
        if (lightLevelReport == null) {
            return lightLevel.getLightLevelState();
        }
        return new QuantityType<>(Math.pow(10f, (double) lightLevelReport.getLightLevel() / 10000f) - 1f, Units.LUX);
    }

    public State getLightLevelLastUpdatedState(ZoneId zoneId) {
        LightLevel lightLevel = this.light;
        if (lightLevel == null) {
            return UnDefType.NULL;
        }
        LightLevelReport lightLevelReport = lightLevel.getLightLevelReport();
        if (lightLevelReport == null) {
            return UnDefType.UNDEF;
        }
        Instant lastChanged = lightLevelReport.getLastChanged();
        if (Instant.EPOCH.equals(lastChanged)) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(lastChanged, zoneId));
    }

    public @Nullable MetaData getMetaData() {
        return metadata;
    }

    public @Nullable Double getMinimumDimmingLevel() {
        Dimming dimming = this.dimming;
        return Objects.nonNull(dimming) ? dimming.getMinimumDimmingLevel() : null;
    }

    public @Nullable MirekSchema getMirekSchema() {
        ColorTemperature colorTemp = this.colorTemperature;
        return Objects.nonNull(colorTemp) ? colorTemp.getMirekSchema() : null;
    }

    public @Nullable Motion getMotion() {
        return motion;
    }

    public State getMotionState() {
        Motion motion = this.motion;
        if (motion == null) {
            return UnDefType.NULL;
        }
        MotionReport motionReport = motion.getMotionReport();
        if (motionReport == null) {
            return motion.getMotionState();
        }
        return OnOffType.from(motionReport.isMotion());
    }

    public State getMotionLastUpdatedState(ZoneId zoneId) {
        Motion motion = this.motion;
        if (motion == null) {
            return UnDefType.NULL;
        }
        MotionReport motionReport = motion.getMotionReport();
        if (motionReport == null) {
            return UnDefType.UNDEF;
        }
        Instant lastChanged = motionReport.getLastChanged();
        if (Instant.EPOCH.equals(lastChanged)) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(lastChanged, zoneId));
    }

    public State getMotionValidState() {
        Motion motion = this.motion;
        return Objects.nonNull(motion) ? motion.getMotionValidState() : UnDefType.NULL;
    }

    public String getName() {
        MetaData metaData = getMetaData();
        if (Objects.nonNull(metaData)) {
            String name = metaData.getName();
            if (Objects.nonNull(name)) {
                return name;
            }
        }
        return getType().toString();
    }

    /**
     * Return the state of the On/Off element (only).
     */
    public State getOnOffState() {
        try {
            OnState on = this.on;
            return Objects.nonNull(on) ? OnOffType.from(on.isOn()) : UnDefType.NULL;
        } catch (DTOPresentButEmptyException e) {
            return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
        }
    }

    public @Nullable OnState getOnState() {
        return on;
    }

    public @Nullable ResourceReference getOwner() {
        return owner;
    }

    public @Nullable Power getPowerState() {
        return powerState;
    }

    public @Nullable ProductData getProductData() {
        return productData;
    }

    public String getProductName() {
        ProductData productData = getProductData();
        if (Objects.nonNull(productData)) {
            return productData.getProductName();
        }
        return getType().toString();
    }

    public @Nullable Recall getRecall() {
        return recall;
    }

    public @Nullable RelativeRotary getRelativeRotary() {
        return relativeRotary;
    }

    public State getRotaryStepsState() {
        RelativeRotary relativeRotary = this.relativeRotary;
        if (relativeRotary == null) {
            return UnDefType.NULL;
        }
        RotaryReport rotaryReport = relativeRotary.getRotaryReport();
        if (rotaryReport == null) {
            return relativeRotary.getStepsState();
        }
        Rotation rotation = rotaryReport.getRotation();
        if (rotation == null) {
            return UnDefType.NULL;
        }
        return rotation.getStepsState();
    }

    public State getRotaryStepsLastUpdatedState(ZoneId zoneId) {
        RelativeRotary relativeRotary = this.relativeRotary;
        if (relativeRotary == null) {
            return UnDefType.NULL;
        }
        RotaryReport rotaryReport = relativeRotary.getRotaryReport();
        if (rotaryReport == null) {
            return UnDefType.UNDEF;
        }
        Instant lastChanged = rotaryReport.getLastChanged();
        if (Instant.EPOCH.equals(lastChanged)) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(lastChanged, zoneId));
    }

    /**
     * Check if the scene resource contains a 'status.active' element. If such an element is present, returns a Boolean
     * Optional whose value depends on the value of that element, or an empty Optional if it is not.
     *
     * @return true, false, or empty.
     */
    public Optional<Boolean> getSceneActive() {
        if (ResourceType.SCENE == getType()) {
            JsonElement status = this.status;
            if (Objects.nonNull(status) && status.isJsonObject()) {
                JsonElement active = ((JsonObject) status).get("active");
                if (Objects.nonNull(active) && active.isJsonPrimitive()) {
                    return Optional.of(!"inactive".equalsIgnoreCase(active.getAsString()));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * If the getSceneActive() optional result is empty return 'UnDefType.NULL'. Otherwise if the optional result is
     * present and 'true' (i.e. the scene is active) return the scene name. Or finally (the optional result is present
     * and 'false') return 'UnDefType.UNDEF'.
     *
     * @return either 'UnDefType.NULL', a StringType containing the (active) scene name, or 'UnDefType.UNDEF'.
     */
    public State getSceneState() {
        return getSceneActive().map(a -> a ? new StringType(getName()) : UnDefType.UNDEF).orElse(UnDefType.NULL);
    }

    /**
     * Check if the smart scene resource contains a 'state' element. If such an element is present, returns a Boolean
     * Optional whose value depends on the value of that element, or an empty Optional if it is not.
     *
     * @return true, false, or empty.
     */
    public Optional<Boolean> getSmartSceneActive() {
        if (ResourceType.SMART_SCENE == getType()) {
            String state = this.state;
            if (Objects.nonNull(state)) {
                return Optional.of(SmartSceneState.ACTIVE == SmartSceneState.of(state));
            }
        }
        return Optional.empty();
    }

    /**
     * If the getSmartSceneActive() optional result is empty return 'UnDefType.NULL'. Otherwise if the optional result
     * is present and 'true' (i.e. the scene is active) return the smart scene name. Or finally (the optional result is
     * present and 'false') return 'UnDefType.UNDEF'.
     *
     * @return either 'UnDefType.NULL', a StringType containing the (active) scene name, or 'UnDefType.UNDEF'.
     */
    public State getSmartSceneState() {
        return getSmartSceneActive().map(a -> a ? new StringType(getName()) : UnDefType.UNDEF).orElse(UnDefType.NULL);
    }

    public List<ResourceReference> getServiceReferences() {
        List<ResourceReference> services = this.services;
        return Objects.nonNull(services) ? services : List.of();
    }

    public JsonObject getStatus() {
        JsonElement status = this.status;
        if (Objects.nonNull(status) && status.isJsonObject()) {
            return status.getAsJsonObject();
        }
        return new JsonObject();
    }

    public State getTamperLastUpdatedState(ZoneId zoneId) {
        TamperReport report = getTamperReportsLatest();
        return Objects.nonNull(report) ? new DateTimeType(ZonedDateTime.ofInstant(report.getLastChanged(), zoneId))
                : UnDefType.NULL;
    }

    /**
     * The the Hue bridge could return its raw list of tamper reports in any order, so sort the list (latest entry
     * first) according to the respective 'changed' instant and return the first entry i.e. the latest changed entry.
     *
     * @return the latest changed tamper report
     */
    private @Nullable TamperReport getTamperReportsLatest() {
        List<TamperReport> reports = this.tamperReports;
        return Objects.nonNull(reports)
                ? reports.stream().sorted((e1, e2) -> e2.getLastChanged().compareTo(e1.getLastChanged())).findFirst()
                        .orElse(null)
                : null;
    }

    public State getTamperState() {
        TamperReport report = getTamperReportsLatest();
        return Objects.nonNull(report)
                ? TamperStateType.TAMPERED == report.getTamperState() ? OpenClosedType.OPEN : OpenClosedType.CLOSED
                : UnDefType.NULL;
    }

    public @Nullable Temperature getTemperature() {
        return temperature;
    }

    public State getTemperatureState() {
        Temperature temperature = this.temperature;
        if (temperature == null) {
            return UnDefType.NULL;
        }
        TemperatureReport temperatureReport = temperature.getTemperatureReport();
        if (temperatureReport == null) {
            return temperature.getTemperatureState();
        }
        return new QuantityType<>(temperatureReport.getTemperature(), SIUnits.CELSIUS);
    }

    public State getTemperatureLastUpdatedState(ZoneId zoneId) {
        Temperature temperature = this.temperature;
        if (temperature == null) {
            return UnDefType.NULL;
        }
        TemperatureReport temperatureReport = temperature.getTemperatureReport();
        if (temperatureReport == null) {
            return UnDefType.UNDEF;
        }
        Instant lastChanged = temperatureReport.getLastChanged();
        if (Instant.EPOCH.equals(lastChanged)) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(lastChanged, zoneId));
    }

    public State getTemperatureValidState() {
        Temperature temperature = this.temperature;
        return Objects.nonNull(temperature) ? temperature.getTemperatureValidState() : UnDefType.NULL;
    }

    public @Nullable TimedEffects getTimedEffects() {
        return timedEffects;
    }

    public ResourceType getType() {
        return ResourceType.of(type);
    }

    public State getZigbeeState() {
        ZigbeeStatus zigbeeStatus = getZigbeeStatus();
        return Objects.nonNull(zigbeeStatus) ? new StringType(zigbeeStatus.toString()) : UnDefType.NULL;
    }

    public @Nullable ZigbeeStatus getZigbeeStatus() {
        JsonElement status = this.status;
        if (Objects.nonNull(status) && status.isJsonPrimitive()) {
            return ZigbeeStatus.of(status.getAsString());
        }
        return null;
    }

    public boolean hasFullState() {
        return !hasSparseData;
    }

    /**
     * Mark that the resource has sparse data.
     *
     * @return this instance.
     */
    public Resource markAsSparse() {
        hasSparseData = true;
        return this;
    }

    public Resource setAlerts(Alerts alert) {
        this.alert = alert;
        return this;
    }

    public Resource setColorTemperature(ColorTemperature colorTemperature) {
        this.colorTemperature = colorTemperature;
        return this;
    }

    public Resource setColorXy(@Nullable ColorXy color) {
        this.color = color;
        return this;
    }

    public Resource setContactReport(ContactReport contactReport) {
        this.contactReport = contactReport;
        return this;
    }

    public Resource setDimming(@Nullable Dimming dimming) {
        this.dimming = dimming;
        return this;
    }

    public Resource setDynamicsDuration(Duration duration) {
        dynamics = new Dynamics().setDuration(duration);
        return this;
    }

    public Resource setFixedEffects(Effects effect) {
        this.effects = effect;
        return this;
    }

    public Resource setEnabled(Command command) {
        if (command instanceof OnOffType) {
            this.enabled = ((OnOffType) command) == OnOffType.ON;
        }
        return this;
    }

    public Resource setId(String id) {
        this.id = id;
        return this;
    }

    public Resource setMetadata(MetaData metadata) {
        this.metadata = metadata;
        return this;
    }

    public Resource setMirekSchema(@Nullable MirekSchema schema) {
        ColorTemperature colorTemperature = this.colorTemperature;
        if (Objects.nonNull(colorTemperature)) {
            colorTemperature.setMirekSchema(schema);
        }
        return this;
    }

    /**
     * Set the on/off JSON element (only).
     *
     * @param command an OnOffTypee command value.
     * @return this resource instance.
     */
    public Resource setOnOff(Command command) {
        if (command instanceof OnOffType) {
            OnOffType onOff = (OnOffType) command;
            OnState on = this.on;
            on = Objects.nonNull(on) ? on : new OnState();
            on.setOn(OnOffType.ON.equals(onOff));
            this.on = on;
        }
        return this;
    }

    public Resource setOnState(@Nullable OnState on) {
        this.on = on;
        return this;
    }

    public Resource setRecallAction(SceneRecallAction recallAction) {
        Recall recall = this.recall;
        this.recall = ((Objects.nonNull(recall) ? recall : new Recall())).setAction(recallAction);
        return this;
    }

    public Resource setRecallAction(SmartSceneRecallAction recallAction) {
        Recall recall = this.recall;
        this.recall = ((Objects.nonNull(recall) ? recall : new Recall())).setAction(recallAction);
        return this;
    }

    public Resource setRecallDuration(Duration recallDuration) {
        Recall recall = this.recall;
        this.recall = ((Objects.nonNull(recall) ? recall : new Recall())).setDuration(recallDuration);
        return this;
    }

    public Resource setTamperReports(List<TamperReport> tamperReports) {
        this.tamperReports = tamperReports;
        return this;
    }

    public Resource setTimedEffects(TimedEffects timedEffects) {
        this.timedEffects = timedEffects;
        return this;
    }

    public Resource setTimedEffectsDuration(Duration dynamicsDuration) {
        TimedEffects timedEffects = this.timedEffects;
        if (Objects.nonNull(timedEffects)) {
            timedEffects.setDuration(dynamicsDuration);
        }
        return this;
    }

    public Resource setType(ResourceType resourceType) {
        this.type = resourceType.name().toLowerCase();
        return this;
    }

    @Override
    public String toString() {
        String id = this.id;
        return String.format("id:%s, type:%s", Objects.nonNull(id) ? id : "?" + " ".repeat(35),
                getType().name().toLowerCase());
    }
}

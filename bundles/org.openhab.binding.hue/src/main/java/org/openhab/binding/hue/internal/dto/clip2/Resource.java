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
package org.openhab.binding.hue.internal.dto.clip2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.RecallAction;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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

    public static final double PERCENT_DELTA = 30f;
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
    private @Nullable @SuppressWarnings("unused") Dynamics dynamics;

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
                // if off the brightness is 0, otherwise it is dimming value
                OnState on = this.on;
                double brightness = Objects.nonNull(on) && !on.isOn() ? 0f
                        : Math.max(0f, Math.min(100f, dimming.getBrightness()));
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
        if (Objects.nonNull(button)) {
            try {
                return new DecimalType(
                        (controlIds.getOrDefault(getId(), 0).intValue() * 1000) + button.getLastEvent().ordinal());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return UnDefType.NULL;
    }

    public State getButtonLastEventState() {
        Button button = this.button;
        return Objects.nonNull(button) ? button.getLastEventState() : UnDefType.NULL;
    }

    public List<ResourceReference> getChildren() {
        List<ResourceReference> children = this.children;
        return Objects.nonNull(children) ? children : List.of();
    }

    /**
     * Get the color as an HSBType. This returns an HSB that is based on an amalgamation of the color xy, dimming, and
     * on/off JSON elements. It takes its 'H' & 'S' parts from the 'ColorXy' JSON element, and its 'B' part from the
     * on/off resp. dimming JSON elements. If off the B part is 0, otherwise it is the dimming element value. Note: this
     * method is only to be used on cached state DTOs which already have a defined color gamut.
     *
     * @return an HSBType containing the current color and brightness level, or UNDEF or NULL.
     */
    public State getColorState() {
        ColorXy color = this.color;
        if (Objects.nonNull(color)) {
            try {
                Gamut gamut = color.getGamut();
                gamut = Objects.nonNull(gamut) ? gamut : ColorUtil.DEFAULT_GAMUT;
                HSBType hsb = ColorUtil.xyToHsb(color.getXY(), gamut);
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
                Gamut gamut = color.getGamut();
                gamut = Objects.nonNull(gamut) ? gamut : ColorUtil.DEFAULT_GAMUT;
                HSBType hsb = ColorUtil.xyToHsb(color.getXY(), gamut);
                return new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
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

    public @Nullable Effects getEffects() {
        return effects;
    }

    public State getEffectState() {
        Effects effects = this.effects;
        return Objects.nonNull(effects) ? new StringType(effects.getStatus().name()) : UnDefType.NULL;
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
        LightLevel light = this.light;
        return Objects.nonNull(light) ? light.getLightLevelState() : UnDefType.NULL;
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
        return Objects.nonNull(motion) ? motion.getMotionState() : UnDefType.NULL;
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

    public State getRelativeRotaryActionState() {
        RelativeRotary relativeRotary = this.relativeRotary;
        return Objects.nonNull(relativeRotary) ? relativeRotary.getActionState() : UnDefType.NULL;
    }

    public State getRotaryStepsState() {
        RelativeRotary relativeRotary = this.relativeRotary;
        return Objects.nonNull(relativeRotary) ? relativeRotary.getStepsState() : UnDefType.NULL;
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
        Optional<Boolean> active = getSceneActive();
        return active.isEmpty() ? UnDefType.NULL : active.get() ? new StringType(getName()) : UnDefType.UNDEF;
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

    public @Nullable Temperature getTemperature() {
        return temperature;
    }

    public State getTemperatureState() {
        Temperature temperature = this.temperature;
        return Objects.nonNull(temperature) ? temperature.getTemperatureState() : UnDefType.NULL;
    }

    public State getTemperatureValidState() {
        Temperature temperature = this.temperature;
        return Objects.nonNull(temperature) ? temperature.getTemperatureValidState() : UnDefType.NULL;
    }

    public @Nullable Effects getTimedEffects() {
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

    public Resource setColorXy(ColorXy color) {
        this.color = color;
        return this;
    }

    public Resource setDimming(Dimming dimming) {
        this.dimming = dimming;
        return this;
    }

    public Resource setDynamicsDuration(Duration duration) {
        dynamics = new Dynamics().setDuration(duration);
        return this;
    }

    public Resource setEffects(Effects effect) {
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

    public void setOnState(OnState on) {
        this.on = on;
    }

    public Resource setRecallAction(RecallAction recallAction) {
        Recall recall = this.recall;
        this.recall = ((Objects.nonNull(recall) ? recall : new Recall())).setAction(recallAction);
        return this;
    }

    public Resource setRecallDuration(Duration recallDuration) {
        Recall recall = this.recall;
        this.recall = ((Objects.nonNull(recall) ? recall : new Recall())).setDuration(recallDuration);
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

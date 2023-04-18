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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.EffectType;
import org.openhab.binding.hue.internal.dto.clip2.enums.RecallAction;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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

    private static final int DELTA = 30;

    /**
     * Static method to get a new percent value depending on the type of command and if relevant the current value.
     *
     * @param command either a PercentType with the new value, an OnOffType to set it at 0 / 100 percent, or an
     *            IncreaseDecreaseType to increment the percentage value by a fixed amount.
     * @param old the current percent value.
     * @return the new PercentType value, or null if the command was not recognised.
     */
    private static State getPercentType(Command command, State current) {
        if (command instanceof PercentType) {
            return (PercentType) command;
        } else if (command instanceof OnOffType) {
            return OnOffType.ON.equals(command) ? PercentType.HUNDRED : PercentType.ZERO;
        } else if (command instanceof IncreaseDecreaseType && current instanceof PercentType) {
            int sign = IncreaseDecreaseType.INCREASE.equals(command) ? 1 : -1;
            double percent = ((PercentType) current).doubleValue() + (sign * DELTA);
            return new PercentType(BigDecimal.valueOf(Math.min(100, Math.max(0, percent))));
        }
        return UnDefType.NULL;
    }

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
    private @Nullable @SerializedName("color_temperature") ColorTemperature2 colorTemperature;
    private @Nullable ColorXy color;
    private @Nullable Alerts alert;
    private @Nullable Effects effects;
    private @Nullable @SerializedName("timed_effects") Effects timedEffects;
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
     * Put this resource's control id in the given map of control ids.
     *
     * @param controlIds the map of control ids to be updated.
     * @return this resource instance.
     */
    public Resource addControlIdToMap(Map<String, Integer> controlIds) {
        if (!hasSparseData) {
            MetaData metadata = this.metadata;
            controlIds.put(getId(), Objects.nonNull(metadata) ? metadata.getControlId() : 0);
        }
        return this;
    }

    /**
     * Method that copies required fields from another Resource instance into this instance. If the field in this
     * instance is null and the same field in the other instance is not null, then the value from the other instance is
     * copied to this instance. This method allows 'hasSparseData' resources to expand themselves to include necessary
     * fields taken over from a previously cached full data DTO.
     *
     * @param other the other resource instance.
     * @return this instance.
     */
    public Resource copyMissingFieldsFrom(Resource other) {
        // dimming
        if (Objects.isNull(this.dimming) && Objects.nonNull(other.dimming)) {
            this.dimming = other.dimming;
        }
        // color
        if (Objects.isNull(this.color) && Objects.nonNull(other.color)) {
            this.color = other.color;
        }
        // gamut
        ColorXy oC = other.color;
        Gamut oG = Objects.nonNull(oC) ? oC.getGamut() : null;
        if (Objects.nonNull(oG)) {
            ColorXy tC = this.color;
            this.color = (Objects.nonNull(tC) ? tC : new ColorXy()).setGamut(oG);
        }
        // mirek schema
        ColorTemperature2 oCT = other.colorTemperature;
        MirekSchema oMS = Objects.nonNull(oCT) ? oCT.getMirekSchema() : null;
        if (Objects.nonNull(oMS)) {
            ColorTemperature2 tCT = this.colorTemperature;
            this.colorTemperature = (Objects.nonNull(tCT) ? tCT : new ColorTemperature2()).setMirekSchema(oMS);
        }
        // metadata
        if (Objects.isNull(this.metadata) && Objects.nonNull(other.metadata)) {
            this.metadata = other.metadata;
        }
        // alerts
        if (Objects.isNull(this.alert) && Objects.nonNull(other.alert)) {
            this.alert = other.alert;
        }
        // effects
        if (Objects.isNull(this.effects) && Objects.nonNull(other.effects)) {
            this.effects = other.effects;
        }
        // effects values
        Effects oE = other.effects;
        List<String> oESV = Objects.nonNull(oE) ? oE.getStatusValues() : null;
        if (Objects.nonNull(oESV)) {
            Effects tE = this.effects;
            this.effects = (Objects.nonNull(tE) ? tE : new Effects()).setStatusValues(oESV);
        }
        return this;
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

    public State getBrightnessState() {
        Dimming dimming = this.dimming;
        try {
            return Objects.nonNull(dimming) ? new PercentType(BigDecimal.valueOf(dimming.getBrightness()))
                    : UnDefType.NULL;
        } catch (DTOPresentButEmptyException e) {
            return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
        }
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
     * Get the color as an HSBType. Take its Hue & Saturation parts from the 'ColorXy' JSON element, and take its
     * Brightness part from the 'Dimming' JSON element.
     *
     * @return an HSBType containing the current color and brightness level.
     */
    public State getColorState() {
        ColorXy color = this.color;
        if (Objects.nonNull(color)) {
            try {
                Gamut gamut = color.getGamut();
                gamut = Objects.nonNull(gamut) ? gamut : ColorUtil.DEFAULT_GAMUT;
                HSBType hsb = ColorUtil.xyToHsb(color.getXY(), gamut);
                Dimming dimming = this.dimming;
                double b = Objects.nonNull(dimming) ? Math.max(0, Math.min(100, dimming.getBrightness())) : 50;
                return new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(BigDecimal.valueOf(b)));
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    public @Nullable ColorTemperature2 getColorTemperature() {
        return colorTemperature;
    }

    public State getColorTemperatureKelvinState() {
        ColorTemperature2 colorTemp = colorTemperature;
        if (Objects.nonNull(colorTemp)) {
            try {
                Double kelvin = colorTemp.getKelvin();
                if (Objects.nonNull(kelvin)) {
                    return new QuantityType<>(kelvin, Units.KELVIN);
                }
            } catch (DTOPresentButEmptyException e) {
                return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
            }
        }
        return UnDefType.NULL;
    }

    /**
     * Get the colour temperature in percent.
     *
     * @return a PercentType with the colour temperature percentage.
     */
    public State getColorTemperaturePercentState() {
        ColorTemperature2 colorTemperature = this.colorTemperature;
        if (Objects.nonNull(colorTemperature)) {
            try {
                Double percent = colorTemperature.getPercent();
                if (Objects.nonNull(percent)) {
                    return new PercentType(BigDecimal.valueOf(percent));
                }
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

    public @Nullable MirekSchema getMirekSchema() {
        ColorTemperature2 colorTemp = this.colorTemperature;
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

    public @Nullable Boolean getSceneActive() {
        JsonElement status = this.status;
        if (Objects.nonNull(status) && status.isJsonObject()) {
            JsonElement active = ((JsonObject) status).get("active");
            if (Objects.nonNull(active) && active.isJsonPrimitive()) {
                return !"inactive".equals(active.getAsString());
            }
        }
        return null;
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

    public State getSwitch() {
        try {
            OnState on = this.on;
            return Objects.nonNull(on) ? OnOffType.from(on.isOn()) : UnDefType.NULL;
        } catch (DTOPresentButEmptyException e) {
            return UnDefType.UNDEF; // indicates the DTO is present but its inner fields are missing
        }
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

    public Resource setAlert(Command command, @Nullable Resource other) {
        if ((command instanceof StringType) && Objects.nonNull(other)) {
            Alerts otherAlert = other.alert;
            if (Objects.nonNull(otherAlert)) {
                ActionType actionType = ActionType.of(((StringType) command).toString());
                if (otherAlert.getActionValues().contains(actionType)) {
                    this.alert = new Alerts().setAction(actionType);
                }
            }
        }
        return this;
    }

    /**
     * Set the brightness percent.
     *
     * @param command either a PercentType with the new value, an OnOffType to set it at 0 / 100 percent, or an
     *            IncreaseDecreaseType to increment the percentage value by a fixed amount.
     * @return this resource instance.
     */
    public Resource setBrightness(Command command) {
        State state = getPercentType(command, getBrightnessState());
        if (state instanceof PercentType) {
            Dimming dimming = this.dimming;
            dimming = Objects.nonNull(dimming) ? dimming : new Dimming();
            dimming.setBrightness(((PercentType) state).doubleValue());
            if (PercentType.ZERO.equals(state)) {
                setSwitch(OnOffType.OFF);
            }
            this.dimming = dimming;
        }
        return this;
    }

    /**
     * Set the color from an HSBType. If this resource has its own Gamut then use that, otherwise if the 'other'
     * parameter is not null and has a Gamut, then use that, and if neither have one then use the default Gamut. Put its
     * Hue and Saturation parts in the 'ColorXy' JSON element, and its Brightness part in the 'Dimming' JSON element.
     *
     * @param command an HSBType with the new color value.
     * @param other the reference (light) resource to be used for the Gamut.
     * @return this resource instance.
     */
    public Resource setColor(Command command, @Nullable Resource other) {
        if (command instanceof HSBType) {
            Gamut gamut = this.getGamut();
            gamut = Objects.nonNull(gamut) ? gamut : Objects.nonNull(other) ? other.getGamut() : null;
            gamut = Objects.nonNull(gamut) ? gamut : ColorUtil.DEFAULT_GAMUT;
            HSBType hsb = (HSBType) command;
            ColorXy col = color;
            Dimming dim = dimming;
            color = (Objects.nonNull(col) ? col : new ColorXy()).setXY(ColorUtil.hsbToXY(hsb, gamut));
            dimming = (Objects.nonNull(dim) ? dim : new Dimming()).setBrightness(hsb.getBrightness().doubleValue());
        }
        return this;
    }

    /**
     * Set the colour temperature in Kelvin.
     *
     * @param command should be a QuantityType(Temperature> (but it can also handle DecimalType).
     * @return this resource instance.
     */
    public Resource setColorTemperatureKelvin(Command command) {
        Double kelvin = null;
        if (command instanceof QuantityType<?>) {
            QuantityType<?> temperature = ((QuantityType<?>) command).toInvertibleUnit(Units.KELVIN);
            if (Objects.nonNull(temperature)) {
                kelvin = temperature.doubleValue();
            }
        } else if (command instanceof DecimalType) {
            kelvin = ((DecimalType) command).doubleValue();
        }
        if (Objects.nonNull(kelvin)) {
            ColorTemperature2 colorTemperature = this.colorTemperature;
            colorTemperature = Objects.nonNull(colorTemperature) ? colorTemperature : new ColorTemperature2();
            colorTemperature.setKelvin(kelvin);
            this.colorTemperature = colorTemperature;
        }
        return this;
    }

    /**
     * Set the color temperature from a PercentType. If this resource has its own MirekSchema then use that, otherwise
     * if the 'other' parameter is not null and has a MirekSchema, then use that, and if neither have one then use the
     * default MirekSchema.
     *
     * @param command a PercentType command value.
     * @param other the reference (light) resource to be used for the MirekSchema.
     * @return this resource instance.
     */
    public Resource setColorTemperaturePercent(Command command, @Nullable Resource other) {
        if (command instanceof PercentType) {
            MirekSchema schema = this.getMirekSchema();
            schema = Objects.nonNull(schema) ? schema : Objects.nonNull(other) ? other.getMirekSchema() : null;
            schema = Objects.nonNull(schema) ? schema : MirekSchema.DEFAULT_SCHEMA;
            ColorTemperature2 colorTemperature = this.colorTemperature;
            colorTemperature = Objects.nonNull(colorTemperature) ? colorTemperature : new ColorTemperature2();
            colorTemperature.setPercent(((PercentType) command).doubleValue(), schema);
            this.colorTemperature = colorTemperature;
        }
        return this;
    }

    public Resource setEffect(Command command, @Nullable Resource other) {
        if ((command instanceof StringType) && Objects.nonNull(other)) {
            Effects otherEffects = other.effects;
            if (Objects.nonNull(otherEffects)) {
                EffectType effectType = EffectType.of(((StringType) command).toString());
                if (otherEffects.allows(effectType)) {
                    this.effects = new Effects().setEffect(effectType);
                }
            }
        }
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

    public void setMirekSchema(@Nullable MirekSchema schema) {
        ColorTemperature2 colorTemperature = this.colorTemperature;
        if (Objects.nonNull(colorTemperature)) {
            colorTemperature.setMirekSchema(schema);
        }
    }

    public Resource setRecall() {
        Recall recall = new Recall();
        recall.setAction(RecallAction.ACTIVE);
        this.recall = recall;
        return this;
    }

    public Resource setSwitch(Command command) {
        if (command instanceof OnOffType) {
            OnState on = this.on;
            on = Objects.nonNull(on) ? on : new OnState();
            on.setOn(OnOffType.ON.equals(command));
            this.on = on;
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

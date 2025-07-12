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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * A MQTT light, following the
 * https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * Individual concrete classes implement the differing semantics of the
 * three different schemas.
 *
 * As of now, only on/off, brightness, and RGB are fully implemented and tested.
 * HS and XY are implemented, but not tested. Color temp is only
 * implemented (but not tested) for the default schema.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Re-write for (nearly) full support
 */
@NonNullByDefault
public abstract class Light<C extends Light.LightConfiguration> extends AbstractComponent<C>
        implements ChannelStateUpdateListener {
    private static final String BASIC_SCHEMA = "basic";
    private static final String JSON_SCHEMA = "json";
    private static final String TEMPLATE_SCHEMA = "template";

    protected static final String STATE_CHANNEL_ID = "state";
    protected static final String SWITCH_CHANNEL_ID = "switch";
    protected static final String BRIGHTNESS_CHANNEL_ID = "brightness";
    protected static final String COLOR_MODE_CHANNEL_ID = "color-mode";
    protected static final String COLOR_TEMP_CHANNEL_ID = "color-temp";
    protected static final String EFFECT_CHANNEL_ID = "effect";
    // This channel is a synthetic channel that may send to other channels
    // underneath
    protected static final String COLOR_CHANNEL_ID = "color";

    protected static final String DUMMY_TOPIC = "dummy";

    private static final BigDecimal BIG_DECIMAL_TWO_FIVE_FIVE = BigDecimal.valueOf(255);

    public static class LightConfiguration extends EntityConfiguration implements RWConfiguration {
        public LightConfiguration(Map<String, @Nullable Object> config, String defaultName) {
            super(config, defaultName);
        }

        String getSchema() {
            return getString("schema");
        }

        boolean getColorTempKelvin() {
            return getBoolean("color_temp_kelvin");
        }

        @Nullable
        List<String> getEffectList() {
            return getOptionalStringList("effect_list");
        }

        @Nullable
        Integer getMaxMireds() {
            return getOptionalInt("max_mireds");
        }

        @Nullable
        Integer getMinMireds() {
            return getOptionalInt("min_mireds");
        }

        @Nullable
        Integer getMaxKelvin() {
            return getOptionalInt("max_kelvin");
        }

        @Nullable
        Integer getMinKelvin() {
            return getOptionalInt("min_kelvin");
        }
    }

    protected final boolean optimistic;

    protected @Nullable ComponentChannel onOffChannel;
    protected @Nullable ComponentChannel brightnessChannel;
    protected @Nullable ComponentChannel colorChannel;

    // State has to be stored here, in order to mux multiple
    // MQTT sources into single OpenHAB channels
    protected PercentageValue brightnessValue;
    protected final NumberValue colorTempValue;
    protected final @Nullable TextValue effectValue;
    protected final ColorValue colorValue = new ColorValue(ColorMode.HSB, null, null, 100);

    protected final ChannelStateUpdateListener channelStateUpdateListener;

    public static Light<?> create(ComponentFactory.ComponentContext componentContext)
            throws UnsupportedComponentException {
        Map<String, @Nullable Object> config = componentContext.getPython()
                .processDiscoveryConfig(componentContext.getHaID().component, componentContext.getConfigJSON());

        String schema = Objects.requireNonNull((String) config.get("schema"));
        switch (schema) {
            case BASIC_SCHEMA:
                return new BasicSchemaLight(componentContext, config);
            case JSON_SCHEMA:
                return new JSONSchemaLight(componentContext, config);
            case TEMPLATE_SCHEMA:
                return new TemplateSchemaLight(componentContext, config);
            default:
                throw new UnsupportedComponentException(
                        "Component '" + componentContext.getHaID() + "' of schema '" + schema + "' is not supported!");
        }
    }

    protected Light(ComponentFactory.ComponentContext componentContext, C config) {
        super(componentContext, config);
        this.channelStateUpdateListener = componentContext.getUpdateListener();

        optimistic = config.isOptimistic() || config.getStateTopic() == null;
        brightnessValue = new PercentageValue(null, BIG_DECIMAL_TWO_FIVE_FIVE, null, null, null, FORMAT_INTEGER);

        List<String> effectList = config.getEffectList();
        if (effectList != null) {
            effectValue = new TextValue(effectList.toArray(new String[0]));
        } else {
            effectValue = null;
        }
        BigDecimal min = null, max = null;
        Integer minMireds = config.getMinMireds(), maxMireds = config.getMaxMireds();
        if (minMireds != null) {
            min = BigDecimal.valueOf(minMireds);
        }
        if (maxMireds != null) {
            max = BigDecimal.valueOf(maxMireds);
        }
        colorTempValue = new NumberValue(min, max, BigDecimal.ONE, Units.MIRED);

        buildChannels();
        finalizeChannels();
    }

    protected abstract void buildChannels();

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }
}

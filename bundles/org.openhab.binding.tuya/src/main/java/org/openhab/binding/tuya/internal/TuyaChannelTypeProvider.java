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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.COLOUR_CHANNEL_CODES;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.DIMMER_CHANNEL_CODES;
import static org.openhab.core.library.CoreItemFactory.COLOR;
import static org.openhab.core.library.CoreItemFactory.DIMMER;
import static org.openhab.core.library.CoreItemFactory.NUMBER;
import static org.openhab.core.library.CoreItemFactory.STRING;
import static org.openhab.core.library.CoreItemFactory.SWITCH;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.i18n.LocalizedKey;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TuyaChannelTypeProvider} generates necessary ChannelTypes.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { ChannelTypeProvider.class, TuyaChannelTypeProvider.class })
public class TuyaChannelTypeProvider implements ChannelTypeProvider {
    private static final String DEFAULT_CONTROL_CATEGORY = "settings";
    private static final String DEFAULT_STATUS_CATEGORY = "line";

    private static final Map<String, String> dimensionToCategory = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
                {
                    put("Angle", "incline");
                    // put("Area", "");
                    // put("DataAmount", "");
                    // put("DataTransferRate", "");
                    // put("Density", "");
                    // put("Dimensionless", "");
                    put("ElectricCurrent", "energy");
                    put("ElectricPotential", "energy");
                    put("Energy", "energy");
                    put("Illuminance", "sunrise");
                    put("Intensity", "sun"); // Most likely UV intensity?
                    // put("Length", "");
                    put("Power", "energy");
                    put("Pressure", "pressure");
                    // put("Speed", ""); // Wind or fan speed?
                    put("Temperature", "temperature");
                    put("Time", "time");
                    put("VolumetricFlowRate", "flow");
                }
            });

    private static final Map<String, String> dimensionToSemanticProperty = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
                {
                    put("Angle", "Tilt");
                    // put("Area", "");
                    // put("DataAmount", "");
                    // put("DataTransferRate", "");
                    // put("Density", "");
                    // put("Dimensionless", "");
                    // put("ElectricCurrent", "");
                    put("ElectricPotential", "Voltage");
                    // put("Energy", "");
                    put("Illuminance", "Illuminance");
                    // put("Intensity", "");
                    // put("Length", "");
                    put("Power", "Energy");
                    put("Pressure", "Pressure");
                    // put("Speed", "");
                    put("Temperature", "Temperature");
                    // put("Time", "");
                    // put("VolumetricFlowRate", "");
                }
            });

    private final Logger logger = LoggerFactory.getLogger(TuyaChannelTypeProvider.class);

    private final Bundle bundle;
    private final ChannelTypeI18nLocalizationService localizationService;

    private final Map<LocalizedKey, ChannelType> channelTypes = new ConcurrentHashMap<>();

    @Activate
    public TuyaChannelTypeProvider(@Reference ChannelTypeI18nLocalizationService localizationService) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.localizationService = localizationService;
    }

    private LocalizedKey getLocalizedKey(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return new LocalizedKey(channelTypeUID, locale != null ? locale.toLanguageTag() : null);
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        // N.B. This only returns the channel types already generated.
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (!BINDING_ID.equals(channelTypeUID.getBindingId())) {
            return null;
        }

        LocalizedKey localizedKey = getLocalizedKey(channelTypeUID, locale);
        ChannelType channelType = channelTypes.get(localizedKey);

        if (channelType != null) {
            return channelType;
        }

        String channelTypeId = channelTypeUID.getId();

        int i = channelTypeId.indexOf("_");
        if (i <= 0 || i >= channelTypeId.length()) {
            // Old format channel types are not our concern.
            return null;
        }

        String productId = channelTypeId.substring(0, i);
        channelTypeId = channelTypeId.substring(i + 1);

        // Build with a channelTypeId of just the lower-cased DP identifier and set defaults for all text.
        channelType = channelTypeFromSchema(channelTypeUID, productId, channelTypeId.toLowerCase());
        if (channelType != null) {
            // Localize that (e.g. using channel-type.tuya.cur_voltage.label = ...)
            channelType = localizationService.createLocalizedChannelType(bundle, channelType, locale);

            // Now use a ChannelTypeBuilder to clone channelType using the requested case
            // for the ID and localize it again.
            channelType = localizationService.createLocalizedChannelType(bundle,
                    clone(new ChannelTypeUID(BINDING_ID, channelTypeId), channelType), locale);

            // Now use a ChannelTypeBuilder to clone channelType using the requested
            // ChannelTypeUID of <product ID>_<DP identifier> and localize it (e.g. possibly
            // using channel-type.tuya.6fa7odsufen374x2_cur_voltage.label = "..." if such
            // a translation exists.
            channelType = localizationService.createLocalizedChannelType(bundle, clone(channelTypeUID, channelType),
                    locale);

            channelTypes.putIfAbsent(localizedKey, channelType);
            channelType = channelTypes.get(localizedKey);
        }

        return channelType;
    }

    private ChannelType clone(ChannelTypeUID channelTypeUID, ChannelType orig) {
        String itemType = orig.getItemType();
        if (itemType == null) { // It isn't. We created orig ourselves!
            itemType = STRING;
        }

        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, orig.getLabel(), itemType)
                .isAdvanced(orig.isAdvanced()) //
                .withAutoUpdatePolicy(orig.getAutoUpdatePolicy()) //
                .withCommandDescription(orig.getCommandDescription()) //
                .withTags(orig.getTags()) //
                .withUnitHint(orig.getUnitHint());

        StateDescription stateDescription = orig.getState();
        if (stateDescription != null) {
            builder.withStateDescriptionFragment(StateDescriptionFragmentBuilder.create(stateDescription).build());
        }

        String category = orig.getCategory();
        if (category != null) {
            builder.withCategory(category);
        }

        URI uri = orig.getConfigDescriptionURI();
        if (uri != null) {
            builder.withConfigDescriptionURI(uri);
        }

        String description = orig.getDescription();
        if (description != null) {
            builder.withDescription(description);
        }

        return builder.build();
    }

    private @Nullable ChannelType channelTypeFromSchema(ChannelTypeUID channelTypeUID, String productId,
            String channelTypeId) {
        SchemaDp schemaDp = TuyaSchemaDB.get(productId, channelTypeId);

        if (schemaDp == null) {
            logger.warn("No schema for product {} channel type {}", productId, channelTypeId);
            return null;
        }

        String label = schemaDp.label;
        if (label.isBlank()) {
            label = channelTypeId.replaceAll("_", " ");

            String l = StringUtils.capitalizeByWhitespace(label);
            if (l != null) {
                l = l.trim();
                if (!l.isBlank()) {
                    label = l;
                }
            }
        }

        String acceptedItemType = STRING;
        String category = "";
        String configurationRef = null;
        Collection<String> tags = new ArrayList<>(2);
        StateDescriptionFragmentBuilder stateDescriptionFragmentBuilder = null;
        boolean advanced = false;

        if (DIMMER_CHANNEL_CODES.contains(channelTypeId)) {
            acceptedItemType = DIMMER;
            category = "slider";
            configurationRef = "channel-type:tuya:dimmer";
            tags.add(schemaDp.readOnly ? "Point" : "Control");
            tags.add("Brightness");
        } else if ("bool".equals(schemaDp.type)) {
            acceptedItemType = SWITCH;
            category = "switch";
            configurationRef = "channel-type:tuya:switch";
            tags.add(schemaDp.readOnly ? "Status" : "Switch");
        } else if ("enum".equals(schemaDp.type)) {
            acceptedItemType = STRING;
            configurationRef = "channel-type:tuya:string";
            tags.add(schemaDp.readOnly ? "Status" : "Control");

            List<String> options = schemaDp.range;
            if (options != null) {
                stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create()
                        .withOptions(options.stream().map(
                                s -> new StateOption(s, StringUtils.capitalizeByWhitespace(s.replaceAll("_", " "))))
                                .toList());
            }
        } else if ("string".equals(schemaDp.type)) {
            if (COLOUR_CHANNEL_CODES.contains(channelTypeId)) {
                acceptedItemType = COLOR;
                category = "colorlight";
                configurationRef = "channel-type:tuya:color";
                tags.add(schemaDp.readOnly ? "Point" : "Control");
                tags.add("Color");
            } else {
                acceptedItemType = STRING;
                configurationRef = "channel-type:tuya:string";
                tags.add(schemaDp.readOnly ? "Status" : "Control");
            }
        } else if ("value".equals(schemaDp.type)) {
            acceptedItemType = NUMBER;
            category = "";
            configurationRef = "channel-type:tuya:number";

            if (!schemaDp.unit.isEmpty()) {
                Unit<?> unit = schemaDp.parsedUnit;
                if (unit == null) {
                    unit = UnitUtils.parseUnit(schemaDp.unit);
                    schemaDp.parsedUnit = unit;
                }

                if (unit != null) {
                    String dimension = UnitUtils.getDimensionName(unit);
                    if (dimension != null) {
                        acceptedItemType = NUMBER + ":" + dimension;
                        category = dimensionToCategory.getOrDefault(dimension,
                                (schemaDp.readOnly ? DEFAULT_STATUS_CATEGORY : DEFAULT_CONTROL_CATEGORY));
                        tags.add(schemaDp.readOnly ? "Measurement"
                                : ("Time".equals(dimension) ? "Control" : "Setpoint"));
                        String tag = dimensionToSemanticProperty.get(dimension);
                        if (tag != null) {
                            tags.add(tag);
                        }
                    } else {
                        logger.warn("Channel {} has unit \"{}\" but openHAB doesn't know the dimension", channelTypeId,
                                schemaDp.unit);

                        tags.add(schemaDp.readOnly ? "Point" : "Setpoint");
                    }
                }
            } else {
                tags.add(schemaDp.readOnly ? "Point" : "Setpoint");
            }

            stateDescriptionFragmentBuilder = StateDescriptionFragmentBuilder.create() //
                    .withReadOnly(schemaDp.readOnly) //
                    .withStep(schemaDp.step) //
                    .withPattern("%." + schemaDp.scale + "f " + ("%".equals(schemaDp.unit) ? "%%" : "%unit%"));

            Double min = schemaDp.min;
            if (min != null) {
                stateDescriptionFragmentBuilder.withMinimum(new BigDecimal(min));
            }

            Double max = schemaDp.max;
            if (max != null) {
                stateDescriptionFragmentBuilder.withMaximum(new BigDecimal(max));
            }
        } else {
            logger.warn("Don't know how to build a channel type for schema entry {} type {} - using string",
                    channelTypeId, schemaDp.type);

            acceptedItemType = STRING;
            configurationRef = "channel-type:tuya:string";
            tags.add(schemaDp.readOnly ? "Status" : "Control");
            advanced = true;
        }

        StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder //
                .state(new ChannelTypeUID(BINDING_ID, channelTypeId.toLowerCase()), label, acceptedItemType) //
                .withCategory(category) //
                .withTags(tags) //
                .isAdvanced(advanced);

        try {
            channelTypeBuilder.withConfigDescriptionURI(new URI(configurationRef));
        } catch (URISyntaxException e) {
        }

        if (!schemaDp.unit.isEmpty()) {
            channelTypeBuilder.withUnitHint(schemaDp.unit);
        }

        if (stateDescriptionFragmentBuilder != null) {
            channelTypeBuilder.withStateDescriptionFragment(stateDescriptionFragmentBuilder.build());
        }

        return channelTypeBuilder.build();
    }
}

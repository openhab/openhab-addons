/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType.RefreshPolicy;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@Component(service = ThingTypeProvider.class)
@NonNullByDefault
public class NetatmoDeviceThingTypeProvider extends BaseDsI18n implements ThingTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(NetatmoDeviceThingTypeProvider.class);

    @Activate
    public NetatmoDeviceThingTypeProvider(@Reference TranslationProvider translationProvider) {
        super(translationProvider);
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        List<ThingType> thingTypes = new LinkedList<>();
        for (ModuleType supportedThingType : ModuleType.values()) {
            ThingType thingType = getThingType(supportedThingType.thingTypeUID, locale);
            if (thingType != null) {
                thingTypes.add(thingType);
            }
        }
        return thingTypes;
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        ModuleType supportedThingType = ModuleType.valueOf(thingTypeUID.getId());

        ThingTypeBuilder thingTypeBuilder = ThingTypeBuilder
                .instance(thingTypeUID, getLabelText(thingTypeUID.getId(), locale))
                .withDescription(getDescText(thingTypeUID.getId(), locale))
                .withProperties(getProperties(supportedThingType)).withRepresentationProperty(EQUIPMENT_ID)
                .withChannelGroupDefinitions(getGroupDefinitions(supportedThingType));

        try {
            URI configURI;
            if (supportedThingType.getSignalLevels() == NetatmoConstants.NO_RADIO) {
                configURI = new URI("netatmo:virtual");
            } else {
                if (supportedThingType.refreshPeriod == RefreshPolicy.CONFIG) {
                    configURI = new URI("netatmo:configurable");
                } else {
                    configURI = new URI("netatmo:device");
                }
            }
            thingTypeBuilder.withConfigDescriptionURI(configURI);
        } catch (URISyntaxException e) {
            logger.warn("Unable to build configuration description URI : {}", e);
        }

        if (supportedThingType.extensions != null) {
            thingTypeBuilder.withExtensibleChannelTypeIds(supportedThingType.extensions);
        }
        if (supportedThingType.bridgeThingType != null) {
            thingTypeBuilder
                    .withSupportedBridgeTypeUIDs(Arrays.asList(supportedThingType.bridgeThingType.getAsString()));
        }

        return thingTypeBuilder.buildBridge();
    }

    private List<ChannelGroupDefinition> getGroupDefinitions(ModuleType supportedThingType) {
        List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
        for (String group : supportedThingType.groups) {
            ChannelGroupTypeUID groupType = new ChannelGroupTypeUID(BINDING_ID, group);
            groupDefinitions.add(new ChannelGroupDefinition(group, groupType));
        }
        return groupDefinitions;
    }

    private Map<String, String> getProperties(ModuleType supportedThingType) {
        Map<String, String> properties = new HashMap<>();

        if (supportedThingType.getSignalLevels() != NetatmoConstants.NO_RADIO) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_MODEL_ID, supportedThingType.name());
        }

        return properties;
    }
}

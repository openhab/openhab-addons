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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.config.NAThingConfiguration;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the ThingTypeProvider to generated Thing types based on {@link ModuleType} enum.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@Component(service = ThingTypeProvider.class)
@NonNullByDefault
public class NetatmoThingTypeProvider implements ThingTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(NetatmoThingTypeProvider.class);
    private final ThingTypeI18nLocalizationService localizationService;
    private final Bundle bundle;

    @Activate
    public NetatmoThingTypeProvider(final @Reference ThingTypeI18nLocalizationService localizationService,
            ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
        this.localizationService = localizationService;
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        return ModuleType.AS_SET.stream().filter(mt -> mt != ModuleType.UNKNOWN)
                .map(mt -> Optional.ofNullable(getThingType(mt.thingTypeUID, locale))).map(Optional::get).toList();
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        if (BINDING_ID.equalsIgnoreCase(thingTypeUID.getBindingId())) {
            try {
                ModuleType moduleType = ModuleType.from(thingTypeUID);

                ThingTypeBuilder thingTypeBuilder = ThingTypeBuilder.instance(thingTypeUID, thingTypeUID.toString())
                        .withRepresentationProperty(NAThingConfiguration.ID)
                        .withExtensibleChannelTypeIds(moduleType.getExtensions())
                        .withChannelGroupDefinitions(getGroupDefinitions(moduleType))
                        .withConfigDescriptionURI(moduleType.getConfigDescription());

                ThingTypeUID bridgeType = moduleType.getBridge().thingTypeUID;
                if (!ModuleType.UNKNOWN.thingTypeUID.equals(bridgeType)) {
                    thingTypeBuilder.withSupportedBridgeTypeUIDs(List.of(bridgeType.getAsString()));
                }

                return localizationService.createLocalizedThingType(bundle,
                        moduleType.isABridge() ? thingTypeBuilder.buildBridge() : thingTypeBuilder.build(), locale);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to define ModuleType for thingType {} : {}", thingTypeUID.getId(), e.getMessage());
            }
        }
        return null;
    }

    private List<ChannelGroupDefinition> getGroupDefinitions(ModuleType thingType) {
        return thingType.getGroupTypes().stream().map(groupType -> new ChannelGroupDefinition(toGroupName(groupType),
                new ChannelGroupTypeUID(BINDING_ID, groupType))).toList();
    }

    public static String toGroupName(String groupeTypeName) {
        String result = groupeTypeName;
        for (String variation : GROUP_VARIATIONS) {
            result = result.replace(variation, "");
        }
        return result;
    }
}

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
package org.openhab.binding.digitalstrom.internal.providers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.handler.CircuitHandler;
import org.openhab.binding.digitalstrom.internal.handler.DeviceHandler;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DsDeviceThingTypeProvider} creates the {@link ThingType}'s for the subclasses of the
 * {@link GeneralDeviceInformations}. It also adds the {@link ThingTypeUID}'s to the related handlers. So only the
 * {@link SupportedThingTypes} enum has to be adjusted, if new device types of digitalSTROM should be supported.
 * Provided the new digitalSTROM devices uses the same mechanism like now.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
@Component(service = ThingTypeProvider.class)
public class DsDeviceThingTypeProvider extends BaseDsI18n implements ThingTypeProvider {

    /**
     * Through the {@link SupportedThingTypes} the {@link ThingType}'s will be created. For that the enum name will be
     * used as thing type id, the first field will set the responsible handler and the last enum field will set the
     * supporting of the power sensor refresh configurations (config-description with refresh priority setting or not).
     *
     * @author Michael Ochel - Initial contribution
     * @author Matthias Siegele - Initial contribution
     */
    public static enum SupportedThingTypes {
        // ThingType, responsible ThingHanlder, Device config-description with power-sensors
        GE(DeviceHandler.class.getSimpleName(), true),
        GR(DeviceHandler.class.getSimpleName(), false),
        SW(DeviceHandler.class.getSimpleName(), true),
        BL(DeviceHandler.class.getSimpleName(), true),
        dSiSens200(DeviceHandler.class.getSimpleName(), false),
        circuit(CircuitHandler.class.getSimpleName(), false);

        private final String handler;
        private final boolean havePowerSensors;

        private SupportedThingTypes(String handler, boolean havePowerSensors) {
            this.handler = handler;
            this.havePowerSensors = havePowerSensors;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(DsDeviceThingTypeProvider.class);

    private static final String DEVICE_WITH_POWER_SENSORS = "thing-type:digitalstrom:deviceWithPowerSensors";
    private static final String DEVICE_WITHOUT_POWER_SENSORS = "thing-type:digitalstrom:deviceWithoutPowerSensors";

    @Activate
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference
    @Override
    protected void setTranslationProvider(TranslationProvider translationProvider) {
        super.setTranslationProvider(translationProvider);
    }

    @Override
    protected void unsetTranslationProvider(TranslationProvider translationProvider) {
        super.unsetTranslationProvider(translationProvider);
    }

    @Override
    protected void init() {
        for (SupportedThingTypes supportedThingType : SupportedThingTypes.values()) {
            if (supportedThingType.handler.equals(DeviceHandler.class.getSimpleName())) {
                DeviceHandler.SUPPORTED_THING_TYPES
                        .add(new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()));
            }
            if (supportedThingType.handler.equals(CircuitHandler.class.getSimpleName())) {
                CircuitHandler.SUPPORTED_THING_TYPES
                        .add(new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()));
            }
        }
    }

    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        List<ThingType> thingTypes = new LinkedList<>();
        for (SupportedThingTypes supportedThingType : SupportedThingTypes.values()) {
            thingTypes.add(getThingType(
                    new ThingTypeUID(DigitalSTROMBindingConstants.BINDING_ID, supportedThingType.toString()), locale));
        }
        return thingTypes;
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        try {
            SupportedThingTypes supportedThingType = SupportedThingTypes.valueOf(thingTypeUID.getId());
            ThingTypeBuilder thingTypeBuilder = ThingTypeBuilder
                    .instance(thingTypeUID, getLabelText(thingTypeUID.getId(), locale))
                    .withSupportedBridgeTypeUIDs(
                            Arrays.asList(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE.getAsString()))
                    .withDescription(getDescText(thingTypeUID.getId(), locale));
            try {
                if (supportedThingType.havePowerSensors) {
                    thingTypeBuilder.withConfigDescriptionURI(new URI(DEVICE_WITH_POWER_SENSORS));
                } else {
                    thingTypeBuilder.withConfigDescriptionURI(new URI(DEVICE_WITHOUT_POWER_SENSORS));
                }
            } catch (URISyntaxException e) {
                logger.debug("An URISyntaxException occurred: ", e);
            }
            if (SupportedThingTypes.GR.equals(supportedThingType)) {
                thingTypeBuilder.withChannelDefinitions(Arrays.asList(new ChannelDefinitionBuilder(
                        DsChannelTypeProvider.SHADE,
                        new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, DsChannelTypeProvider.SHADE))
                                .build()));
            }
            if (SupportedThingTypes.circuit.equals(supportedThingType)) {
                List<ChannelDefinition> channelDefinitions = new ArrayList<>(3);
                for (MeteringTypeEnum meteringType : MeteringTypeEnum.values()) {
                    channelDefinitions.add(new ChannelDefinitionBuilder(
                            DsChannelTypeProvider.getMeteringChannelID(meteringType, MeteringUnitsEnum.WH, false),
                            new ChannelTypeUID(DigitalSTROMBindingConstants.BINDING_ID, DsChannelTypeProvider
                                    .getMeteringChannelID(meteringType, MeteringUnitsEnum.WH, false))).build());
                }
                thingTypeBuilder.withChannelDefinitions(channelDefinitions);
            }
            return thingTypeBuilder.build();
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return null;
    }
}

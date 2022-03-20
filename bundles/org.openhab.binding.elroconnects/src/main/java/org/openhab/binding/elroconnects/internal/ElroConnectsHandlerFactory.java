/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsCOAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsEntrySensorHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsHeatAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsMotionSensorHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsPowerSocketHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsSmokeAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsWaterAlarmHandler;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ElroConnectsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.elroconnects", service = ThingHandlerFactory.class)
public class ElroConnectsHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) NetworkAddressService networkAddressService;
    private @NonNullByDefault({}) ElroConnectsDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        switch (thing.getThingTypeUID().getId()) {
            case TYPE_CONNECTOR:
                return new ElroConnectsBridgeHandler((Bridge) thing, networkAddressService,
                        dynamicStateDescriptionProvider);
            case TYPE_SMOKEALARM:
                return new ElroConnectsSmokeAlarmHandler(thing);
            case TYPE_WATERALARM:
                return new ElroConnectsWaterAlarmHandler(thing);
            case TYPE_COALARM:
                return new ElroConnectsCOAlarmHandler(thing);
            case TYPE_HEATALARM:
                return new ElroConnectsHeatAlarmHandler(thing);
            case TYPE_ENTRYSENSOR:
                return new ElroConnectsEntrySensorHandler(thing);
            case TYPE_MOTIONSENSOR:
                return new ElroConnectsMotionSensorHandler(thing);
            case TYPE_POWERSOCKET:
                return new ElroConnectsPowerSocketHandler(thing);
            case TYPE_THSENSOR:
                return new ElroConnectsDeviceHandler(thing);
            default:
                return null;
        }
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(
            ElroConnectsDynamicStateDescriptionProvider dynamicStateDescriptionProver) {
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProver;
    }

    protected void unsetDynamicStateDescriptionProvider(
            ElroConnectsDynamicStateDescriptionProvider dynamicStateDescriptionProver) {
        this.dynamicStateDescriptionProvider = null;
    }
}

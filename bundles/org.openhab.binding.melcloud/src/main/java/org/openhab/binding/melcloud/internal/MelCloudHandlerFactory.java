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
package org.openhab.binding.melcloud.internal;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.handler.MelCloudAccountHandler;
import org.openhab.binding.melcloud.internal.handler.MelCloudDeviceHandler;
import org.openhab.binding.melcloud.internal.handler.MelCloudHeatpumpDeviceHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MelCloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Wietse van Buitenen - Added heatpump device
 */
@Component(configurationPid = "binding.melcloud", service = ThingHandlerFactory.class)
public class MelCloudHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MELCLOUD_ACCOUNT.equals(thingTypeUID)) {
            MelCloudAccountHandler handler = new MelCloudAccountHandler((Bridge) thing);
            return handler;
        } else if (THING_TYPE_ACDEVICE.equals(thingTypeUID)) {
            MelCloudDeviceHandler handler = new MelCloudDeviceHandler(thing);
            return handler;
        } else if (THING_TYPE_HEATPUMPDEVICE.equals(thingTypeUID)) {
            MelCloudHeatpumpDeviceHandler handler = new MelCloudHeatpumpDeviceHandler(thing);
            return handler;
        }

        return null;
    }
}

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
package org.openhab.binding.systeminfo.internal;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.systeminfo.internal.handler.SysteminfoHandler;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SysteminfoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papazov - Pass systeminfo service to the SysteminfoHandler constructor
 * @author Wouter Born - Add null annotations
 * @author Mark Herwege - Add dynamic creation of extra channels
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.systeminfo")
public class SysteminfoHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) SysteminfoInterface systeminfo;
    private @NonNullByDefault({}) SysteminfoThingTypeProvider thingTypeProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId())
                && thingTypeUID.getId().startsWith(THING_TYPE_COMPUTER_ID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (supportsThingType(thingTypeUID)) {
            String extString = "-" + thing.getUID().getId();
            ThingTypeUID extThingTypeUID = new ThingTypeUID(BINDING_ID, THING_TYPE_COMPUTER_ID + extString);
            if (thingTypeProvider.getThingType(extThingTypeUID, null) == null) {
                thingTypeProvider.createThingType(extThingTypeUID);
                thingTypeProvider.storeChannelsConfig(thing); // Save the current channels configs, will be restored
                                                              // after thing type change.
            }
            return new SysteminfoHandler(thing, thingTypeProvider, systeminfo);
        }
        return null;
    }

    @Reference
    public void bindSystemInfo(SysteminfoInterface systeminfo) {
        this.systeminfo = systeminfo;
    }

    public void unbindSystemInfo(SysteminfoInterface systeminfo) {
        this.systeminfo = null;
    }

    @Reference
    public void setSysteminfoThingTypeProvider(SysteminfoThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = thingTypeProvider;
    }

    public void unsetSysteminfoThingTypeProvider(SysteminfoThingTypeProvider thingTypeProvider) {
        this.thingTypeProvider = null;
    }
}

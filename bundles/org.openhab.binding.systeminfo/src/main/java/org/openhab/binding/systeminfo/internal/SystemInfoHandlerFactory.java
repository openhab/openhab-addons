/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.systeminfo.internal.SystemInfoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.systeminfo.internal.handler.ProcessInfoHandler;
import org.openhab.binding.systeminfo.internal.handler.SystemInfoHandler;
import org.openhab.binding.systeminfo.internal.model.SystemInfoInterface;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SystemInfoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papazov - Pass systeminfo service to the SystemInfoHandler constructor
 * @author Wouter Born - Add null annotations
 * @author Alexander Falkenstern - Process information
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.systeminfo")
public class SystemInfoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_COMPUTER,
            THING_TYPE_PROCESS);

    private @NonNullByDefault({}) SystemInfoInterface systeminfo;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingHandler handler = null;
        if (BRIDGE_TYPE_COMPUTER.equals(thing.getThingTypeUID()) && (thing instanceof Bridge)) {
            handler = new SystemInfoHandler((Bridge) thing, systeminfo);
        } else if (THING_TYPE_PROCESS.equals(thing.getThingTypeUID())) {
            handler = new ProcessInfoHandler(thing, systeminfo);
        }
        return handler;
    }

    @Reference
    public void bindSystemInfo(SystemInfoInterface systeminfo) {
        this.systeminfo = systeminfo;
    }

    public void unbindSystemInfo(SystemInfoInterface systeminfo) {
        this.systeminfo = null;
    }
}

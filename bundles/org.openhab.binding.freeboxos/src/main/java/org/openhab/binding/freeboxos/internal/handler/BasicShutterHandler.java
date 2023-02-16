/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.List;
import java.util.Set;
<<<<<<< Upstream, based on origin/main

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link BasicShutterHandler} is responsible for handling everything associated to
 * any Freebox Home basic_shutter thing type.
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class BasicShutterHandler extends HomeNodeHandler {
    private final static Set<String> SHUTTER_ENDPOINTS = Set.of(SHUTTER_STOP, BASIC_SHUTTER_UP, BASIC_SHUTTER_DOWN);

    public BasicShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void internalConfigureChannel(String channelId, Configuration conf, List<Endpoint> endpoints) {
        endpoints.stream().filter(ep -> channelId.equals(BASIC_SHUTTER_STATE) && SHUTTER_ENDPOINTS.contains(ep.name()))
                .forEach(endPoint -> conf.put(endPoint.name(), endPoint.id()));
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        return value != null && channelId.equals(BASIC_SHUTTER_STATE)
                ? state.asBoolean() ? OpenClosedType.CLOSED : OpenClosedType.OPEN
                : UnDefType.NULL;
    }

    @Override
    protected boolean executeChannelCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config) throws FreeboxException {
        Integer slot = getSlotId(config, command.toString().toLowerCase());
        if (BASIC_SHUTTER_STATE.equals(channelId) && slot != null) {
            return homeManager.putCommand(getClientId(), slot, true);
        }
        return false;
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;
=======
>>>>>>> 9aef877 Rebooting Home Node part

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link BasicShutterHandler} is responsible for handling everything associated to
 * any Freebox Home basic_shutter thing type.
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class BasicShutterHandler extends HomeNodeHandler {
    private final static Set<String> SHUTTER_ENDPOINTS = Set.of(SHUTTER_STOP, BASIC_SHUTTER_UP, BASIC_SHUTTER_DOWN);

    public BasicShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void internalConfigureChannel(String channelId, Configuration conf, List<Endpoint> endpoints) {
        endpoints.stream().filter(ep -> channelId.equals(BASIC_SHUTTER_STATE) && SHUTTER_ENDPOINTS.contains(ep.name()))
                .forEach(endPoint -> conf.put(endPoint.name(), endPoint.id()));
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        return value != null && channelId.equals(BASIC_SHUTTER_STATE)
                ? state.asBoolean() ? OpenClosedType.CLOSED : OpenClosedType.OPEN
                : UnDefType.NULL;
    }

    @Override
    protected boolean executeChannelCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config) throws FreeboxException {
        Integer slot = getSlotId(config, command.toString().toLowerCase());
        if (BASIC_SHUTTER_STATE.equals(channelId) && slot != null) {
            return homeManager.putCommand(getClientId(), slot, true);
        }
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
        return super.internalHandleCommand(channelUID, command);
>>>>>>> e4c7780 Implementing SHUTTER Home Node
=======
        return super.internalHandleCommand(channelId, command);
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
=======
        return false;
>>>>>>> 9aef877 Rebooting Home Node part
    }
}

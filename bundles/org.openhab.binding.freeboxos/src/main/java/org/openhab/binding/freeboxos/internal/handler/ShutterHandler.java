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

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ShutterHandler} is responsible for handling everything associated to any Freebox Home shutter.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ShutterHandler extends HomeNodeHandler {

    public ShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void internalConfigureChannel(String channelId, Configuration conf, List<Endpoint> endpoints) {
        endpoints.stream().filter(ep -> channelId.equals(SHUTTER_POSITION) && ep.name().equals(SHUTTER_STOP))
                .forEach(endPoint -> conf.put(endPoint.name(), endPoint.id()));
    }

    @Override
    protected State getChannelState(HomeManager homeManager, String channelId, EndpointState state) {
        String value = state.value();
        return value != null && channelId.equals(SHUTTER_POSITION) ? new QuantityType<Dimensionless>(value + " %")
                : UnDefType.NULL;
    }

    @Override
    protected boolean executeSlotCommand(HomeManager homeManager, String channelId, Command command,
            Configuration config, int positionSlot) throws FreeboxException {
        Integer stopSlot = getSlotId(config, SHUTTER_STOP);
        if (SHUTTER_POSITION.equals(channelId) && stopSlot instanceof Integer) {
            if (command instanceof UpDownType upDownCmd) {
                return operateShutter(homeManager, stopSlot, positionSlot, upDownCmd == UpDownType.DOWN ? 100 : 0);
            } else if (command instanceof StopMoveType stopMove && stopMove == StopMoveType.STOP) {
                return operateShutter(homeManager, stopSlot, positionSlot, -1);
            } else if (command instanceof Number numberCmd) {
                return operateShutter(homeManager, stopSlot, positionSlot, numberCmd.intValue());
            }
        }
        return false;
    }

    private boolean operateShutter(HomeManager homeManager, int stopSlot, int positionSlot, int target)
            throws FreeboxException {
        homeManager.putCommand(getClientId(), stopSlot, true);
        if (target >= 0) {
            homeManager.putCommand(getClientId(), positionSlot, target);
        }
        return true;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState.ValueType;
import org.openhab.binding.freeboxos.internal.config.ShutterConfiguration;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link ShutterHandler} is responsible for handling everything associated to any Freebox Home shutter thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ShutterHandler extends BasicShutterHandler {

    public ShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        ShutterConfiguration config = getConfiguration();
        EndpointState state = getManager(HomeManager.class).getEndpointsState(config.id, config.stateSignalId);
        EndpointState position = getManager(HomeManager.class).getEndpointsState(config.id, config.positionSignalId);
        Double percent = null;
        if (state != null && position != null) {
            if (ValueType.BOOL.equals(position.valueType())) {
                percent = Boolean.TRUE.equals(position.asBoolean()) ? 1.0 : 0.0;
            } else if (ValueType.INT.equals(position.valueType())) {
                Integer inValue = position.asInt();
                if (inValue != null) {
                    percent = inValue.doubleValue() / 100.0;
                }
            }
        }
        updateChannelDecimal(BASIC_SHUTTER, BASIC_SHUTTER_CMD, percent);
    }

    private ShutterConfiguration getConfiguration() {
        return getConfigAs(ShutterConfiguration.class);
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (BASIC_SHUTTER_CMD.equals(channelId)) {
            ShutterConfiguration config = getConfiguration();
            if (StopMoveType.STOP.equals(command)) {
                getManager(HomeManager.class).putCommand(config.id, config.stopSlotId, true);
                return true;
            }
        }
<<<<<<< Upstream, based on origin/main
        return super.internalHandleCommand(channelUID, command);
>>>>>>> e4c7780 Implementing SHUTTER Home Node
=======
        return super.internalHandleCommand(channelId, command);
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
    }
}

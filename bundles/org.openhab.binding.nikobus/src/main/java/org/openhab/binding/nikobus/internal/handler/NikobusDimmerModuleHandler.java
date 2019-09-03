/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.handler;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.binding.nikobus.internal.utils.Utils;

/**
 * The {@link NikobusDimmerModuleHandler} is responsible for communication between Nikobus dim-controller and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class NikobusDimmerModuleHandler extends NikobusSwitchModuleHandler {
    private @Nullable Future<?> requestUpdateFuture;

    public NikobusDimmerModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        Utils.cancel(requestUpdateFuture);
        requestUpdateFuture = null;

        super.dispose();
    }

    @Override
    public void requestStatus(SwitchModuleGroup group) {
        Utils.cancel(requestUpdateFuture);
        super.requestStatus(group);
        requestUpdateFuture = scheduler.schedule(() -> {
            super.requestStatus(group);
        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected int valueFromCommand(Command command) {
        if (command instanceof PercentType) {
            return (int) (((PercentType) command).doubleValue() / 100.0 * 255.0 + 0.5);
        }

        return super.valueFromCommand(command);
    }

    @Override
    protected State stateFromValue(int value) {
        int result = (int) ((value * 100.0 / 255.0) + 0.5);
        return new PercentType(result);
    }
}

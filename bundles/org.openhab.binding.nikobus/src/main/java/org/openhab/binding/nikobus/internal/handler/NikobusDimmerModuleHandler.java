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
package org.openhab.binding.nikobus.internal.handler;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link NikobusDimmerModuleHandler} is responsible for communication between Nikobus dim-controller and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
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
        requestUpdateFuture = scheduler.schedule(() -> super.requestStatus(group), 1, TimeUnit.SECONDS);
    }

    @Override
    protected int valueFromCommand(String channelId, Command command) {
        if (command instanceof PercentType) {
            return Math.round(((PercentType) command).floatValue() / 100f * 255f);
        }

        return super.valueFromCommand(channelId, command);
    }

    @Override
    protected State stateFromValue(String channelId, int value) {
        int result = Math.round(value * 100f / 255f);
        return new PercentType(result);
    }
}

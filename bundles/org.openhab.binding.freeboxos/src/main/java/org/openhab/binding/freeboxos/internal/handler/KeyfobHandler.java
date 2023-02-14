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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link KeyfobHandler} is responsible for handling everything associated to
 * any Freebox Home keyfob thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class KeyfobHandler extends AlarmHandler {

    public KeyfobHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        return super.internalHandleCommand(channelId, command);
    }
}

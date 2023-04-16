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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractNoResponseCommand extends IotDeviceCommand<AbstractNoResponseCommand.Nothing> {
    public static class Nothing {
        private Nothing() {
        }

        private static final Nothing INSTANCE = new Nothing();
    }

    protected AbstractNoResponseCommand() {
        super();
    }

    @Override
    public Nothing convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson) {
        return Nothing.INSTANCE;
    }
}

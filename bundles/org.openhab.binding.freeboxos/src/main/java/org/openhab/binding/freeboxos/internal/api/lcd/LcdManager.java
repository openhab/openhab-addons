/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.lcd;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;

/**
 * The {@link LcdManager} is the Java class used to handle api requests
 * related to lcd screen of the server
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LcdManager extends RestManager {

    public LcdManager(ApiHandler apiHandler) {
        super(apiHandler, "lcd/config");
    }

    public LcdConfig getConfig() throws FreeboxException {
        return get(null, LcdConfigResponse.class, true);
    }

    public LcdConfig setConfig(LcdConfig config) throws FreeboxException {
        return put(null, config, LcdConfigResponse.class);
    }

    // Response classes and validity evaluations
    private static class LcdConfigResponse extends Response<LcdConfig> {
    }
}

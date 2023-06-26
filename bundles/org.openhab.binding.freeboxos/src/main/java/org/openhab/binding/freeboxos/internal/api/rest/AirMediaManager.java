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
package org.openhab.binding.freeboxos.internal.api.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link AirMediaManager} is the Java class used to handle api requests related to air media global configuration
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaManager extends ConfigurableRest<AirMediaManager.Config, AirMediaManager.ConfigResponse> {
    private static final String PATH = "airmedia";

    protected static record Config(boolean enabled) {
    }

    protected static class ConfigResponse extends Response<Config> {
    }

    public AirMediaManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(PATH),
                CONFIG_PATH);
        session.addManager(MediaReceiverManager.class, new MediaReceiverManager(session, getUriBuilder()));
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().enabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        return setConfig(new Config(enabled)).enabled();
    }
}

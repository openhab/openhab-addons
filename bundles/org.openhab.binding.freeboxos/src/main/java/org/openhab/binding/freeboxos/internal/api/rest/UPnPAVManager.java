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
<<<<<<< Upstream, based on origin/main

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends ConfigurableRest<UPnPAVManager.Config, UPnPAVManager.ConfigResponse> {
    private static final String PATH = "upnpav";

    protected static class ConfigResponse extends Response<Config> {
    }

    protected static record Config(boolean enabled) {
    }

    public UPnPAVManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(PATH),
                CONFIG_PATH);
=======
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends ConfigurableRest<UPnPAVManager.Config, UPnPAVManager.ConfigResponse> {
    public static final String PATH = "upnpav";

    public static class ConfigResponse extends Response<Config> {
    }

    protected static record Config(boolean enabled) {
    }

    public UPnPAVManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(PATH), CONFIG_PATH);
>>>>>>> e4ef5cc Switching to Java 17 records
    }

    public boolean getStatus() throws FreeboxException {
        return getConfig().enabled();
    }

    public boolean setStatus(boolean enabled) throws FreeboxException {
        return setConfig(new Config(enabled)).enabled();
    }
}

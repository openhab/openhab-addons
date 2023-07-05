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

import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link LcdManager} is the Java class used to handle api requests related to lcd screen of the server
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LcdManager extends ConfigurableRest<LcdManager.Config, LcdManager.ConfigResponse> {
    private static final String PATH = "lcd";

    protected static class ConfigResponse extends Response<Config> {
    }

    public static record Config(int brightness, int orientation, boolean orientationForced) {
    }

    public LcdManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigResponse.class, session.getUriBuilder().path(PATH),
                CONFIG_PATH);
    }

    private void setBrightness(int brightness) throws FreeboxException {
        Config oldConfig = getConfig();
        setConfig(new Config(brightness, oldConfig.orientation, oldConfig.orientationForced));
    }

    public void setOrientation(int orientation) throws FreeboxException {
        Config oldConfig = getConfig();
        setConfig(new Config(oldConfig.brightness, orientation, oldConfig.orientationForced));
    }

    public void setOrientationForced(boolean forced) throws FreeboxException {
        Config oldConfig = getConfig();
        setConfig(new Config(oldConfig.brightness, oldConfig.orientation, forced));
    }

    public void setBrightness(Callable<Integer> function) throws FreeboxException {
        try {
            setBrightness(function.call());
        } catch (Exception e) {
            throw new FreeboxException(e, "Error setting brightness");
        }
    }
}

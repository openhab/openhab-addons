/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.api.lcd.LcdConfig.LcdConfigResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link LcdManager} is the Java class used to handle api requests
 * related to lcd screen of the server
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LcdManager extends ConfigurableRest<LcdConfig, LcdConfigResponse> {
    private static final String LCD_SUB_PATH = "lcd";

    public LcdManager(FreeboxOsSession session) {
        super(session, LcdConfigResponse.class, LCD_SUB_PATH, CONFIG_SUB_PATH);
    }
}

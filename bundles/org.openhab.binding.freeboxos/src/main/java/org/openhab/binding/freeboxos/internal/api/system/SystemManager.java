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
package org.openhab.binding.freeboxos.internal.api.system;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.ConfigurableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.system.SystemConf.SystemConfigurationResponse;

/**
 * The {@link SystemManager} is the Java class used to handle api requests
 * related to system
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemManager extends ConfigurableRest<SystemConf, SystemConfigurationResponse> {
    public SystemManager(FreeboxOsSession session) {
        super(session, SystemConfigurationResponse.class, SYSTEM_SUB_PATH, null);
    }

    public void reboot() throws FreeboxException {
        post(REBOOT_SUB_PATH);
    }
}

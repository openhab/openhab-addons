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
package org.openhab.io.imperihome.internal.handler;

import javax.servlet.http.HttpServletRequest;

import org.openhab.io.imperihome.internal.ImperiHomeConfig;
import org.openhab.io.imperihome.internal.model.System;

/**
 * System data request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SystemHandler {

    private ImperiHomeConfig config;

    public SystemHandler(ImperiHomeConfig imperiHomeConfig) {
        config = imperiHomeConfig;
    }

    public System handle(HttpServletRequest req) {
        System system = new System();
        system.setId(config.getSystemId());
        system.setApiVersion(1);
        return system;
    }

}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

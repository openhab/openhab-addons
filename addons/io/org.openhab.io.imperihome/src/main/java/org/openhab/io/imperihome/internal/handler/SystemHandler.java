/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.handler;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.id.InstanceUUID;
import org.openhab.io.imperihome.internal.model.System;

/**
 * System data request handler.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SystemHandler {

    public System handle(HttpServletRequest req) {
        System system = new System();
        system.setId(InstanceUUID.get());
        system.setApiVersion(1);
        return system;
    }

}

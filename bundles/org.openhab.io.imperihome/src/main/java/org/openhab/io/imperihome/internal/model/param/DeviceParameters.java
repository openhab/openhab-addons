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
package org.openhab.io.imperihome.internal.model.param;

import java.util.HashMap;

/**
 * No-op extension of HashMap storing device parameters. This class exists because it allows the use of a Map in Device
 * and at the same
 * time makes it possible to expose the values as a JSON array using a custom serializer.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceParameters extends HashMap<ParamType, DeviceParam> {

    private static final long serialVersionUID = -3877582034887195137L;

    public void set(DeviceParam param) {
        put(param.getKey(), param);
    }
}

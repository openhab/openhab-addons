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
package org.openhab.binding.foobot.internal.json;

/**
 * The {@link FoobotDevice} is the Java class used to map the JSON response to the foobot.io request.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Code refactor
 */
public class FoobotDevice {

    private String uuid;
    private String mac;
    private String name;

    public String getUuid() {
        return uuid;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }
}

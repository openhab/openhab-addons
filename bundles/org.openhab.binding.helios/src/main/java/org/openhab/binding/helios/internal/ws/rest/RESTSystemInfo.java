/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.helios.internal.ws.rest;

/**
 * Helper class for encapsulating REST objects
 *
 * @author Karel Goderis - Initial contribution
 */
public class RESTSystemInfo {

    public String variant;
    public String serialNumber;
    public String hwVersion;
    public String swVersion;
    public String buildType;
    public String deviceName;

    RESTSystemInfo() {
    }
}

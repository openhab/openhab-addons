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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

/**
 * Software Update State is part of PublicInformation.
 * 
 * @author Gerd Zanker - Initial contribution
 */
public class SoftwareUpdateState {

    public String swUpdateState;
    public String swInstalledVersion;
    public String swUpdateAvailableVersion;

    public static boolean isValid(SoftwareUpdateState obj) {
        return obj != null && obj.swUpdateState != null && obj.swInstalledVersion != null
                && obj.swUpdateAvailableVersion != null;
    }
}

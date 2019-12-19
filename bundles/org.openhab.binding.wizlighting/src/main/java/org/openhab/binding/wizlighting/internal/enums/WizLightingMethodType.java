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
package org.openhab.binding.wizlighting.internal.enums;

/**
 * This enum represents the available Wiz Lighting Request Methods
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public enum WizLightingMethodType {
    /** Registration */
    registration("registration"),
    /** Pulse */
    pulse("pulse"),
    /** setPilot */
    setPilot("setPilot"),
    /** setSystemConfig */
    setSystemConfig("setSystemConfig");

    private String method;

    private WizLightingMethodType(final String method) {
        this.method = method;
    }

    /**
     * Gets the method name for request method
     *
     * @return the method name
     */
    public String getMethod() {
        return method;
    }
}

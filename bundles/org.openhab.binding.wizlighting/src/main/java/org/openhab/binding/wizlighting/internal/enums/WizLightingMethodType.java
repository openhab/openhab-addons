/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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
package org.openhab.binding.russound.internal.rio.controller;

/**
 * Configuration class for the {@link RioControllerHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioControllerConfig {
    /**
     * Constant defined for the "controller" configuration field
     */
    public static final String CONTROLLER = "controller";

    /**
     * ID of the controller
     */
    private int controller;

    /**
     * Gets the controller identifier
     *
     * @return the controller identifier
     */
    public int getController() {
        return controller;
    }

    /**
     * Sets the controller identifier
     *
     * @param controller the controller identifier
     */
    public void setController(int controller) {
        this.controller = controller;
    }
}

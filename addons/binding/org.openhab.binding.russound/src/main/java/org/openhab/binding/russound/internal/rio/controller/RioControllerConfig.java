/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

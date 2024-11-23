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
package org.openhab.binding.russound.internal.rio.source;

/**
 * Configuration class for the {@link RioSourceHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioSourceConfig {
    /**
     * Constant defined for the "source" configuration field
     */
    public static final String SOURCE = "source";

    /**
     * ID of the source
     */
    private int source;

    /**
     * Gets the source identifier
     *
     * @return the source identifier
     */
    public int getSource() {
        return source;
    }

    /**
     * Sets the source identifier
     *
     * @param source the source identifier
     */
    public void setSource(int source) {
        this.source = source;
    }
}

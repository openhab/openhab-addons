/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.config;

/**
 * Configuration settings for a {@link org.openhab.binding.minecraft.internal.handler.MinecraftSignHandler}.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class SignConfig {
    private String signName = "";

    /**
     * Get text of sign to monitor.
     *
     * @return sign text
     */
    public String getName() {
        return signName;
    }

    /**
     * Set the sign text to listen for.
     *
     * @param sign text.
     */
    public void setName(String name) {
        this.signName = name;
    }
}

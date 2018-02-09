/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.config;

/**
 * Configuration settings for a {@link org.openhab.binding.minecraft.handler.MinecraftSignHandler}.
 *
 * @author Mattias Markehed
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

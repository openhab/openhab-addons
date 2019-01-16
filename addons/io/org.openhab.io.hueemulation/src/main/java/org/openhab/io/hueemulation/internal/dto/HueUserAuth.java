/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hue user object. Used by {@link HueAuthorizedConfig}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueUserAuth {
    public String name = "";
    public String createDate = "";
    public String lastUseDate = "";

    /**
     * For de-serialization.
     */
    public HueUserAuth() {
    }

    /**
     * Create a new user
     *
     * @param name Visible name
     */
    public HueUserAuth(String name) {
        this.name = name;
        this.createDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
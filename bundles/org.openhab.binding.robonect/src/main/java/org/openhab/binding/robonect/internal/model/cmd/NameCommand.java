/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The command allows to set or retrieve the mower name.
 *
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class NameCommand implements Command {

    private @Nullable String newName;

    /**
     * Sets the mower name; a {@code null} name is sent as an empty string.
     *
     * @param newName - the mower name.
     * @return - the command instance.
     */
    public NameCommand withNewName(@Nullable String newName) {
        this.newName = newName != null ? newName : "";
        return this;
    }

    /**
     * Builds the mower name URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        if (newName == null) {
            return baseURL + "?cmd=name";
        } else {
            return baseURL + "?cmd=name&name=" + URLEncoder.encode(newName, StandardCharsets.ISO_8859_1);
        }
    }
}

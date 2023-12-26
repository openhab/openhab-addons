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
package org.openhab.binding.solax.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;

/**
 * The {@link GsonSupplier} provides a singleton instance of a Gson object
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class GsonSupplier {
    private static final Gson GSON = new Gson();

    private GsonSupplier() {
    };

    public static Gson getInstance() {
        return GSON;
    }
}

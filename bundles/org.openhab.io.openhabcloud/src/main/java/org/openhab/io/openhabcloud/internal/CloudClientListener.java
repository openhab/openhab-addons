/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.io.openhabcloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface provides callbacks from CloudClient
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to ESH APIs
 *
 */
@NonNullByDefault
public interface CloudClientListener {
    /**
     * This method receives command for an item from the openHAB Cloud client and should post it
     * into openHAB
     *
     * @param item the {@link String} containing item name
     * @param command the {@link String} containing a command
     * @param source the {@link String} containing the source of the command
     * @param userId the {@link String} containing the cloud user ID
     */
    void sendCommand(String item, String command, @Nullable String source, @Nullable String userId);
}

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
package org.openhab.binding.knx.internal.rest.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A KNX bridge: one with a handler, or the stored project of one that has none.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class BridgeDTO {
    public String uid;
    public @Nullable String label;
    public boolean present;
    public @Nullable String status;
    public @Nullable String connection;
    public @Nullable String ownAddress;
    public @Nullable Integer bufferSize;
    public String projectState;
    public @Nullable String projectName;
    public @Nullable Integer projectSpaces;
    public @Nullable Boolean keyringLoaded;
    public boolean sendAllowed;
}

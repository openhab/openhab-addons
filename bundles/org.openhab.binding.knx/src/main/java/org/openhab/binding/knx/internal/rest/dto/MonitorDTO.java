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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The header of the group monitor of a bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class MonitorDTO {
    public @Nullable String connection;
    public @Nullable String ownAddress;
    public int bufferSize;
    public int rows;
    public long lastNumber;
    public String monitorId;
    public long received;
    public long sent;
    public Map<String, Long> notDecrypted;
    public long notDecryptedOtherReason;
    public long plainOnSecuredAddress;
    public List<NotDecryptedDTO> notDecryptedSenders;
}

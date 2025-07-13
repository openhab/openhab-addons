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
package org.openhab.binding.zwavejs.internal.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * @author Leo Siepel - Initial contribution
 */
public class Node {
    public int nodeId;
    public int index;
    public Status status;
    public boolean ready;
    public boolean isListening;
    public boolean isRouting;
    public int manufacturerId;
    public int productId;
    public int productType;
    public String firmwareVersion;
    public DeviceConfig deviceConfig;
    public String label;
    public int interviewAttempts;
    public List<Endpoint> endpoints;
    public List<Value> values;
    public boolean isFrequentListening;
    public int maxDataRate;
    public List<Integer> supportedDataRates;
    public int protocolVersion;
    public boolean supportsBeaming;
    public boolean supportsSecurity;
    public DeviceClass deviceClass;
    public String interviewStage;
    public String deviceDatabaseUrl;
    public Statistics statistics;
    public boolean isControllerNode;
    public boolean keepAwake;
    public int protocol;
    public int installerIcon;
    public int userIcon;
    public boolean isSecure;
    public int zwavePlusVersion;
    public int nodeType;
    public int zwavePlusNodeType;
    public int zwavePlusRoleType;
    public int highestSecurityClass;
    public Instant lastSeen;
}

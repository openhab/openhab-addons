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

import java.util.List;

/**
 * @author Leo Siepel - Initial contribution
 */
public class Controller {
    public int type;
    public long homeId;
    public int ownNodeId;
    public boolean isUsingHomeIdFromOtherNetwork;
    public boolean isSISPresent;
    public boolean wasRealPrimary;
    public int manufacturerId;
    public int productType;
    public int productId;
    public List<Integer> supportedFunctionTypes;
    public int sucNodeId;
    public boolean supportsTimers;
    public Statistics statistics;
    public int inclusionState;
    public String sdkVersion;
    public String firmwareVersion;
    public boolean isPrimary;
    public boolean isSUC;
    public int nodeType;
    public int rfRegion;
    public int status;
    public boolean isRebuildingRoutes;
    public boolean supportsLongRange;
    public int maxLongRangePowerlevel;
    public int longRangeChannel;
    public boolean supportsLongRangeAutoChannelSelection;
}

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
package org.openhab.binding.unifiaccess.internal.dto;

import java.util.List;

/**
 * Device details
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Device {
    public String alias;
    public String id;
    public String name;
    public String type; // e.g., "UAH", "UDA-LITE", "UA-G2-PRO"
    public String locationId; // associated door/location id (for filtering)
    public Boolean isAdopted;
    public Boolean isConnected;
    public Boolean isManaged;
    public Boolean isOnline;
    public List<String> capabilities;
}

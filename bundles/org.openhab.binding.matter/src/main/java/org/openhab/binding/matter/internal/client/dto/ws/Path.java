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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

/**
 * Path
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Path {
    public BigInteger nodeId;
    public Integer endpointId;
    public Integer clusterId;
    public String clusterName;
    public Integer attributeId;
    public String attributeName;
    public Integer eventId;
    public String eventName;
}

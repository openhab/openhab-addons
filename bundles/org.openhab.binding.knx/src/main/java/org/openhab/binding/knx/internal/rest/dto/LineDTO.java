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

/**
 * A line of the topology with the devices of all its segments.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class LineDTO {
    public String address;
    public String name;
    public List<DeviceDTO> devices;
}

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

/**
 * The body of {@code POST /rest/things}, with the field names of core's {@code ThingDTO}. A record, as core's name
 * {@code UID} is no field name the code style allows.
 *
 * @author Stamate Viorel - Initial contribution
 */
public record ThingBodyDTO(String UID, String thingTypeUID, String bridgeUID, String label,
        Map<String, String> configuration, List<ChannelBodyDTO> channels) {
}

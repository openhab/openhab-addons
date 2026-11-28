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
 * A channel of a {@link ThingBodyDTO}, with the field names of core's {@code ChannelDTO}.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ChannelBodyDTO {
    public String uid;
    public String id;
    public String channelTypeUID;
    public String kind;
    public String itemType;
    public String label;
    public Map<String, String> configuration;
    public Map<String, String> properties;
    public List<String> defaultTags;
}

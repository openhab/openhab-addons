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
 * The channel ids of the lines for a target Thing that the Thing has already, or that the lines hold twice.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ExistingChannelsDTO {
    public String target;
    public List<String> channelIds;
}

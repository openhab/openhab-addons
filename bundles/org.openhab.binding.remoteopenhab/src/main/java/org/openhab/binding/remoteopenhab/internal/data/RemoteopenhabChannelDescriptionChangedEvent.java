/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.remoteopenhab.internal.data;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Payload from ChannelDescriptionChangedEvent events received through the SSE connection.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabChannelDescriptionChangedEvent {

    public String field = "";
    public String channelUID = "";
    public Set<String> linkedItemNames = Set.of();
    public String value = "";
    public @Nullable String oldValue = "";
}

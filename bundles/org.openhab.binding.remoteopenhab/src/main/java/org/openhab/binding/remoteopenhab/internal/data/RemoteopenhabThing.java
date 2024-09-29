/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response to the API GET /rest/things
 * Also payload from ThingAddedEvent / ThingRemovedEvent events received through the SSE connection.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabThing {

    public String label = "";
    @SerializedName("UID")
    public String uid = "";
    public @Nullable RemoteopenhabStatusInfo statusInfo;
    public List<RemoteopenhabChannel> channels = new ArrayList<>();
}

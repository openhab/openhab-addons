/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.NodeId;

import com.google.gson.stream.JsonReader;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link NodeId}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class NodeIdGsonAdapter extends GsonTypeAdapterBase<NodeId> {
    @Override
    public @Nullable NodeId readValue(final JsonReader in) throws IOException {
        return new NodeId(UUID.fromString(in.nextString()));
    }
}

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
package org.openhab.binding.smhi.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smhi.provider.ParameterMetadata;

@NonNullByDefault
public class MockSmhiChannelTypeProvider {
    private final Map<String, ParameterMetadata> storage = new HashMap<>();

    public MockSmhiChannelTypeProvider() {
    }

    public void putParameterMetadata(ParameterMetadata metadata) {
        storage.put(metadata.name(), metadata);
    }

    public @Nullable ParameterMetadata getParameterMetadata(String name) {
        return storage.get(name);
    }

    public Collection<ParameterMetadata> getAllParameterMetadata() {
        return storage.values();
    }
}

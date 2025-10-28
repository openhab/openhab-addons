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
package org.openhab.binding.ferroamp.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ferroamp.internal.api.DataType;
import org.openhab.binding.ferroamp.internal.api.FerroAmpUpdateListener;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class FerroAmpTestListener implements FerroAmpUpdateListener {

    public Map<String, @Nullable String> keyValueMap = new HashMap<>();

    @Override
    public void onFerroAmpUpdate(DataType type, Map<String, @Nullable String> keyValueMap) {
        this.keyValueMap = keyValueMap;
    }
}

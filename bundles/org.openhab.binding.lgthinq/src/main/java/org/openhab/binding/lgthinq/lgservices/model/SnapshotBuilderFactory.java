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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeSnapshotBuilder;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshotBuilder;

/**
 * The {@link SnapshotBuilderFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class SnapshotBuilderFactory {
    private final Map<Class<? extends SnapshotDefinition>, SnapshotBuilder<? extends SnapshotDefinition>> internalBuilders = new HashMap<>();

    private static final SnapshotBuilderFactory instance;
    static {
        instance = new SnapshotBuilderFactory();
    }

    private SnapshotBuilderFactory() {
    };

    public static SnapshotBuilderFactory getInstance() {
        return instance;
    }

    public SnapshotBuilder<? extends SnapshotDefinition> getBuilder(Class<? extends SnapshotDefinition> snapDef) {
        SnapshotBuilder<? extends SnapshotDefinition> result = internalBuilders.get(snapDef);
        if (result == null) {
            if (snapDef.equals(WasherDryerSnapshot.class)) {
                result = new WasherDryerSnapshotBuilder();
            } else if (snapDef.equals(ACCanonicalSnapshot.class)) {
                result = new ACSnapshotBuilder();
            } else if (snapDef.equals(FridgeCanonicalSnapshot.class)) {
                result = new FridgeSnapshotBuilder();
            } else {
                throw new IllegalStateException(
                        "Snapshot definition " + snapDef + " not supported by this Factory. It most likely a bug");
            }
            internalBuilders.put(snapDef, result);
        }
        return result;
    }
}

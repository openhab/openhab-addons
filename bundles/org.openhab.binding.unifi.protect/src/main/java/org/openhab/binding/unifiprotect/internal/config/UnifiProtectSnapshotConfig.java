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
package org.openhab.binding.unifiprotect.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UnifiProtectSnapshotConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectSnapshotConfig {
    public String sequence = "before";

    public enum Sequence {
        BEFORE("before"),
        AFTER("after"),
        NONE("none");

        private final String value;

        Sequence(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Sequence fromValue(String value) {
            return List.of(Sequence.values()).stream().filter(s -> s.getValue().equals(value)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sequence: " + value));
        }
    }
}

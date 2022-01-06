/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonWakeWords} encapsulate the GSON data of the wake word request
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonWakeWords {
    public @Nullable List<WakeWord> wakeWords;

    public static class WakeWord {
        public @Nullable Boolean active;
        public @Nullable String deviceSerialNumber;
        public @Nullable String deviceType;
        public @Nullable Object midFieldState;
        public @Nullable String wakeWord;
    }
}

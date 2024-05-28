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
package org.openhab.transform.basicprofiles.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for {@link org.openhab.transform.basicprofiles.internal.profiles.DebounceTimeStateProfile}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DebounceTimeStateProfileConfig {
    public int toHandlerDelay = 0;
    public int toItemDelay = 0;
    public DebounceMode mode = DebounceMode.LAST;

    @Override
    public String toString() {
        return "DebounceTimeStateProfileConfig{toHandlerDelay=" + toHandlerDelay + ", toItemDelay=" + toItemDelay
                + ", mode=" + mode + "}";
    }

    public enum DebounceMode {
        FIRST,
        LAST
    }
}

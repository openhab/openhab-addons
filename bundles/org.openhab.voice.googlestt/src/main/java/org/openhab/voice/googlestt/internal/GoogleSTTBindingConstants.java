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
package org.openhab.voice.googlestt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GoogleSTTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class GoogleSTTBindingConstants {
    /**
     * Service name
     */
    static final String SERVICE_NAME = "Google Cloud Speech-to-Text";
    /**
     * Service id
     */
    static final String SERVICE_ID = "googlestt";

    /**
     * Service category
     */
    static final String SERVICE_CATEGORY = "voice";

    /**
     * Service pid
     */
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
}

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
package org.openhab.binding.tasmotaplug.internal;

import static org.openhab.binding.tasmotaplug.internal.TasmotaPlugBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TasmotaPlugConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class TasmotaPlugConfiguration {
    public String hostName = BLANK;
    public int refresh = DEFAULT_REFRESH_PERIOD_SEC;
    public int numChannels = DEFAULT_NUM_CHANNELS;
    public String username = BLANK;
    public String password = BLANK;
}

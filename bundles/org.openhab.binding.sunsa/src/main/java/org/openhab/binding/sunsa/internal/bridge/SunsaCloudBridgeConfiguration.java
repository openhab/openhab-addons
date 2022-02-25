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
package org.openhab.binding.sunsa.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunsaCloudBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaCloudBridgeConfiguration {
    public String userId = "";
    public String apiKey = "";
    public String baseUri = "";

    public boolean isValid() {
        return !userId.isBlank() && !apiKey.isBlank();
    }
}

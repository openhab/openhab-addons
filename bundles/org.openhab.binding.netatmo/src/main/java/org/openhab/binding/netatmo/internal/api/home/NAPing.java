/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.home;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiResponse;

/**
 * The {@link NAPing} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NAPing extends ApiResponse<String> {
    private @NonNullByDefault({}) String localUrl;
    private @NonNullByDefault({}) String productName;

    @Override
    public String getStatus() {
        return localUrl;
    }

    @Override
    public String getBody() {
        return productName;
    }
}

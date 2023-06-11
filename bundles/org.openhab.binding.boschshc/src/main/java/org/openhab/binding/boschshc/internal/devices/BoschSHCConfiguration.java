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
package org.openhab.binding.boschshc.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link BoschSHCConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschSHCConfiguration {
    /**
     * ID of the device as returned by the controller.
     */
    public @Nullable String id;
}

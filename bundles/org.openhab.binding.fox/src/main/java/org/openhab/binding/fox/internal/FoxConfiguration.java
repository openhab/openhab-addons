/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FoxConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    public @Nullable String gateHost;
    public @Nullable String gatePassword;
    public @Nullable String functions;
}

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
package org.openhab.binding.blink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccountConfiguration} class contains fields mapping account bridge configuration parameters.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    public @NonNullByDefault({}) String email = "";
    public @NonNullByDefault({}) String password = "";
    public @NonNullByDefault({}) int refreshInterval = 120;
}

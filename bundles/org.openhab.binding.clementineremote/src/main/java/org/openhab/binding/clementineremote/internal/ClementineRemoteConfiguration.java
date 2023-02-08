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
package org.openhab.binding.clementineremote.internal;

import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.DEFAULT_HOST;
import static org.openhab.binding.clementineremote.internal.ClementineRemoteBindingConstants.DEFAULT_PORT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ClementineRemoteConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stephan Richter - Initial contribution
 */
@NonNullByDefault
public class ClementineRemoteConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String hostname = DEFAULT_HOST;
    @Nullable public Integer authCode = null;
    public int port = DEFAULT_PORT;
}

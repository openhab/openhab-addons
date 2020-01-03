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
package org.openhab.binding.pushover.internal.config;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.DEFAULT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PushoverAccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverAccountConfiguration {
    public @Nullable String apikey;
    public @Nullable String user;
    public String title = "openHAB";
    public String format = "none";
    public String sound = DEFAULT;
    public int retry = 300;
    public int expire = 3600;
}

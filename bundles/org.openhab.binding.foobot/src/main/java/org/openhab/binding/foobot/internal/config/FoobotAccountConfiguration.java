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
package org.openhab.binding.foobot.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoobotAccountConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author George Katsis - Initial contribution
 */
@NonNullByDefault
public class FoobotAccountConfiguration {

    public String apiKey = "";
    public String username = "";
    public int refreshInterval;
}

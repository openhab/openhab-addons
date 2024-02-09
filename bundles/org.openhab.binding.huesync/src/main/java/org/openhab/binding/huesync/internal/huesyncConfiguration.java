/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link huesyncConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Marco Kawon - Initial contribution
 * @author Patrik Gfeller - Integration into official repository, update to 4.x infrastructure
 */
@NonNullByDefault
public class huesyncConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String hostname = "";
    public String password = "";
    public int refreshInterval = 600;
}

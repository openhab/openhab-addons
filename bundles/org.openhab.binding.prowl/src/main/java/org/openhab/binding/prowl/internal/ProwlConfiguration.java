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
package org.openhab.binding.prowl.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ProwlConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class ProwlConfiguration {

    /**
     * Prowl configuration parameters.
     */
    public String apiKey = "";
    public String application = "openHAB";
    public int refresh = 30;
}

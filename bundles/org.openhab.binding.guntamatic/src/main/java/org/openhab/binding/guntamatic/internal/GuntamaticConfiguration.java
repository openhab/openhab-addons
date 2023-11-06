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
package org.openhab.binding.guntamatic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GuntamaticConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class GuntamaticConfiguration {

    /**
     * Configuration parameters
     */
    public String hostname = "";
    public String key = "";
    public int refreshInterval = 60;
    public String encoding = "windows-1252";
}

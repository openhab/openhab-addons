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
package org.openhab.binding.tibber.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TibberConfiguration} class contains fields mapping configuration parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 */
@NonNullByDefault
public class TibberConfiguration {
    private String token = "";
    private String homeid = "";
    private int refresh;

    public String getToken() {
        return token;
    }

    public String getHomeid() {
        return homeid;
    }

    public int getRefresh() {
        return refresh;
    }
}

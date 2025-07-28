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
package org.openhab.binding.worxlandroid.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WebApiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils - Initial contribution
 * @author Gaël L'hopital - Added NonNullByDefault, removed setters, removed reconnectInterval
 */
@NonNullByDefault
public class WebApiConfiguration {
    public String username = "";
    public String password = "";

    @Override
    public String toString() {
        return "WebApiConfiguration [username='%s', password='*****']".formatted(username);
    }
}

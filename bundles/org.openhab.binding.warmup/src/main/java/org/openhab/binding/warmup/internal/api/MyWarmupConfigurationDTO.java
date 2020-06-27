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
package org.openhab.binding.warmup.internal.api;

/**
 * The {@link MyWarmupConfigurationDTO} class contains fields mapping thing configuration parameters.
 *
 * @author James Melville - Initial contribution
 */
public class MyWarmupConfigurationDTO implements Comparable<MyWarmupConfigurationDTO> {

    public String username;
    public String password;

    @Override
    public int compareTo(MyWarmupConfigurationDTO o) {
        return this.username.equals(o.username) && this.password.equals(o.password) ? 0 : -1;
    }
}

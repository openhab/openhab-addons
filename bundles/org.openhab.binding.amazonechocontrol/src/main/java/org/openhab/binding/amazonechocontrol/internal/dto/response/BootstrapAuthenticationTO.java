/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link BootstrapAuthenticationTO} encapsulates the authentication information of a bootstrap response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BootstrapAuthenticationTO {
    public boolean authenticated;
    public boolean canAccessPrimeMusicContent;
    public String customerEmail;
    public String customerId;
    public String customerName;

    @Override
    public @NonNull String toString() {
        return "BootstrapAuthenticationTO{authenticated=" + authenticated + ", canAccessPrimeMusicContent="
                + canAccessPrimeMusicContent + ", customerEmail='" + customerEmail + "', customerId='" + customerId
                + "', customerName='" + customerName + "'}";
    }
}

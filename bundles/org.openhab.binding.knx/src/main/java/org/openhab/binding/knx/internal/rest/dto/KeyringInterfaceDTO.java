/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.rest.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * One interface entry of a keyring.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class KeyringInterfaceDTO {
    public String address;
    public String type;
    public @Nullable String host;
    public @Nullable Integer userId;
    public int linkedSecuredGroupAddresses;
    public boolean own;
}

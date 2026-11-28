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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * What the loaded keyring of a bridge says; never a key or a password.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class KeyringDTO {
    public boolean loaded;
    public @Nullable String fileName;
    public @Nullable Boolean signatureValid;
    public @Nullable Boolean backboneKeyPresent;
    public @Nullable Integer securedGroupAddresses;
    public @Nullable List<KeyringInterfaceDTO> interfaces;
}

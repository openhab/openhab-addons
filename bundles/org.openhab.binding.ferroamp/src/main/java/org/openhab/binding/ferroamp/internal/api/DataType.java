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
package org.openhab.binding.ferroamp.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * * The {@link DataType} enum defines the types of data that can be processed by the Ferroamp system.
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@NonNullByDefault
public enum DataType {
    UNKNOWN,
    EHUB,
    SSO,
    ESM,
    ESO
}
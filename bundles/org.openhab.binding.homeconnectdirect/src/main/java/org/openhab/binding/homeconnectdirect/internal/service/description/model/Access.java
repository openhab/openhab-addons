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
package org.openhab.binding.homeconnectdirect.internal.service.description.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Access level model from the device description.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public enum Access {
    READ_WRITE,
    READ,
    READ_STATIC,
    WRITE_ONLY,
    NONE
}

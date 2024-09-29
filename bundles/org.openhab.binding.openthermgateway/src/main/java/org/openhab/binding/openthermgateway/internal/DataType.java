/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DataType} enum indicates the type of data from a DataItem.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public enum DataType {
    FLAGS,
    UINT8,
    INT8,
    FLOAT,
    UINT16,
    INT16,
    DOWTOD
}

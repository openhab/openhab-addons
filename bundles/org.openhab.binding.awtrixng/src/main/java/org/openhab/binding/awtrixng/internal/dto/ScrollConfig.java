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
package org.openhab.binding.awtrixng.internal.dto;

/**
 * The {@code scroll} object of an app payload or of the global settings. Null fields are omitted when
 * serialized, because AWTRIX NG rejects a payload as a whole if it contains an invalid key.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class ScrollConfig {
    public String mode;
    public String direction;
    public String entry;
    public String whenFits;
    public Integer speed;
    public Integer gap;
    public Integer holdMs;
}

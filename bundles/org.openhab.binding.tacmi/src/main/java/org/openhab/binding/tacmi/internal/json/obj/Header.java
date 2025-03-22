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
package org.openhab.binding.tacmi.internal.json.obj;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class holding the Header JSON element
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 */
@NonNullByDefault
public class Header {
    public Integer version = -1;
    public String device = "";
    public Integer timestamp = 0;
}

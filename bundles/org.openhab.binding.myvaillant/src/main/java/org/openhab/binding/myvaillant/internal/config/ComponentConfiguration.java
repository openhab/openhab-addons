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
package org.openhab.binding.myvaillant.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of a zone, circuit or hot water Thing.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class ComponentConfiguration {

    /** Index of the zone, circuit or hot water in the system (hot water usually 255) */
    public int index = -1;
}

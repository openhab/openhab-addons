/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NAObjectMap} defines an hashmap of NAObjects identified
 * by their id.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAObjectMap<T extends NAObject> extends HashMap<String, T> {
    private static final long serialVersionUID = 7635233672795516649L;
}

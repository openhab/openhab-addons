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
package org.openhab.binding.govee.internal.model;

/**
 *
 * @param r red
 * @param g green
 * @param b blue
 *
 * @author Stefan Höhn - Initial contribution
 */
public record Color(int r, int g, int b) {
}

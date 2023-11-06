/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

/**
 * Hue Capabilities per endpoint
 * Enpoint: /api/{username}/capabilities
 * <p>
 * https://developers.meethue.com/develop/hue-api/10-capabilities-api/
 *
 * @author David Graeff - Initial contribution
 */
public class HueCapability {
    public int available = 10;
    public int total = 10000;
}

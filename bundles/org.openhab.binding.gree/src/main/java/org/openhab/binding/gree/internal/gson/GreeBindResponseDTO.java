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
package org.openhab.binding.gree.internal.gson;

/**
 *
 * The GreeBindResponseDTO class is used by Gson to hold values returned from
 * the Air Conditioner during Binding
 *
 * @author John Cunha - Initial contribution
 */
public class GreeBindResponseDTO extends GreeResponseBaseDTO {
    public transient GreeBindResponsePackDTO packJson = null;
}

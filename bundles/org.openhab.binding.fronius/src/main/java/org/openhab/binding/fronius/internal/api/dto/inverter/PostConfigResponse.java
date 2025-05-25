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
package org.openhab.binding.fronius.internal.api.dto.inverter;

import java.util.List;

/**
 * Record representing the response of a POST request to a <code>/config</code> endpoint.
 *
 * @author Florian Hotze - Initial contribution
 */
public record PostConfigResponse(List<String> errors, List<String> permissionFailure, List<String> unknownNodes,
        List<String> validationErrors, List<String> writeFailure, List<String> writeSuccess) {
}

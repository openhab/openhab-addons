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
package org.openhab.binding.dreame.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Device information returned by the Dreamehome account API.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public record DreameDevice(String id, String name, String model, String version, String masterUid, String bindDomain,
        String property) {
}

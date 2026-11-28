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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An object instance together with the device it belongs to.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxLinkedObject(KnxDevice device, KnxObjectInstance object) {
}

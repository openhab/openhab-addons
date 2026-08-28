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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A map descriptor returned by Dreamehome.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public record DreameMap(int id, int index, String name, BigDecimal area) {
}

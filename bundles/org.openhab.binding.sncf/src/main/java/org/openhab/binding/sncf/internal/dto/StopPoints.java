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
package org.openhab.binding.sncf.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StopPoints} holds a list of Stop Points.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class StopPoints extends SncfAnswer {
    public @Nullable List<StopPoint> stopPoints;
}

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
package org.openhab.binding.knx.internal.rest.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * What the wizard proposes Things for: group addresses, functions, spaces and devices of the
 * project. {@code readAtStart} left out counts as true.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposalRequestDTO {
    public @Nullable List<String> groupAddresses;
    public @Nullable List<String> functions;
    public @Nullable List<String> spaces;
    public @Nullable List<String> devices;
    public @Nullable Boolean readAtStart;
}

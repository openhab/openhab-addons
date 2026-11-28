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
 * The body of {@code PUT /rest/items/{name}}, with the field names of core's {@code ItemDTO}.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ItemBodyDTO {
    public String type;
    public String name;
    public String label;
    public @Nullable String category;
    public List<String> tags;
    public List<String> groupNames;
}

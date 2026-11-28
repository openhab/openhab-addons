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

import org.eclipse.jdt.annotation.Nullable;

/**
 * One difference between a configured address and the project.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class DifferenceDTO {
    public String kind;
    public String address;
    public @Nullable String configuredDataType;
    public @Nullable String projectDataType;
    public String label;
    public boolean noteOnly;
}

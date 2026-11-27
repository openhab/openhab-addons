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
package org.openhab.binding.bmwcardata.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A telematic data container. A container defines the set of technical descriptors that is returned when telematic
 * data is requested for it.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class Container {
    public static final String STATE_ACTIVE = "ACTIVE";

    public @Nullable String containerId;
    public @Nullable String name;
    public @Nullable String purpose;
    public @Nullable String state;
    public @Nullable List<String> technicalDescriptors;

    /**
     * Wrapper of the response of {@code GET /customers/containers}.
     */
    public static class ContainerList {
        public @Nullable List<Container> containers;
    }
}

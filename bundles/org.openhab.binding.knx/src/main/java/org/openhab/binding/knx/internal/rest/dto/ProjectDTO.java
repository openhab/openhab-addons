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
 * The state of the stored ETS project of a bridge and, when it is loaded, what it holds.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProjectDTO {
    public String state;
    public @Nullable String name;
    public @Nullable String installation;
    public @Nullable String importedAt;
    public @Nullable String sourceName;
    public @Nullable String language;
    public @Nullable String groupAddressStyle;
    public @Nullable String toolVersion;
    public @Nullable String lastModified;
    public @Nullable Integer groupAddresses;
    public @Nullable Integer securedGroupAddresses;
    public @Nullable Integer devices;
    public @Nullable Integer groupObjects;
    public @Nullable Integer ignoredInstallations;
}

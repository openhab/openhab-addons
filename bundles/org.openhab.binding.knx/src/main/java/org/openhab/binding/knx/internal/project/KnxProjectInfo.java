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
import org.eclipse.jdt.annotation.Nullable;

/**
 * What {@code project.xml} says about a project.
 *
 * @param id the id of the project part in the container, {@code P-xxxx}
 * @param name the project name, {@code ""} when it has none
 * @param groupAddressStyle the style group addresses are shown in; {@link KnxGroupAddressStyle#THREE_LEVEL}
 *            when the project names none or an unknown one
 * @param createdBy the {@code CreatedBy} attribute of the root element, for example {@code ETS6}
 * @param toolVersion the {@code ToolVersion} attribute of the root element
 * @param schemaVersion the number at the end of the project namespace, {@code null} when the namespace has
 *            another form
 * @param lastModified the {@code LastModified} attribute as written, an ISO 8601 date and time
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxProjectInfo(String id, String name, KnxGroupAddressStyle groupAddressStyle, @Nullable String createdBy,
        @Nullable String toolVersion, @Nullable Integer schemaVersion, @Nullable String lastModified) {
}

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
package org.openhab.binding.knx.internal.projectstore;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProject;

/**
 * The ETS project kept for a bridge.
 *
 * @param project the project as read, without the parameter values and module instances of its devices
 * @param importedAt when it was imported
 * @param sourceName the name of the imported file, without any folder
 * @param language the language the texts were read in, {@code null} for the default texts
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record StoredProject(KnxProject project, Instant importedAt, String sourceName, @Nullable String language) {
}

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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Version information for one Robonect software component.
 */
public class VersionDetail {

    private @Nullable String version;
    private @Nullable String compiled;
    private @Nullable String comment;

    public @Nullable String getVersion() {
        return version;
    }

    public @Nullable String getCompiled() {
        return compiled;
    }

    public @Nullable String getComment() {
        return comment;
    }
}

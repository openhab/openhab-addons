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
package org.openhab.binding.veluxactive.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Home status and per-module failures returned in the response body.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HomeStatus {
    public @Nullable Home home;
    public @Nullable List<@Nullable ModuleError> errors;

    public @Nullable Module module(String moduleId) {
        Home home = this.home;
        List<@Nullable Module> modules = home == null ? null : home.modules;
        if (modules != null) {
            for (Module module : modules) {
                if (module != null && moduleId.equals(module.id)) {
                    return module;
                }
            }
        }
        return null;
    }

    public @Nullable ModuleError error(String moduleId) {
        List<@Nullable ModuleError> errors = this.errors;
        ModuleError homeError = null;
        if (errors != null) {
            for (ModuleError error : errors) {
                if (error != null) {
                    if (moduleId.equals(error.id)) {
                        return error;
                    }
                    if (error.id == null) {
                        homeError = error;
                    }
                }
            }
        }
        return homeError;
    }
}

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
package org.openhab.transform.basicprofiles.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.transform.basicprofiles.internal.profiles.StateFilterProfile;

/**
 * Configuration class for {@link StateFilterProfile}.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class StateFilterProfileConfig {

    public List<String> conditions = List.of();

    public @Nullable String mismatchState;

    public String separator = ",";
}

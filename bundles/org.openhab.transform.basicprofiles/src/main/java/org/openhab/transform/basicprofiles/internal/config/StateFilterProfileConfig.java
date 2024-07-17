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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.basicprofiles.internal.profiles.StateFilterProfile;

/**
 * Configuration class for {@link StateFilterProfile}.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class StateFilterProfileConfig {

    public String conditions = "";

    public String mismatchState = UnDefType.UNDEF.toString();

    public String separator = ",";
}

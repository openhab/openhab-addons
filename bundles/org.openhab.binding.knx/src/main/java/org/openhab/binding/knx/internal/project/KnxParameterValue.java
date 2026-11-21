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
 * A parameter value stored on a device ({@code ParameterInstanceRef}).
 *
 * @param refId the full parameter reference, {@code <appId>_P-n_R-n}; inside a module
 *            {@code <appId>_MD-n_M-n_MI-n_P-n_R-n}
 * @param value the value as written; {@code null} when the element has no {@code Value} attribute, which is not
 *            the same as the empty text
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxParameterValue(String refId, @Nullable String value) {
}

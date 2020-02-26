/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.feature;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class FeatureBuilderUtil {
    private static final String CANNOT_SET_TARGET_MESSAGE = "Cannot set target for \"{0}\" because that attribute is not available";

    public static String getCannotSetTargetMessage(final String attributeName) {
        return MessageFormat.format(CANNOT_SET_TARGET_MESSAGE, attributeName);
    }

    private FeatureBuilderUtil() {
        throw new AssertionError();
    }
}

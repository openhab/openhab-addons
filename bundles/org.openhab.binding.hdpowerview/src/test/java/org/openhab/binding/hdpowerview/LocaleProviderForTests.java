/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;

/**
 * Locale provider for unit tests.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class LocaleProviderForTests implements LocaleProvider {
    public Locale getLocale() {
        return Locale.ENGLISH;
    }
}

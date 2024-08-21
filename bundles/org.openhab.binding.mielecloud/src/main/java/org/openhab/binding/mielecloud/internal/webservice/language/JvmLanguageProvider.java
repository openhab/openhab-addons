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
package org.openhab.binding.mielecloud.internal.webservice.language;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link LanguageProvider} returning the default JVM language.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class JvmLanguageProvider implements LanguageProvider {
    @Override
    public Optional<String> getLanguage() {
        return Optional.ofNullable(Locale.getDefault()).map(Locale::getLanguage).filter(l -> !l.isEmpty());
    }
}

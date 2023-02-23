/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for providing language code information.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public interface LanguageProvider {
    /**
     * Gets a language represented as 2-letter language code.
     *
     * @return The language represented as 2-letter language code.
     */
    Optional<String> getLanguage();
}

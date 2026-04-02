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
package org.openhab.io.yamlcomposer.internal.processors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;

/**
 * The {@link PlaceholderProcessor} processes placeholder instances in YAML models.
 *
 * @param <T> The type of placeholder to be processed
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface PlaceholderProcessor<T extends Placeholder> {

    @Nullable
    Object process(T placeholder, RecursiveTransformer recursiveTransformer);

    Class<T> getPlaceholderType();
}

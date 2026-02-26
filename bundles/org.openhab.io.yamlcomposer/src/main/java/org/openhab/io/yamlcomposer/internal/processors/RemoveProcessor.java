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
import org.openhab.io.yamlcomposer.internal.core.RemovalSignal;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;

/**
 * The {@link RemoveProcessor} processes {@link RemovePlaceholder} instances in YAML models.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class RemoveProcessor implements PlaceholderProcessor<RemovePlaceholder> {

    @Override
    public Class<RemovePlaceholder> getPlaceholderType() {
        return RemovePlaceholder.class;
    }

    @Override
    public @Nullable Object process(RemovePlaceholder placeholder, RecursiveTransformer recursiveTransformer) {
        return RemovalSignal.REMOVE;
    }
}

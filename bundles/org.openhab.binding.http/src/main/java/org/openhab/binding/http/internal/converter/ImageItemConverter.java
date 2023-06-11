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
package org.openhab.binding.http.internal.converter;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.http.internal.http.Content;
import org.openhab.core.library.types.RawType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link ImageItemConverter} implements {@link org.openhab.core.library.items.ImageItem} conversions
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class ImageItemConverter implements ItemValueConverter {
    private final Consumer<State> updateState;

    public ImageItemConverter(Consumer<State> updateState) {
        this.updateState = updateState;
    }

    @Override
    public void process(Content content) {
        String mediaType = content.getMediaType();
        updateState.accept(
                new RawType(content.getRawContent(), mediaType != null ? mediaType : RawType.DEFAULT_MIME_TYPE));
    }

    @Override
    public void send(Command command) {
        throw new IllegalStateException("Read-only channel");
    }
}

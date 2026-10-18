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
package org.openhab.io.yamlcomposer.internal.constructors;

import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.placeholders.InterpolablePlaceholder;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * The {@link ConstructInterpolablePlaceholder} is a generic constructor used to create
 * placeholder objects for nodes whose value can be interpolated by {@link YamlComposer}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ConstructInterpolablePlaceholder<T extends InterpolablePlaceholder<T>> implements ConstructNode {
    private final BiFunction<@Nullable Object, String, T> creator;
    private final ModelConstructor constructor;

    ConstructInterpolablePlaceholder(ModelConstructor constructor, BiFunction<@Nullable Object, String, T> creator) {
        this.constructor = constructor;
        this.creator = creator;
    }

    @Override
    public @Nullable Object construct(@Nullable Node node) {
        if (node == null) {
            return null;
        }
        Object value = constructor.constructByType(node);
        return creator.apply(value, constructor.getLocation(node));
    }
}

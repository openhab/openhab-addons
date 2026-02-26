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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * The {@link ConstructSub} is the constructor used on the <code>!sub</code> tag
 * to keep track of custom substitution patterns.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ConstructSub implements ConstructNode {
    private static final String SUB_TAG_PREFIX = "sub:";

    ModelConstructor constructor;

    ConstructSub(ModelConstructor constructor) {
        this.constructor = constructor;
    }

    @Override
    @NonNullByDefault({})
    public Object construct(Node node) {
        String patternName = extractPatternName(Objects.requireNonNull(node.getTag()));
        constructor.trackPatternName(patternName);
        return constructor.constructByType(node);
    }

    private @Nullable String extractPatternName(Tag tag) {
        String suffix = extractSubTagSuffix(tag);
        if (suffix == null || suffix.isBlank()) {
            return null;
        }
        return suffix;
    }

    private @Nullable String extractSubTagSuffix(Tag tag) {
        String tagValue = tag.getValue();

        // The tag value doesn't always start with !, for example
        // when using the verbatim tag syntax like !<sub:pattern>
        // the value will be "sub:pattern" without the leading "!"
        // see https://yaml.org/spec/1.2.2/#verbatim-tags
        if (tagValue.startsWith("!")) {
            tagValue = tagValue.substring(1);
        }

        if (!tagValue.startsWith(SUB_TAG_PREFIX)) {
            return null;
        }

        return tagValue.substring(SUB_TAG_PREFIX.length());
    }
}

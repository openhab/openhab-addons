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
package org.openhab.io.yamlcomposer.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.yamlcomposer.internal.constructors.ModelConstructor;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.CoreScalarResolver;

/**
 * A custom scalar resolver for SnakeYAML Engine that shifts structural processing
 * from the engine's native layer to our internal composer layer.
 *
 * <p>
 * This implementation modifies the default SnakeYAML Engine resolution in two key ways:
 * </p>
 * <ul>
 * <li><b>Custom Merge Key Handling:</b> It disables the default merge key ({@code <<})
 * resolution. Instead, it registers a custom implicit resolver that maps the
 * merge key to a unique tag. This allows the composer to handle object
 * merging manually, providing more control over the transformation lifecycle.</li>
 *
 * <li><b>Collision Prevention:</b> It opts out of the default {@code ENV_TAG}
 * resolver. This ensures that the standard YAML environment variable syntax
 * ({@code ${VAR}}) does not interfere with our custom substitution syntax.</li>
 * </ul>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ModelResolver extends CoreScalarResolver {
    ModelResolver() {
        super(false);
        addImplicitResolver(ModelConstructor.DEFERRED_MERGE_TAG, MERGE, "<");
    }

    @Override
    @NonNullByDefault({})
    public void addImplicitResolver(Tag tag, Pattern regexp, String first) {
        if (!Tag.ENV_TAG.equals(tag)) {
            super.addImplicitResolver(tag, regexp, first);
        }
    }
}

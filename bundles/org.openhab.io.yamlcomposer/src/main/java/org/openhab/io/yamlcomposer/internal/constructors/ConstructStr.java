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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

/**
 * The {@link ConstructStr} is the constructor used for STR tag which
 * may create a {@link org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder}, depending on the
 * current
 * substitution stack.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ConstructStr implements ConstructNode {
    private final ModelConstructor constructor;

    ConstructStr(ModelConstructor constructor) {
        this.constructor = constructor;
    }

    @Override
    @NonNullByDefault({})
    public Object construct(Node node) {
        return constructor.constructScalarOrSubstitution((ScalarNode) node);
    }
}

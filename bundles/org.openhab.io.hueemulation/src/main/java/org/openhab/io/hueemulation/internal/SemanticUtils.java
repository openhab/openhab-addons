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
package org.openhab.io.hueemulation.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.SemanticTags;
import org.openhab.core.semantics.SemanticsPredicates;
import org.openhab.core.semantics.Tag;

/**
 * This utility class provides methods for working with items belonging to
 * the semantic model.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SemanticUtils {
    public static @Nullable GroupItem getSemanticGroupItem(ItemRegistry itemRegistry, Item item, SemanticTag tag) {
        Class<? extends Tag> semanticRootTag = SemanticTags.getById(tag.getUID());
        if (semanticRootTag == null) {
            return null;
        }

        return item.getGroupNames().stream().map(itemRegistry::get).filter(Objects::nonNull)
                .filter(GroupItem.class::isInstance).map(GroupItem.class::cast)
                .filter(SemanticsPredicates.isA(semanticRootTag)).findFirst().orElse(null);
    }
}

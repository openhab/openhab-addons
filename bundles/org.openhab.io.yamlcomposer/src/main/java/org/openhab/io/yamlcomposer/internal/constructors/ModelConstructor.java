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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ReplacePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeType;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * Extends SnakeYAML Engine's {@link StandardConstructor} to add support for the
 * composer's custom YAML tags and model‑transformation features.
 *
 * <p>
 * The {@code ModelConstructor} handles all extended tags, including:
 * <ul>
 * <li><code>!sub</code> - variable interpolation</li>
 * <li><code>!nosub</code> - disable interpolation for a value</li>
 * <li><code>!if</code> - conditional evaluation</li>
 * <li><code>!include</code> - include external YAML files</li>
 * <li><code>!insert</code> - insert templates with variable context</li>
 * <li><code>!replace</code> - replace parts of the model within a package</li>
 * <li><code>!remove</code> - remove parts of the model within a package</li>
 * </ul>
 *
 * These extensions allow the composer to construct a fully evaluated
 * in‑memory model before further processing or consumption.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ModelConstructor extends StandardConstructor {
    static final String SUB_TAG = "sub";

    private static final Tag NOSUB_TAG = new Tag("!nosub");
    private static final Tag IF_TAG = new Tag("!if");
    private static final Tag INCLUDE_TAG = new Tag("!include");
    private static final Tag REPLACE_TAG = new Tag("!replace");
    private static final Tag REMOVE_TAG = new Tag("!remove");
    private static final Tag INSERT_TAG = new Tag("!insert");

    // A replacement for the built in Tag.MERGE because we want to
    // do the merge key processing at the composer level
    public static final Tag DEFERRED_MERGE_TAG = new Tag("!deferred-merge");

    String sourcePath;
    final Deque<Boolean> substitutionStack = new ArrayDeque<>();
    final Deque<@Nullable String> substitutionPatternNameStack = new LinkedList<>();

    private final ConstructSub constructSub;

    public ModelConstructor(LoadSettings settings, String sourcePath) {
        super(settings);

        this.sourcePath = sourcePath;
        this.substitutionStack.push(false);
        this.substitutionPatternNameStack.push(null);

        this.constructSub = new ConstructSub(this);
        this.tagConstructors.put(NOSUB_TAG, new ConstructNoSub(this));

        this.tagConstructors.put(Tag.STR, new ConstructStr(this));

        this.tagConstructors.put(DEFERRED_MERGE_TAG,
                new ConstructInterpolablePlaceholder<MergeKeyPlaceholder>(this, MergeKeyPlaceholder::new));
        this.tagConstructors.put(IF_TAG, new ConstructInterpolablePlaceholder<IfPlaceholder>(this, IfPlaceholder::new));
        this.tagConstructors.put(INCLUDE_TAG,
                new ConstructInterpolablePlaceholder<IncludePlaceholder>(this, IncludePlaceholder::new));
        this.tagConstructors.put(INSERT_TAG,
                new ConstructInterpolablePlaceholder<InsertPlaceholder>(this, InsertPlaceholder::new));
        this.tagConstructors.put(REPLACE_TAG,
                new ConstructInterpolablePlaceholder<ReplacePlaceholder>(this, ReplacePlaceholder::new));
        this.tagConstructors.put(REMOVE_TAG,
                new ConstructInterpolablePlaceholder<RemovePlaceholder>(this, RemovePlaceholder::new));
    }

    @Override
    @NonNullByDefault({})
    @SuppressWarnings("null")
    protected Optional<ConstructNode> findConstructorFor(Node node) {
        if (isSubstitutionTag(node.getTag())) {
            return Optional.of(constructSub);
        }
        return super.findConstructorFor(node);
    }

    /**
     * Gets a string representation of the node's location for logging purposes.
     *
     * @param node the YAML node to get the location of
     * @return a string describing the source location of the node, including file path and line/column if available
     */
    String getLocation(Node node) {
        String location = "";
        Mark startMark = node.getStartMark().orElse(null);
        if (startMark != null) {
            location = ":%d:%d".formatted(startMark.getLine() + 1, startMark.getColumn() + 1);
        }
        return this.sourcePath + location;
    }

    /**
     * Default construction method that routes to the appropriate construct method
     * based on the node type.
     *
     * Use this instead of constructObject() to avoid an infinite recursion when
     * constructing a node on a custom tag.
     *
     * @param node the node to construct
     * @return the constructed object
     */
    protected @Nullable Object constructByType(Node node) {
        NodeType type = node.getNodeType();
        if (type == NodeType.MAPPING) {
            return constructMapping((MappingNode) node);
        }
        if (type == NodeType.SEQUENCE) {
            return constructSequence((SequenceNode) node);
        }
        if (type == NodeType.SCALAR) {
            return constructScalarOrSubstitution((ScalarNode) node);
        }
        return constructObject(node);
    }

    /**
     * Construct a scalar node, potentially as a SubstitutionPlaceholder
     * if the current substitution state is enabled.
     *
     * @param scalarNode the scalar node to construct
     * @return
     */
    @SuppressWarnings("null") // The stacks and SnakeYAML methods shouldn't return null
    protected @Nullable Object constructScalarOrSubstitution(ScalarNode scalarNode) {
        Tag tag = scalarNode.getTag();
        String value = constructScalar(scalarNode);
        boolean enabled = substitutionStack.peek();
        if (enabled || isSubstitutionTag(tag)) {
            String patternName = substitutionPatternNameStack.peek();
            String location = getLocation(scalarNode);
            return new SubstitutionPlaceholder(value, patternName, location);
        }
        return value;
    }

    /**
     * Intercept constructObject to keep track of the current substitution state.
     *
     * @param node the node to construct
     * @return the constructed object
     */
    @Override
    @NonNullByDefault({})
    protected @Nullable Object constructObject(Node node) {
        Tag tag = Objects.requireNonNull(node.getTag());
        boolean parent = Objects.requireNonNull(substitutionStack.peek());
        boolean enabled = resolveSubstitution(tag, parent);
        substitutionStack.push(enabled);
        substitutionPatternNameStack.push(substitutionPatternNameStack.peek());
        try {
            return super.constructObject(node);
        } finally {
            substitutionPatternNameStack.pop();
            substitutionStack.pop();
        }
    }

    private static boolean resolveSubstitution(Tag tag, boolean parent) {
        if (NOSUB_TAG.equals(tag)) {
            return false;
        }

        if (isSubstitutionTag(tag)) {
            return true;
        }
        return parent;
    }

    static boolean isSubstitutionTag(Tag tag) {
        String value = tag.getValue();
        if (value.startsWith("!")) {
            value = value.substring(1);
        }
        return value.startsWith(SUB_TAG);
    }

    protected void trackPatternName(@Nullable String patternName) {
        substitutionPatternNameStack.pop();
        substitutionPatternNameStack.push(patternName);
    }
}

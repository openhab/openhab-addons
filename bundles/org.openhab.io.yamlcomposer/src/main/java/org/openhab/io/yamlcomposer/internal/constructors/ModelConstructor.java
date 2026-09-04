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
import org.openhab.io.yamlcomposer.internal.placeholders.DefaultPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ElseIfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ElsePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.ForPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.FreezePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.VarPlaceholder;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.schema.CoreSchema;

/**
 * Extends SnakeYAML Engine's {@link StandardConstructor} to add support for the
 * composer's custom YAML tags and model‑transformation features.
 *
 * <p>
 * The {@code ModelConstructor} handles all extended tags, grouped by category:
 *
 * <p>
 * <b>Variables:</b>
 * </p>
 * <ul>
 * <li><code>!var</code> — local variable definition and scope assignment</li>
 * </ul>
 *
 * <p>
 * <b>Conditionals:</b>
 * </p>
 * <ul>
 * <li><code>!if</code> — conditional evaluation</li>
 * <li><code>!elseif</code> (aliases: <code>!elsif</code>, <code>!elif</code>) —
 * conditional branch evaluation</li>
 * <li><code>!else</code> — fallback conditional branch execution</li>
 * </ul>
 *
 * <p>
 * <b>Loops:</b>
 * </p>
 * <ul>
 * <li><code>!for</code> — collection iteration and destructuring</li>
 * </ul>
 *
 * <p>
 * <b>Structural Merge and Manipulation Directives:</b>
 * </p>
 * <p>
 * These directives control how nodes participate in structural merging,
 * enabling fine‑grained manipulation of maps, sequences, and composite
 * model fragments. They complement YAML’s standard merge key (<code>&lt;&lt;:</code>)
 * by providing deterministic, rule‑based behaviour for deep merges,
 * conflict resolution, and selective replacement.
 * </p>
 *
 * <ul>
 * <li><code>!deep</code> —
 * Performs a recursive, structural merge of nested mappings and sequences.
 * A <code>!deep &lt;&lt;</code> directive recursively fills missing target
 * values while preserving target precedence. Individual entries may refine
 * this behaviour through value tags such as <code>!default</code>, <code>!freeze</code>/<code>!replace</code>,
 * and <code>!remove</code>, allowing precise control
 * over how each node participates in the merge process.
 * </li>
 *
 * <li><code>!default</code> —
 * Marks an entry as a fallback value. Incoming sources replace tagged scalar
 * values, take precedence on conflicts while recursively merging tagged maps,
 * and replace tagged lists. Untagged target values remain authoritative.
 * </li>
 *
 * <li><code>!freeze</code> —
 * Declares a key, mapping, or subtree as immutable for the remainder of the
 * merge process. Frozen nodes cannot be overridden, replaced, or structurally
 * merged with incoming sources, ensuring authoritative configuration fragments
 * remain intact.
 * </li>
 *
 * <li><code>!replace</code> —
 * This is an alias for <code>!freeze</code>. Pick one of the two tags to use for clarity in your model.
 * Both tags have the same effect of marking a node as authoritative and preventing it from being overridden
 * or merged with incoming sources.
 * </li>
 *
 * <li><code>!remove</code> —
 * Marks a key or sequence element for removal from the final merged structure.
 * When encountered during a merge, the corresponding target entry is deleted
 * regardless of origin or precedence.
 * </li>
 *
 * </ul>
 *
 * <p>
 * <b>File and Template Directives:</b>
 * </p>
 * <ul>
 * <li><code>!include</code> — external YAML file inclusion</li>
 * <li><code>!insert</code> — template insertion with local variable context</li>
 * </ul>
 *
 * <p>
 * <b>Miscellaneous:</b>
 * </p>
 * <ul>
 * <li><code>!literal</code> — disable evaluation for raw scalar values</li>
 * <li><code>!sub</code> — variable string interpolation</li>
 * </ul>
 *
 * <p>
 * These extensions allow the composer to construct a fully evaluated
 * in‑memory model before further processing or consumption.
 * </p>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ModelConstructor extends StandardConstructor {
    static final String SUB_TAG = "sub";

    private static final Tag LITERAL_TAG = new Tag("!literal");
    private static final Tag IF_TAG = new Tag("!if");
    private static final Tag ELSE_IF_TAG = new Tag("!elseif");
    private static final Tag ELSIF_TAG = new Tag("!elsif"); // Ruby-style alias for !elseif
    private static final Tag ELIF_TAG = new Tag("!elif"); // Python-style alias for !elseif
    private static final Tag ELSE_TAG = new Tag("!else");
    private static final Tag FOR_TAG = new Tag("!for");
    private static final Tag VAR_TAG = new Tag("!var");
    private static final Tag REPLACE_TAG = new Tag("!replace");
    private static final Tag FREEZE_TAG = new Tag("!freeze"); // alias for !replace
    private static final Tag REMOVE_TAG = new Tag("!remove");
    private static final Tag DEFAULT_TAG = new Tag("!default");
    private static final Tag INCLUDE_TAG = new Tag("!include");
    private static final Tag INSERT_TAG = new Tag("!insert");

    // Replacements for the built in Tag.MERGE because we want to
    // do the merge key processing at the composer level
    public static final Tag SHALLOW_MERGE_TAG = new Tag("!shallow");
    private static final Tag DEEP_MERGE_TAG = new Tag("!deep");

    private final FallbackConstructor fallbackConstructor;

    String sourcePath;
    final Deque<Boolean> substitutionStack = new ArrayDeque<>();
    final Deque<@Nullable String> substitutionPatternNameStack = new LinkedList<>();

    private final ConstructSub constructSub;

    public ModelConstructor(LoadSettings settings, String sourcePath) {
        super(settings);
        this.fallbackConstructor = new FallbackConstructor();
        this.sourcePath = sourcePath;
        this.substitutionStack.push(true);
        this.substitutionPatternNameStack.push(null);

        this.constructSub = new ConstructSub(this);
        this.tagConstructors.put(LITERAL_TAG, new ConstructLiteral(this));

        this.tagConstructors.put(Tag.STR, new ConstructStr(this));

        this.tagConstructors.put(IF_TAG, new ConstructInterpolablePlaceholder<IfPlaceholder>(this, IfPlaceholder::new));

        this.tagConstructors.put(ELSE_IF_TAG, new ConstructInterpolablePlaceholder<ElseIfPlaceholder>(this,
                (value, location) -> new ElseIfPlaceholder(ELSE_IF_TAG.getValue(), value, location)));
        this.tagConstructors.put(ELSIF_TAG, new ConstructInterpolablePlaceholder<ElseIfPlaceholder>(this,
                (value, location) -> new ElseIfPlaceholder(ELSIF_TAG.getValue(), value, location)));
        this.tagConstructors.put(ELIF_TAG, new ConstructInterpolablePlaceholder<ElseIfPlaceholder>(this,
                (value, location) -> new ElseIfPlaceholder(ELIF_TAG.getValue(), value, location)));

        this.tagConstructors.put(ELSE_TAG, new ConstructInterpolablePlaceholder<>(this, ElsePlaceholder::new));

        this.tagConstructors.put(FOR_TAG,
                new ConstructInterpolablePlaceholder<ForPlaceholder>(this, ForPlaceholder::new));
        this.tagConstructors.put(VAR_TAG,
                new ConstructInterpolablePlaceholder<VarPlaceholder>(this, VarPlaceholder::new));

        this.tagConstructors.put(SHALLOW_MERGE_TAG, new ConstructInterpolablePlaceholder<MergeKeyPlaceholder>(this,
                (value, location) -> new MergeKeyPlaceholder(false, value, location)));
        this.tagConstructors.put(DEEP_MERGE_TAG, new ConstructInterpolablePlaceholder<MergeKeyPlaceholder>(this,
                (value, location) -> new MergeKeyPlaceholder(true, value, location)));
        this.tagConstructors.put(REPLACE_TAG,
                new ConstructInterpolablePlaceholder<FreezePlaceholder>(this, FreezePlaceholder::new));
        this.tagConstructors.put(FREEZE_TAG,
                new ConstructInterpolablePlaceholder<FreezePlaceholder>(this, FreezePlaceholder::new));
        this.tagConstructors.put(DEFAULT_TAG,
                new ConstructInterpolablePlaceholder<DefaultPlaceholder>(this, DefaultPlaceholder::new));
        this.tagConstructors.put(REMOVE_TAG,
                new ConstructInterpolablePlaceholder<RemovePlaceholder>(this, RemovePlaceholder::new));

        this.tagConstructors.put(INCLUDE_TAG,
                new ConstructInterpolablePlaceholder<IncludePlaceholder>(this, IncludePlaceholder::new));
        this.tagConstructors.put(INSERT_TAG,
                new ConstructInterpolablePlaceholder<InsertPlaceholder>(this, InsertPlaceholder::new));
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
        return switch (node) {
            case MappingNode mappingNode -> constructMapping(mappingNode);
            case SequenceNode sequenceNode -> constructSequence(sequenceNode);
            case ScalarNode scalarNode -> constructScalarOrSubstitution(scalarNode);
            default -> constructObject(node);
        };
    }

    /**
     * Construct a scalar node, potentially as a SubstitutionPlaceholder
     * if the current substitution state is enabled.
     *
     * @param scalarNode the scalar node to construct
     * @return the constructed scalar or placeholder
     */
    @SuppressWarnings("null") // The stacks and SnakeYAML methods shouldn't return null
    protected @Nullable Object constructScalarOrSubstitution(ScalarNode scalarNode) {
        Object implicitValue = constructImplicitScalar(scalarNode);
        if (Tag.NULL.equals(implicitValue)) {
            return null;
        }

        if (implicitValue != null) {
            return implicitValue;
        }

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
     * Attempts implicit scalar type parsing (Integer, Float, Boolean) for unquoted (plain)
     * scalars, delegating construction to {@link FallbackConstructor} to avoid recursion.
     * <p>
     * This ensures custom-tagged plain values (e.g., {@code !default 42} or {@code !freeze true})
     * retain their native Java primitive types rather than falling back to strings.
     *
     * @param scalarNode the scalar node to inspect
     * @return the parsed primitive object, or {@code null} if the scalar is quoted or not a primitive
     */
    protected @Nullable Object constructImplicitScalar(ScalarNode scalarNode) {
        if (!scalarNode.isPlain()) {
            return null;
        }

        String value = scalarNode.getValue();
        ScalarResolver scalarResolver = settings.getSchema().getScalarResolver();
        Tag implicitTag = scalarResolver.resolve(value, true);

        if (Tag.NULL.equals(implicitTag)) {
            return Tag.NULL;
        }

        if (Tag.INT.equals(implicitTag) || Tag.FLOAT.equals(implicitTag) || Tag.BOOL.equals(implicitTag)) {
            ScalarNode typedNode = new ScalarNode(implicitTag, true, value, scalarNode.getScalarStyle(),
                    scalarNode.getStartMark(), scalarNode.getEndMark());
            return fallbackConstructor.constructObject(typedNode);
        }

        return null;
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
        if (LITERAL_TAG.equals(tag)) {
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

    /**
     * Isolated constructor using a clean {@link CoreSchema} to parse standard primitive
     * scalar types without triggering custom tag overrides or infinite recursion.
     * <p>
     * Must use its own fresh {@link LoadSettings} rather than the outer settings to avoid
     * re-entering custom schema resolution (e.g. {@link org.openhab.io.yamlcomposer.internal.ModelResolver}).
     */
    private static class FallbackConstructor extends StandardConstructor {
        public FallbackConstructor() {
            super(LoadSettings.builder().setSchema(new CoreSchema()).build());
        }

        @Override
        public @Nullable Object constructObject(@Nullable Node node) {
            return super.constructObject(node);
        }
    }
}

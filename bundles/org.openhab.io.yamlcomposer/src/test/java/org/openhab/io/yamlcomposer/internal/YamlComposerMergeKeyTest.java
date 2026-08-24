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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerMergeKeyTest} contains tests for the merge key (<<) functionality in the {@link YamlComposer}
 * class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Merge Key (<<) Specifications")
class YamlComposerMergeKeyTest extends AbstractYamlComposerTest {

    /**
     * Ensures our custom {@code RecursiveTransformer} implementation adheres to the
     * official YAML 1.1 Merge Key Language-Independent Type specification.
     * <p>
     * Because we manually handle {@code <<} to support dynamic tags like {@code !if}
     * and {@code !include}, we must strictly replicate the "First-Key-Wins"
     * precedence rules to avoid breaking standard YAML behavior.
     * * @see <a href="https://yaml.org/type/merge.html">YAML 1.1 Merge Key Spec</a>
     */
    @Nested
    @DisplayName("Standard YAML 1.1 Compliance")
    class StandardCompliance {

        @Test
        @DisplayName("Spec: Local keys override merged keys")
        void localKeyPrecedence() throws IOException {
            String yaml = """
                    base: &base
                      status: "original"
                      type: "base"
                    target:
                      status: "overridden"
                      <<: *base
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            // Local 'status' exists first, so 'original' is ignored.
            assertThat(getNestedValue(data, "target", "status"), equalTo("overridden"));
            assertThat(getNestedValue(data, "target", "type"), equalTo("base"));
        }

        @Test
        @DisplayName("Spec: Sequence Merge - Earlier mappings override later ones")
        void sequenceMergePrecedence() throws IOException {
            String yaml = """
                    m1: &m1 { val: "first" }
                    m2: &m2 { val: "second" }
                    target:
                      <<: [*m1, *m2]
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            // m1 is processed first; val is set to "first".
            // When m2 is processed, val already exists, so "second" is skipped.
            assertThat(getNestedValue(data, "target", "val"), equalTo("first"));
        }

        @Test
        @DisplayName("Spec: Multiple merge keys - First merge key wins")
        void multipleMergeKeysFirstWins() throws IOException {
            String yaml = """
                    m1: &m1 { common: "from_m1", unique_a: 1 }
                    m2: &m2 { common: "from_m2", unique_b: 2 }
                    target:
                      <<: *m1
                      <<: *m2
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            // Following the "unless the key already exists" rule:
            // 1. unique_a and common ("from_m1") are inserted.
            // 2. common already exists, so "from_m2" is ignored.
            // 3. unique_b is inserted.
            assertThat(getNestedValue(data, "target", "common"), equalTo("from_m1"));
            assertThat(getNestedValue(data, "target", "unique_a"), equalTo(1));
            assertThat(getNestedValue(data, "target", "unique_b"), equalTo(2));
        }

        @Test
        @DisplayName("Spec: Complex precedence (Local > Merge1 > Merge2)")
        void complexPrecedence() throws IOException {
            String yaml = """
                    m1: &m1 { a: "m1", b: "m1", c: "m1" }
                    m2: &m2 { a: "m2", b: "m2", d: "m2" }
                    target:
                      a: "local"
                      <<: *m1
                      <<: *m2
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertAll(() -> assertThat("Local wins", getNestedValue(data, "target", "a"), equalTo("local")),
                    () -> assertThat("First merge wins", getNestedValue(data, "target", "b"), equalTo("m1")),
                    () -> assertThat("From first merge", getNestedValue(data, "target", "c"), equalTo("m1")),
                    () -> assertThat("From second merge", getNestedValue(data, "target", "d"), equalTo("m2")));
        }

        @Test
        @DisplayName("Spec: Deep merge is NOT supported")
        void mergeIsNotRecursive() throws IOException {
            String yaml = """
                    default_meta: &default_meta
                      tags: { level: "info", persistent: true }
                    target:
                      <<: *default_meta
                      tags: { level: "debug" }
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            // The local 'tags' key blocks the entire merged 'tags' map.
            assertThat(getNestedValue(data, "target", "tags", "level"), equalTo("debug"));
            assertThat(getNestedValue(data, "target", "tags", "persistent"), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Alias Support")
    class AliasSupport {

        @Test
        @DisplayName("Merge with simple alias mapping")
        void mergeWithAliasMapping() throws IOException {
            String yaml = """
                    base: &base
                      x: 1
                      y: 2
                      a: [foo, bar]
                    target:
                      z: 3
                      <<: *base
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat(getNestedValue(data, "target", "x"), equalTo(1));
            assertThat(getNestedValue(data, "target", "y"), equalTo(2));
            assertThat(getNestedValue(data, "target", "a"), equalTo(List.of("foo", "bar")));
            assertThat(getNestedValue(data, "target", "z"), equalTo(3));
        }

        @Test
        @DisplayName("Merge with alias sequence of mappings")
        void mergeWithAliasSequenceOfMappings() throws IOException {
            String yaml = """
                    m1: &m1 { a: 10 }
                    m2: &m2 { b: 20 }
                    target:
                      <<: [*m1, *m2]
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat(getNestedValue(data, "target", "a"), equalTo(10));
            assertThat(getNestedValue(data, "target", "b"), equalTo(20));
        }

        @Test
        @DisplayName("Merge with alias referencing another alias")
        void mergeWithAliasReferencingAlias() throws IOException {
            String yaml = """
                    base1: &base1 { foo: 1 }
                    base2: &base2 { <<: *base1 }
                    target:
                      <<: *base2
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat(getNestedValue(data, "target", "foo"), equalTo(1));
        }
    }

    @Nested
    @DisplayName("Conditional Integration (!if)")
    class ConditionalIntegration {

        @Test
        @DisplayName("Merge with !if resolving to a mapping (truthy)")
        void mergeWithTruthyIf() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      is_prod: true
                      prod_settings: { timeout: 30, retry: 3 }
                    target:
                      existing: "value"
                      <<: !if
                        if: ${is_prod}
                        then: ${prod_settings}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "target", "timeout"), equalTo(30));
            assertThat(getNestedValue(data, "target", "existing"), equalTo("value"));
        }

        @Test
        @DisplayName("Merge with !if resolving to alternative mapping (else branch)")
        void mergeWithFalsyIfWithElseBranch() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      env: "dev"
                    target:
                      <<: !if
                        if: "${env == 'prod'}"
                        then: { color: "red" }
                        else: { color: "green" }
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "target", "color"), equalTo("green"));
        }

        @Test
        @DisplayName("Merge with !if with falsy condition without else results in a no-op")
        @SuppressWarnings("null")
        void mergeWithFalsyIfWithoutElse() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      is_prod: false
                    target:
                      existing: "preserved"
                      <<: !if
                        if: ${is_prod}
                        then: { secret: "password" }
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) data.get("target");

            assertThat(target, equalTo(Map.of("existing", "preserved")));

            // No warnings should be emitted for this valid use case
            assertThat(logSession.getTrackedWarnings(), is(empty()));
        }

        @Test
        @DisplayName("Merge with sequence form !if (multiple branches)")
        void mergeWithSequenceIf() throws IOException {
            Path main = writeFixture("main.yaml", """
                    target:
                      <<: !if
                        - if: false
                          then: { branch: 1 }
                        - elseif: true
                          then: { branch: 2 }
                        - else: { branch: 3 }
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "target", "branch"), equalTo(2));
        }
    }

    @Nested
    @DisplayName("Inclusion Strategy")
    class Inclusion {

        @Test
        @DisplayName("Merge with !include in scalar form")
        void mergeWithScalarInclude() throws IOException {
            writeFixture("inc.yaml", "foo: include1");
            Path main = writeFixture("main.yaml", """
                    simple:
                      <<: !include inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "foo"), equalTo("include1"));
        }

        @Test
        @DisplayName("Merge with !include in mapping form with variables")
        void mergeWithMappingInclude() throws IOException {
            writeFixture("inc.yaml", "greeting: 'Hello ${name}'");
            Path main = writeFixture("main.yaml", """
                    simple:
                      <<: !include
                        file: inc.yaml
                        vars:
                          name: "World"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "greeting"), equalTo("Hello World"));
        }

        @Test
        @DisplayName("Merge with !include using substitutions in filename and vars")
        void mergeWithIncludeSubstitutions() throws IOException {
            writeFixture("production.yaml", """
                    mode: prod
                    owner: ${owner}
                    """);
            Path main = writeFixture("main.yaml", """
                    variables:
                      env: production
                      user: gemini

                    target:
                      <<: !include
                        file: "${env}.yaml"
                        vars:
                          owner: "${user}"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "target", "mode"), equalTo("prod"));
            assertThat(getNestedValue(data, "target", "owner"), equalTo("gemini"));
        }

        @Test
        @DisplayName("Merge with !include in a map")
        void mergeWithIncludeInMap() throws IOException {
            writeFixture("inc.yaml", "foo: include1");
            Path main = writeFixture("main.yaml", """
                    simple:
                      <<:
                        bar: !include inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "bar", "foo"), equalTo("include1"));
        }

        @Test
        @DisplayName("Merge with !include non-existent file results in a no-op")
        void mergeWithNonExistentInclude() throws IOException {
            Path main = writeFixture("main.yaml", "simple: { <<: !include missing.yaml }");

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple"), equalTo(Map.of()));
            assertThat(logSession.getTrackedWarnings(), not(hasItem(containsString("Expected a mapping"))));
        }
    }

    @Nested
    @DisplayName("Substitution and Inheritance")
    class Substitution {

        @Test
        @DisplayName("Merge keys work with substitutions for dynamic mapping injection")
        void mergeWithSubstitution() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      map1:
                        foo: bar
                        baz: "${foo}"
                    simple:
                      <<: ${map1}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "foo"), equalTo("bar"));
            assertThat(getNestedValue(data, "simple", "baz"), nullValue());
        }

        @Test
        @DisplayName("Merge keys handle undefined variables as no-ops")
        void mergeWithUndefinedVariable() throws IOException {
            Path main = writeFixture("main.yaml", """
                    target:
                      existing_key: "preserved"
                      <<: ${undefined_var}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) Objects
                    .requireNonNull(getNestedValue(data, "target"));

            assertThat(target, hasEntry("existing_key", "preserved"));
            assertThat(target.size(), equalTo(1));

            assertThat(logSession.getTrackedWarnings(), not(hasItem(containsString("Expected a mapping"))));
        }

        @Test
        @DisplayName("Merge keys support lists of substitutions")
        void mergeWithSubstitutionsThatReturnsListOfMaps() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      map1: { foo: bar }
                      map2: { qux: quux }
                    array_merge:
                      <<: ${[map1, map2]}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "array_merge", "foo"), equalTo("bar"));
            assertThat(getNestedValue(data, "array_merge", "qux"), equalTo("quux"));
        }

        @Test
        @DisplayName("!literal on parent allows internal substitutions on merge keys to process")
        void mergeWithParentLiteral() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      map1: { foo: bar }
                    parent_literal: !literal
                      <<: !sub ${map1}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "parent_literal", "foo"), equalTo("bar"));
        }

        @Test
        @DisplayName("Merge keys work with substitution inside a map")
        void mergeWithSubstitutionInMap() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      map1:
                        foo: bar
                        baz: "${foo}"
                    simple:
                      <<:
                        qux: ${map1}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "qux", "foo"), equalTo("bar"));
            assertThat(getNestedValue(data, "simple", "qux", "baz"), nullValue());
        }

        @Test
        @DisplayName("Merge keys work with substitution inside a list")
        void mergeWithSubstitutionInList() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      map1:
                        foo: bar
                        baz: "${foo}"
                    simple:
                      <<:
                        qux:
                          - ${map1}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "qux"), equalTo(List.of(Map.of("foo", "bar"))));
        }

        @Test
        @DisplayName("Conditional logic in substitutions without else results in a no-op merge")
        void mergeWithConditionalWithoutElse() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      empty_map: {}
                    conditionally_empty:
                      <<: ${empty_map if false}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "conditionally_empty"), equalTo(Map.of()));

            assertThat(logSession.getTrackedWarnings(), is(empty()));
        }
    }

    @Nested
    @DisplayName("Templates and Collections")
    class TemplatesAndCollections {

        @Test
        @DisplayName("Merge keys work with !insert templates (scalar form)")
        void mergeWithScalarTemplates() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      base: { foo: bar }
                    simple:
                      <<: !insert base
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "foo"), equalTo("bar"));
        }

        @Test
        @DisplayName("Merge keys work with !insert templates (mapping form)")
        void mergeWithMappingTemplate() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      device:
                        type: ${dev_type}
                        vendor: "openHAB"
                    simple:
                      <<: !insert
                        template: device
                        vars:
                          dev_type: Light
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "simple", "type"), equalTo("Light"));
            assertThat(getNestedValue(data, "simple", "vendor"), equalTo("openHAB"));
        }

        @Test
        @DisplayName("Merge keys with !include at template top-level (placeholder-valued merge source)")
        void mergeKeysWithIncludeAtTemplateTopLevel() throws IOException {
            writeFixture("base.yaml", """
                    shared_attr: from_base
                    """);

            Path main = writeFixture("main.yaml", """
                    templates:
                      merged_template:
                        <<: !include base.yaml
                        own_attr: from_template
                    device: !insert merged_template
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            // merged_template should contain both attributes.
            assertThat(getNestedValue(data, "device", "shared_attr"), equalTo("from_base"));
            assertThat(getNestedValue(data, "device", "own_attr"), equalTo("from_template"));
        }

        @Test
        @DisplayName("Merge keys with !insert at template top-level (placeholder-valued merge source)")
        void mergeKeysWithInsertAtTemplateTopLevel() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      base_data:
                        a: 1
                        b: 2
                      extended_template:
                        <<: !insert base_data
                        c: 3
                    result: !insert extended_template
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            // Control case: merge key inside a template value with !insert source.
            assertThat(getNestedValue(data, "result", "a"), equalTo(1));
            assertThat(getNestedValue(data, "result", "b"), equalTo(2));
            assertThat(getNestedValue(data, "result", "c"), equalTo(3));
        }

        @Test
        @DisplayName("Merge keys at templates TOP-LEVEL with !include placeholder (demonstrates the bug)")
        void mergeKeysAtTemplatesMapLevel() throws IOException {
            writeFixture("base_templates.yaml", """
                    base_template:
                      value: from_base
                    """);

            Path main = writeFixture("main.yaml", """
                    templates:
                      <<: !include base_templates.yaml
                    merged_template: !insert base_template
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "merged_template", "value"), equalTo("from_base"));
        }
    }

    @Test
    @DisplayName("Merge keys in !include vars are visible to included file")
    void mergeKeysInIncludeVars() throws IOException {
        writeFixture("include.inc.yaml", "foo: ${foo}");

        Path main = writeFixture("main.yaml", """
                packages:
                  foo_package: !include
                    file: include.inc.yaml
                    vars:
                      <<:
                        foo: bar
                """);

        Map<Object, @Nullable Object> data = loadFixture(main);

        assertThat(getNestedValue(data, "foo"), equalTo("bar"));
    }

    @Nested
    @DisplayName("Type Fidelity")
    class TypeFidelity {

        @Test
        @DisplayName("Merge keys retain primitive types (int, boolean) from !include")
        void mergeRetainsTypesFromInclude() throws IOException {
            // inc.yaml contains actual boolean and int types
            writeFixture("inc.yaml", """
                    active: true
                    timeout: 30
                    """);
            Path main = writeFixture("main.yaml", """
                    device:
                      <<: !include inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            Object active = getNestedValue(data, "device", "active");
            Object timeout = getNestedValue(data, "device", "timeout");

            // Verify values AND types
            assertThat(active, allOf(equalTo(true), instanceOf(Boolean.class)));
            assertThat(timeout, allOf(equalTo(30), instanceOf(Integer.class)));
        }

        @Test
        @DisplayName("Merge keys retain types from substitution map injection")
        void mergeRetainsTypesFromSub() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      settings:
                        enabled: true
                        port: 8080
                    target:
                      <<: ${settings}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            Object enabled = getNestedValue(data, "target", "enabled");
            Object port = getNestedValue(data, "target", "port");

            assertThat(enabled, instanceOf(Boolean.class));
            assertThat(port, instanceOf(Integer.class));
        }

        @Test
        @DisplayName("Merge keys retain types when using !insert with variable overrides")
        void mergeRetainsTypesFromInsert() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      base:
                        value: ${in_val}
                    target:
                      <<: !insert
                        template: base
                        vars:
                          in_val: 100
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            Object value = getNestedValue(data, "target", "value");

            // If bug exists, value would be "100" (String) instead of 100 (Integer)
            assertThat(value, allOf(equalTo(100), instanceOf(Integer.class)));
        }

        @Test
        @DisplayName("Merge keys retain nested List and Map structures")
        void mergeRetainsComplexStructures() throws IOException {
            writeFixture("inc.yaml", """
                    tags: [a, b, c]
                    config: { level: 1 }
                    """);
            Path main = writeFixture("main.yaml", """
                    target:
                      <<: !include inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "target", "tags"), instanceOf(List.class));
            assertThat(getNestedValue(data, "target", "config"), instanceOf(Map.class));
            assertThat(getNestedValue(data, "target", "config", "level"), instanceOf(Integer.class));
        }
    }
}

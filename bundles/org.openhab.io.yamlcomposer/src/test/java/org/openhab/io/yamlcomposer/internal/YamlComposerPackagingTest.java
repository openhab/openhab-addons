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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerPackagingTest} contains tests for the {@code packages} functionality in the
 * {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Packaging Specification")
class YamlComposerPackagingTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Integration Styles")
    class IntegrationStyles {

        @Test
        @DisplayName("Packages can be defined using external include files")
        void packageInclusionWorks() throws IOException {
            // The Blueprint
            writeFixture("package.inc.yaml", """
                    things:
                      ${name}:
                        label: ${thing_label}
                    items:
                      ${name}:
                        label: ${item_label}
                    """);

            // The Main Composition
            Path main = writeFixture("main.yaml", """
                    packages:
                      basic1: !include
                        file: package.inc.yaml
                        vars:
                          name: basic1
                          thing_label: "B1 Thing"
                          item_label: "B1 Item"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "things", "basic1", "label"), equalTo("B1 Thing"));
            assertThat(getNestedValue(data, "items", "basic1", "label"), equalTo("B1 Item"));
        }

        @Test
        @DisplayName("Packages can be defined using local templates")
        void packageTemplateWorks() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      pkg_tpl:
                        things:
                          ${name}:
                            label: "Template Label"

                    packages:
                      basic1: !insert
                        template: pkg_tpl
                        vars:
                          name: "from_tpl"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "things", "from_tpl", "label"), equalTo("Template Label"));
        }

        @Test
        @DisplayName("Non-package content remains after merge")
        void nonPackageContentRemainsAfterMerge() throws IOException {
            Path main = writeFixture("main.yaml", """
                    packages:
                      pkg1: !include { file: package.inc.yaml, vars: { name: "p1" } }
                    things:
                      static_thing:
                        label: "keep_me"
                    """);
            writeFixture("package.inc.yaml", "things: { '${name}': { label: 'pkg' } }");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "things", "static_thing", "label"), equalTo("keep_me"));
            assertThat(getNestedValue(data, "things", "p1", "label"), equalTo("pkg"));
        }
    }

    @Nested
    @DisplayName("Package ID Injection")
    class PackageID {

        @Test
        @DisplayName("Injects package ID into mapping-form include")
        void packageIdInjectedIntoMappingInclude() throws IOException {
            writeFixture("package.yaml", """
                    result:
                      ${package_id}: 'active'
                    """);
            Path main = writeFixture("main.yaml", """
                    packages:
                      test_id: !include
                        file: package.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
        }

        @Test
        @DisplayName("Injects package ID into scalar-form include")
        void packageIdInjectedIntoScalarInclude() throws IOException {
            writeFixture("package.yaml", """
                    result:
                      ${package_id}: 'active'
                    """);
            Path main = writeFixture("main.yaml", """
                    packages:
                      test_id: !include package.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
        }

        @Test
        @DisplayName("Injects package ID into mapping-form insert using named template")
        void packageIdInjectedIntoMappingInsert() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      test_template:
                        result:
                          ${package_id}: 'active'

                    packages:
                      test_id: !insert
                        template: test_template
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
        }

        @Test
        @DisplayName("Injects package ID into scalar-form insert using named template")
        void packageIdInjectedIntoScalarInsert() throws IOException {
            Path main = writeFixture("main.yaml", """
                    templates:
                      test_template:
                        result:
                          ${package_id}: 'active'

                    packages:
                      test_id: !insert test_template
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
        }

        @Test
        @DisplayName("Manual package ID variable takes precedence over automatic injection")
        void packageIdIsOverridable() throws IOException {
            writeFixture("package.yaml", "value: ${package_id}");

            Path main = writeFixture("main.yaml", """
                    packages:
                      default_id: !include
                        file: package.yaml
                        vars:
                          package_id: "custom_id"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "value"), equalTo("custom_id"));
        }
    }

    @Nested
    @DisplayName("Merge Strategies")
    class MergeStrategies {

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Deeply merges maps: overwrites shared scalars, appends lists, and preserves unique keys from both")
        void defaultDeepMergeLogic() throws IOException {
            writeFixture("pkg.yaml", """
                    things:
                      thing:
                        scalar: package
                        config:
                          scalar1: package
                          scalar2: package
                          map1:
                            scalar1: package
                            scalar2: package
                          list1:
                            - package
                    """);

            Path main = writeFixture("main.yaml", """
                    packages:
                      p1: !include pkg.yaml
                    things:
                      thing:
                        main_only_scalar: "preserved"
                        config:
                          scalar2: main
                          map1:
                            scalar2: main
                            scalar3: "main_only"
                          map2:
                            new_key: "main_only"
                          list1:
                            - main
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);
            Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                    "thing");

            // 1. Verify Overwrites (Main wins)
            assertThat(getNestedValue(thing, "config", "scalar2"), equalTo("main"));
            assertThat(getNestedValue(thing, "config", "map1", "scalar2"), equalTo("main"));

            // 2. Verify Package-only values are preserved
            assertThat(getNestedValue(thing, "scalar"), equalTo("package"));
            assertThat(getNestedValue(thing, "config", "scalar1"), equalTo("package"));
            assertThat(getNestedValue(thing, "config", "map1", "scalar1"), equalTo("package"));

            // 3. Verify Main-only values are preserved (The Union)
            assertThat(getNestedValue(thing, "main_only_scalar"), equalTo("preserved"));
            assertThat(getNestedValue(thing, "config", "map1", "scalar3"), equalTo("main_only"));
            assertThat(getNestedValue(thing, "config", "map2", "new_key"), equalTo("main_only"));

            // 4. Verify List behavior (Append)
            assertThat(getNestedValue(thing, "config", "list1"), equalTo(List.of("package", "main")));
        }

        @Nested
        @DisplayName("PackageMergeHelpers")
        class PackageMergeHelpers {

            @Test
            @DisplayName("Nested package include can override items using !replace and !remove")
            void replaceAndRemoveInsideIncludedPackageSourceWork() throws IOException {
                writeFixture("nested-items.inc.yaml", """
                        things:
                          thing:
                            label: from_nested_package
                            config:
                              tags:
                                - from_nested
                              details:
                                source: nested
                                remove_me: true
                        items:
                          target_item:
                            label: base_label
                            tags:
                              - from_nested
                            metadata:
                              stateDescription:
                                value: from_nested
                              category:
                                value: from_nested
                          untouched_item:
                            label: untouched_from_nested
                        """);

                writeFixture("pkg-with-overrides.inc.yaml", """
                        packages:
                          nested: !include nested-items.inc.yaml
                        items:
                          target_item:
                            tags: !replace
                              - from_outer
                            metadata:
                              stateDescription: !remove
                              category: !replace
                                value: from_outer
                                config:
                                  origin: nested_package_override
                        """);

                Path main = writeFixture("main.yaml", """
                        packages:
                          p1: !include pkg-with-overrides.inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "items", "target_item", "tags"), equalTo(List.of("from_outer")));
                assertThat((Map<?, ?>) getNestedValue(data, "items", "target_item", "metadata"),
                        not(hasKey("stateDescription")));
                assertThat(getNestedValue(data, "items", "target_item", "metadata", "category", "value"),
                        equalTo("from_outer"));
                assertThat(getNestedValue(data, "items", "target_item", "metadata", "category", "config", "origin"),
                        equalTo("nested_package_override"));
                assertThat(getNestedValue(data, "items", "untouched_item", "label"), equalTo("untouched_from_nested"));
            }

            @Test
            @DisplayName("!remove directive deletes specific keys from the merge result")
            void removeDirectiveWorks() throws IOException {
                writeFixture("pkg.yaml", """
                        things:
                          thing:
                            label: to_remove
                            scalar: package
                            config:
                              scalar1: package
                              map1: { key: val }
                          whole_thing_removed: { key: val }
                          thing_to_keep: { status: 'safe' }
                        """);

                Path main = writeFixture("main.yaml", """
                        packages:
                          p1: !include pkg.yaml
                        things:
                          thing:
                            label: !remove
                            config:
                              map1: !remove
                          whole_thing_removed: !remove
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                        "thing");

                // 1. Verify Removals
                assertThat(thing, not(hasKey("label")));
                assertThat((Map<?, ?>) getNestedValue(thing, "config"), not(hasKey("map1")));
                assertThat((Map<?, ?>) getNestedValue(data, "things"), not(hasKey("whole_thing_removed")));

                // 2. Verify Survival of Neighbors
                assertThat(getNestedValue(thing, "scalar"), equalTo("package"));
                assertThat(getNestedValue(thing, "config", "scalar1"), equalTo("package"));
                assertThat(getNestedValue(data, "things", "thing_to_keep", "status"), equalTo("safe"));
            }

            @Test
            @DisplayName("!replace directive overwrites complex nodes instead of merging them")
            void replaceDirectiveWorks() throws IOException {
                writeFixture("pkg.yaml", """
                        things:
                          thing:
                            map1:
                              scalar1: package
                              scalar2: package
                            list1:
                              - package
                            scalar_to_keep: package
                        """);

                Path main = writeFixture("main.yaml", """
                        packages:
                          p1: !include pkg.yaml
                        things:
                          thing:
                            map1: !replace
                              scalar1: main
                            list1: !replace
                              - main
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                        "thing");

                // !replace results in ONLY main's data
                assertThat(getNestedValue(thing, "map1"), equalTo(Map.of("scalar1", "main")));
                assertThat(getNestedValue(thing, "list1"), equalTo(List.of("main")));
                assertThat(getNestedValue(thing, "scalar_to_keep"), equalTo("package"));
            }

            @Test
            @DisplayName("!replace and !remove inside !insert templates are applied only after package merge")
            void insertTemplatePackageOverridesAreDeferredUntilOverridePhase() throws IOException {
                writeFixture("pkg.yaml", """
                        things:
                          thing:
                            map1:
                              from_package: true
                            remove_me: from_package
                        """);

                Path main = writeFixture("main.yaml", """
                        templates:
                          overrides_tpl:
                            thing:
                              map1: !replace
                                from_main: true
                              remove_me: !remove

                        packages:
                          p1: !include pkg.yaml

                        things: !insert overrides_tpl
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "things", "thing", "map1"), equalTo(Map.of("from_main", true)));
                assertThat((Map<?, ?>) getNestedValue(data, "things", "thing"), not(hasKey("remove_me")));
            }

            @Nested
            @DisplayName("Defensive Fallbacks (Outside Package Context)")
            class DefensiveFallbacks {

                @Test
                @DisplayName("!remove in Map: Should self-delete even without a merge")
                void removeMapSafety() throws IOException {
                    Path main = writeFixture("main_only.yaml", """
                            config:
                              active: true
                              junk: !remove
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);

                    assertThat(getNestedValue(data, "config"), is(instanceOf(Map.class)));
                    assertThat((Map<?, ?>) getNestedValue(data, "config"), not(hasKey("junk")));
                    assertThat(getNestedValue(data, "config", "active"), is(true));
                }

                @Test
                @DisplayName("!remove in List: Should purge the entry even without a merge")
                void removeListSafety() throws IOException {
                    Path main = writeFixture("list_only.yaml", """
                            items:
                              - "A"
                              - !remove
                              - "B"
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);
                    List<?> items = (List<?>) data.get("items");

                    assertThat(items, contains("A", "B"));
                    assertThat(items, hasSize(2));
                }

                @Test
                @DisplayName("!replace: Should unwrap to raw content to avoid 'Opaque Tag' artifacts")
                void replaceSafety() throws IOException {
                    Path main = writeFixture("replace_only.yaml", """
                            settings:
                              mode: !replace "standalone"
                              options: !replace
                                speed: fast
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);

                    assertThat(getNestedValue(data, "settings", "mode"), equalTo("standalone"));
                    assertThat(getNestedValue(data, "settings", "options", "speed"), equalTo("fast"));
                }

                @Test
                @DisplayName("Replace with insert works")
                void replaceWithInsert() throws IOException {
                    Path main = writeFixture("replace_only.yaml", """
                            templates:
                              test: bar

                            settings:
                              mode: !replace
                                foo: !insert test
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);

                    assertThat(getNestedValue(data, "settings", "mode", "foo"), equalTo("bar"));
                }

                @Test
                @DisplayName("Replace with include works")
                void replaceWithInclude() throws IOException {
                    writeFixture("test.yaml", "bar");
                    Path main = writeFixture("replace_only.yaml", """
                            templates:
                              test: bar

                            settings:
                              mode: !replace
                                foo: !include test.yaml
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);

                    assertThat(getNestedValue(data, "settings", "mode", "foo"), equalTo("bar"));
                }
            }
        }
    }

    @Nested
    @DisplayName("Conflict Resolution")
    class Conflicts {

        @Test
        @DisplayName("Earlier packages take precedence over later packages in the sequence")
        void earlierPackageWinsConflict() throws IOException {
            // Package A: The first one processed
            writeFixture("pkg_a.inc.yaml", """
                    things:
                      shared_id:
                        status: "from_a"
                        only_in_a: "value_a"
                    """);

            // Package B: The second one processed
            writeFixture("pkg_b.inc.yaml", """
                    things:
                      shared_id:
                        status: "from_b"
                        only_in_b: "value_b"
                    """);

            Path main = writeFixture("main.yaml", """
                    packages:
                      instance_1: !include pkg_a.inc.yaml
                      instance_2: !include pkg_b.inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            // 1. Precedence Check: 'from_a' should remain because it was merged into Main
            // first
            assertThat(getNestedValue(data, "things", "shared_id", "status"), equalTo("from_a"));

            // 2. Union Check: Maps are still merged, so unique keys from B are still added
            assertThat(getNestedValue(data, "things", "shared_id", "only_in_a"), equalTo("value_a"));
            assertThat(getNestedValue(data, "things", "shared_id", "only_in_b"), equalTo("value_b"));
        }
    }
}

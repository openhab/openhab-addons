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
package org.openhab.automation.jrubyscripting.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.openhab.core.OpenHAB;
import org.openhab.core.service.WatchService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

/**
 * Tests console dependency management overrides.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class JRubyScriptEngineFactoryTest {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void skipsBundlerAndRequiresOnlyForRequestedEngine(boolean gemfileExists, @TempDir Path directory)
            throws Exception {
        Path gemfile = directory.resolve("Gemfile");
        Files.writeString(directory.resolve("configured.rb"), "$configured_require_loaded = true\n");
        Bundle bundle = mock(Bundle.class);
        when(bundle.getVersion()).thenReturn(new Version(5, 3, 0));
        try (MockedStatic<FrameworkUtil> framework = mockStatic(FrameworkUtil.class)) {
            framework.when(() -> FrameworkUtil.getBundle(OpenHAB.class)).thenReturn(bundle);
            JRubyScriptEngineFactory factory = new JRubyScriptEngineFactory(mock(WatchService.class),
                    Map.of("bundle_gemfile", gemfile.toString(), "gem_home", directory.resolve("gems").toString(),
                            "rubylib", directory.toString(), "gems", "", "require", "configured", "dependency_tracking",
                            false));
            try {
                if (gemfileExists) {
                    // Any attempt to evaluate this Gemfile fails, including automatic installation.
                    Files.writeString(gemfile, "raise 'Bundler must not run'\n");
                }
                try (JRubyEngineWrapper engine = (JRubyEngineWrapper) factory.withoutBundle(() -> {
                    var result = factory.createScriptEngine("rb");
                    assertNotNull(result);
                    factory.scopeValues(result, Map.of("ir", "preset"));
                    return result;
                })) {
                    assertNotNull(engine);
                    assertEquals(true, engine.eval("ENV['BUNDLE_GEMFILE'].nil?"));
                    assertEquals(false, engine.eval("$LOADED_FEATURES.any? { |f| f.include?('/bundler') }"));
                    assertEquals(true, engine.eval("$configured_require_loaded.nil?"));
                    assertEquals("preset", engine.eval("$ir"));
                    assertEquals(directory.resolve("gems").toString(), engine.eval("ENV['GEM_HOME']"));
                }
                assertThrows(IllegalStateException.class, () -> factory.withoutBundle(() -> {
                    throw new IllegalStateException("Engine creation failed");
                }));
                try (JRubyEngineWrapper engine = (JRubyEngineWrapper) factory.createScriptEngine("rb")) {
                    assertNotNull(engine);
                    assertEquals(gemfileExists ? gemfile.toString() : null, engine.eval("ENV['BUNDLE_GEMFILE']"));
                    assertEquals(true, engine.eval("defined?(Bundler) != nil"));
                    if (!gemfileExists) {
                        factory.scopeValues(engine, Map.of("ir", "preset"));
                        assertEquals(true, engine.eval("$configured_require_loaded"));
                    }
                }
            } finally {
                factory.deactivate();
            }
        }
    }
}

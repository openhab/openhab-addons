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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jruby.embed.jsr223.JRubyEngine;
import org.jruby.embed.jsr223.JRubyEngineFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.openhab.core.OpenHAB;

/**
 * Tests Bundler command selection against real lockfiles.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class JRubyScriptEngineConfigurationTest {

    @ParameterizedTest
    @CsvSource({ "false, missing, 1.0.0, install", "false, rake, 1.0.0, install",
            "false, openhab-scripting, 1.0.0, install", "false, my-openhab, 1.0.0, install",
            "false, openhab, 1.0.0, update --conservative openhab", "false, openhab, 5.3.0, install",
            "false, openhab, 5.3.0.0, update --conservative openhab", "true, missing, 1.0.0, update --all",
            "true, openhab, 1.0.0, update --all", "true, openhab, 5.3.0, update --all" })
    void selectsBundlerCommand(boolean update, String gem, String version, String expected, @TempDir Path directory)
            throws Exception {
        Path gemfile = directory.resolve("Gemfile");
        Files.writeString(gemfile, "source 'https://rubygems.org'\n");
        if (!"missing".equals(gem)) {
            Files.writeString(directory.resolve("Gemfile.lock"), """
                    GEM
                      remote: https://rubygems.org/
                      specs:
                        %s (%s)

                    PLATFORMS
                      java

                    DEPENDENCIES
                      %s
                    """.formatted(gem, version, gem));
        }
        try (MockedStatic<OpenHAB> openhab = mockStatic(OpenHAB.class, CALLS_REAL_METHODS);
                JRubyEngine engine = (JRubyEngine) new JRubyEngineFactory().getScriptEngine()) {
            openhab.when(OpenHAB::getVersion).thenReturn("5.3.0");
            engine.put("gemfile_path", gemfile.toString());
            engine.eval("""
                    require "jruby"
                    JRuby.runtime.instance_config.update_native_env_enabled = false
                    ENV["BUNDLE_GEMFILE"] = gemfile_path
                    require "bundler"
                    require "bundler/cli"
                    class << Bundler::CLI
                      def start(args)
                        $bundler_args = args.join(" ")
                      end
                    end
                    """);
            new JRubyScriptEngineConfiguration().bundlerInit(engine, update);
            assertEquals(expected, engine.eval("$bundler_args"));
        }
    }
}

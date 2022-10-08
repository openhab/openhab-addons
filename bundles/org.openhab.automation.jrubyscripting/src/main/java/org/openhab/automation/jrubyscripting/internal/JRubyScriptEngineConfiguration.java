/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes JRuby Configuration Parameters.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Jimmy Tanagra - Add $LOAD_PATH, require injection
 */
@NonNullByDefault
public class JRubyScriptEngineConfiguration {

    private final Logger logger = LoggerFactory.getLogger(JRubyScriptEngineConfiguration.class);

    private static final Path DEFAULT_GEM_HOME = Paths.get(OpenHAB.getConfigFolder(), "scripts", "lib", "ruby",
            "gem_home");

    private static final Path DEFAULT_RUBYLIB = Paths.get(OpenHAB.getConfigFolder(), "automation", "lib", "ruby");

    private static final String GEM_HOME = "gem_home";
    private static final String RUBYLIB = "rubylib";
    private static final String GEMS = "gems";
    private static final String REQUIRE = "require";
    private static final String CHECK_UPDATE = "check_update";

    // Map of configuration parameters
    private static final Map<String, OptionalConfigurationElement> CONFIGURATION_PARAMETERS = Map.ofEntries(
            Map.entry("local_context",
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.SYSTEM_PROPERTY)
                            .mappedTo("org.jruby.embed.localcontext.scope").defaultValue("singlethread").build()),

            Map.entry("local_variable",
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.SYSTEM_PROPERTY)
                            .mappedTo("org.jruby.embed.localvariable.behavior").defaultValue("transient").build()),

            Map.entry(GEM_HOME,
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT)
                            .mappedTo("GEM_HOME").defaultValue(DEFAULT_GEM_HOME.toString()).build()),

            Map.entry(RUBYLIB,
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT)
                            .mappedTo("RUBYLIB").defaultValue(DEFAULT_RUBYLIB.toString()).build()),

            Map.entry(GEMS, new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.GEM).build()),

            Map.entry(REQUIRE,
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.REQUIRE).build()),

            Map.entry(CHECK_UPDATE,
                    new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.CHECK_UPDATE).build()));

    private static final Map<OptionalConfigurationElement.Type, List<OptionalConfigurationElement>> CONFIGURATION_TYPE_MAP = CONFIGURATION_PARAMETERS
            .values().stream().collect(Collectors.groupingBy(v -> v.type));

    /**
     * Update configuration
     * 
     * @param config Configuration parameters to apply to ScriptEngine
     * @param factory ScriptEngineFactory to configure
     */
    void update(Map<String, Object> config, ScriptEngineFactory factory) {
        logger.trace("JRuby Script Engine Configuration: {}", config);
        config.forEach(this::processConfigValue);
        configureScriptEngine(factory);
    }

    /**
     * Apply configuration key/value to known configuration parameters
     * 
     * @param key Configuration key
     * @param value Configuration value
     */
    private void processConfigValue(String key, Object value) {
        OptionalConfigurationElement configurationElement = CONFIGURATION_PARAMETERS.get(key);
        if (configurationElement != null) {
            configurationElement.setValue(value.toString());
        } else {
            logger.debug("Ignoring unexpected configuration key: {}", key);
        }
    }

    /**
     * Configure the ScriptEngine
     * 
     * @param factory Script Engine to configure
     */
    void configureScriptEngine(ScriptEngineFactory factory) {
        configureSystemProperties();

        ScriptEngine engine = factory.getScriptEngine();
        configureRubyEnvironment(engine);
        configureGems(engine);
    }

    /**
     * Makes Gem home directory if it does not exist
     */
    private void ensureGemHomeExists() {
        OptionalConfigurationElement gemHomeConfigElement = CONFIGURATION_PARAMETERS.get(GEM_HOME);
        if (gemHomeConfigElement == null) {
            return;
        }
        Optional<String> gemHome = gemHomeConfigElement.getValue();
        if (gemHome.isPresent()) {
            File gemHomeDirectory = new File(gemHome.get());
            if (!gemHomeDirectory.exists()) {
                logger.debug("gem_home directory does not exist, creating");
                if (!gemHomeDirectory.mkdirs()) {
                    logger.warn("Error creating gem_home directory");
                }
            }
        } else {
            logger.debug("Gem install requested without gem_home specified, not ensuring gem_home path exists");
        }
    }

    /**
     * Install a gems in ScriptEngine
     * 
     * @param engine Engine to install gems
     */
    private synchronized void configureGems(ScriptEngine engine) {
        ensureGemHomeExists();

        OptionalConfigurationElement gemsConfigElement = CONFIGURATION_PARAMETERS.get(GEMS);
        if (gemsConfigElement == null || !gemsConfigElement.getValue().isPresent()) {
            return;
        }
        boolean checkUpdate = true;
        OptionalConfigurationElement updateConfigElement = CONFIGURATION_PARAMETERS.get(CHECK_UPDATE);
        if (updateConfigElement != null && updateConfigElement.getValue().isPresent()) {
            checkUpdate = updateConfigElement.getValue().get().equals("true");
        }

        String[] gems = gemsConfigElement.getValue().get().split(",");
        // Set update_native_env_enabled to false so that bundler doesn't leak
        // into other script engines
        String gemCommand = "require 'jruby'\nJRuby.runtime.instance_config.update_native_env_enabled = false\nrequire 'bundler/inline'\nrequire 'openssl'\n\ngemfile("
                + checkUpdate + ") do\n" + "  source 'https://rubygems.org/'\n";
        int validGems = 0;
        for (String gem : gems) {
            gem = gem.trim();
            String version = "";
            if (gem.contains("=")) {
                String[] gemParts = gem.split("=");
                gem = gemParts[0].trim();
                version = gemParts[1].trim();
            }

            if (gem.isEmpty()) {
                continue;
            }

            gemCommand += "  gem '" + gem + "'";
            if (!version.isEmpty()) {
                gemCommand += ", '" + version + "'";
            }
            gemCommand += ", require: false\n";
            validGems += 1;
        }
        if (validGems == 0) {
            return;
        }
        gemCommand += "end\n";

        try {
            logger.debug("Installing Gems");
            logger.trace("Gem install code:\n{}", gemCommand);
            engine.eval(gemCommand);
        } catch (ScriptException e) {
            logger.warn("Error installing Gems: {}", e.getMessage());
        }
    }

    /**
     * Execute ruby require statement in the ScriptEngine
     * 
     * @param engine Engine to insert the require statements
     */
    public void injectRequire(ScriptEngine engine) {
        OptionalConfigurationElement requireConfigElement = CONFIGURATION_PARAMETERS.get(REQUIRE);
        if (requireConfigElement == null || !requireConfigElement.getValue().isPresent()) {
            return;
        }

        Stream.of(requireConfigElement.getValue().get().split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty())
                .forEach(script -> {
                    final String requireStatement = String.format("require '%s'", script);
                    try {
                        logger.trace("Injecting require statement: {}", requireStatement);
                        engine.eval(requireStatement);
                    } catch (ScriptException e) {
                        logger.warn("Error evaluating statement {}: {}", requireStatement, e.getMessage());
                    }
                });
    }

    /**
     * Configure the optional elements of the Ruby Environment
     * 
     * @param engine Engine in which to configure environment
     */
    public ScriptEngine configureRubyEnvironment(ScriptEngine engine) {
        getConfigurationElements(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT).forEach(configElement -> {
            final String environmentSetting = String.format("ENV['%s']='%s'", configElement.mappedTo().get(),
                    configElement.getValue().get());
            try {
                logger.trace("Setting Ruby environment with code: {} ", environmentSetting);
                engine.eval(environmentSetting);
            } catch (ScriptException e) {
                logger.warn("Error setting ruby environment", e);
            }
        });

        configureRubyLib(engine);
        return engine;
    }

    /**
     * Split up and insert ENV['RUBYLIB'] into Ruby's $LOAD_PATH
     * This needs to be called after ENV['RUBYLIB'] has been set by configureRubyEnvironment
     * 
     * @param engine Engine in which to configure environment
     */
    private void configureRubyLib(ScriptEngine engine) {
        OptionalConfigurationElement rubyLibConfigElement = CONFIGURATION_PARAMETERS.get(RUBYLIB);
        if (rubyLibConfigElement == null) {
            return;
        }

        Optional<String> rubyLib = rubyLibConfigElement.getValue();
        if (rubyLib.isPresent() && !rubyLib.get().trim().isEmpty()) {
            final String code = "$LOAD_PATH.unshift *ENV['RUBYLIB']&.split(File::PATH_SEPARATOR)" + //
                    "&.reject(&:empty?)" + //
                    "&.reject { |path| $LOAD_PATH.include?(path) }"; //
            try {
                engine.eval(code);
            } catch (ScriptException exception) {
                logger.warn("Error setting $LOAD_PATH from RUBYLIB='{}': {}", rubyLib.get(), exception.getMessage());
            }
        }
    }

    /**
     * Configure system properties
     * 
     * @param optionalConfigurationElements Optional system properties to configure
     */
    private void configureSystemProperties() {
        getConfigurationElements(OptionalConfigurationElement.Type.SYSTEM_PROPERTY).forEach(configElement -> {
            String systemProperty = configElement.mappedTo().get();
            String propertyValue = configElement.getValue().get();
            logger.trace("Setting system property ({}) to ({})", systemProperty, propertyValue);
            System.setProperty(systemProperty, propertyValue);
        });
    }

    private Stream<OptionalConfigurationElement> getConfigurationElements(
            OptionalConfigurationElement.Type configurationType) {
        return CONFIGURATION_TYPE_MAP
                .getOrDefault(configurationType, Collections.<OptionalConfigurationElement> emptyList()).stream()
                .filter(element -> element.getValue().isPresent());
    }

    /**
     * Inner static companion class for configuration elements
     */
    private static class OptionalConfigurationElement {

        private final Optional<String> defaultValue;
        private final Optional<String> mappedTo;
        private final Type type;
        private Optional<String> value;

        private OptionalConfigurationElement(Type type, @Nullable String mappedTo, @Nullable String defaultValue) {
            this.type = type;
            this.defaultValue = Optional.ofNullable(defaultValue);
            this.mappedTo = Optional.ofNullable(mappedTo);
            value = Optional.empty();
        }

        private Optional<String> getValue() {
            return value.or(() -> defaultValue);
        }

        private void setValue(String value) {
            this.value = Optional.of(value);
        }

        private Optional<String> mappedTo() {
            return mappedTo;
        }

        private enum Type {
            SYSTEM_PROPERTY,
            RUBY_ENVIRONMENT,
            GEM,
            REQUIRE,
            CHECK_UPDATE,
        }

        private static class Builder {
            private final Type type;
            private @Nullable String defaultValue = null;
            private @Nullable String mappedTo = null;

            private Builder(Type type) {
                this.type = type;
            }

            private Builder mappedTo(String mappedTo) {
                this.mappedTo = mappedTo;
                return this;
            }

            private Builder defaultValue(String value) {
                this.defaultValue = value;
                return this;
            }

            private OptionalConfigurationElement build() {
                return new OptionalConfigurationElement(type, mappedTo, defaultValue);
            }
        }
    }
}

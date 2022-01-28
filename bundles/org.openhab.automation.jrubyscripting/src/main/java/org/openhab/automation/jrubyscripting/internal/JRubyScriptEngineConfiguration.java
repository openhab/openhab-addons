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
 */
@NonNullByDefault
public class JRubyScriptEngineConfiguration {

    private final Logger logger = LoggerFactory.getLogger(JRubyScriptEngineConfiguration.class);

    private static final Path DEFAULT_GEM_HOME = Paths.get(OpenHAB.getConfigFolder(), "scripts", "lib", "ruby",
            "gem_home");

    private static final Path DEFAULT_RUBYLIB = Paths.get(OpenHAB.getConfigFolder(), "automation", "lib", "ruby");

    private static final String GEM_HOME = "gem_home";
    private static final String RUBYLIB = "rubylib";

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

            Map.entry("gems", new OptionalConfigurationElement.Builder(OptionalConfigurationElement.Type.GEM).build()));

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
        configureSystemProperties(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.SYSTEM_PROPERTY,
                Collections.<OptionalConfigurationElement> emptyList()));

        ScriptEngine engine = factory.getScriptEngine();

        configureRubyEnvironment(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                Collections.<OptionalConfigurationElement> emptyList()), engine);

        configureGems(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.GEM,
                Collections.<OptionalConfigurationElement> emptyList()), engine);
    }

    /**
     * Makes Gem home directory if it does not exist
     */
    private void ensureGemHomeExists() {
        OptionalConfigurationElement gemHomeConfigElement = CONFIGURATION_PARAMETERS.get(GEM_HOME);
        if (gemHomeConfigElement != null) {
            Optional<String> gemHome = gemHomeConfigElement.getValue();
            if (gemHome.isPresent()) {
                File gemHomeDirectory = new File(gemHome.get());
                if (!gemHomeDirectory.exists()) {
                    logger.debug("gem_home directory does not exist, creating");
                    if (!gemHomeDirectory.mkdirs()) {
                        logger.debug("Error creating gem_home direcotry");
                    }
                }
            } else {
                logger.debug("Gem install requested without gem_home specified, not ensuring gem_home path exists");
            }
        }
    }

    /**
     * Install a gems in ScriptEngine
     * 
     * @param gemsDirectives List of gems to install
     * @param engine Engine to install gems
     */
    private synchronized void configureGems(List<OptionalConfigurationElement> gemDirectives, ScriptEngine engine) {
        for (OptionalConfigurationElement gemDirective : gemDirectives) {
            if (gemDirective.getValue().isPresent()) {
                ensureGemHomeExists();

                String[] gems = gemDirective.getValue().get().split(",");
                for (String gem : gems) {
                    gem = gem.trim();
                    String gemCommand;
                    if (gem.contains("=")) {
                        String[] gemParts = gem.split("=");
                        gem = gemParts[0];
                        String version = gemParts[1];
                        gemCommand = "Gem.install('" + gem + "',version='" + version + "')\n";
                    } else {
                        gemCommand = "Gem.install('" + gem + "')\n";
                    }

                    try {
                        logger.debug("Installing Gem: {} ", gem);
                        logger.trace("Gem install code:\n{}\n", gemCommand);
                        engine.eval(gemCommand);
                    } catch (Exception e) {
                        logger.error("Error installing Gem", e);
                    }
                }
            } else {
                logger.debug("Ruby gem property has no value");
            }
        }
    }

    /**
     * Configure the base Ruby Environment
     * 
     * @param engine Engine to configure
     */
    public ScriptEngine configureRubyEnvironment(ScriptEngine engine) {
        configureRubyEnvironment(CONFIGURATION_TYPE_MAP.getOrDefault(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                Collections.<OptionalConfigurationElement> emptyList()), engine);
        return engine;
    }

    /**
     * Configure the optional elements of the Ruby Environment
     * 
     * @param optionalConfigurationElements Optional elements to configure in the ruby environment
     * @param engine Engine in which to configure environment
     */
    private void configureRubyEnvironment(List<OptionalConfigurationElement> optionalConfigurationElements,
            ScriptEngine engine) {
        for (OptionalConfigurationElement configElement : optionalConfigurationElements) {
            String environmentProperty = configElement.mappedTo().get();
            if (configElement.getValue().isPresent()) {
                String environmentSetting = "ENV['" + environmentProperty + "']='" + configElement.getValue().get()
                        + "'";
                try {
                    logger.trace("Setting Ruby environment with code: {} ", environmentSetting);
                    engine.eval(environmentSetting);
                } catch (ScriptException e) {
                    logger.error("Error setting ruby environment", e);
                }
            } else {
                logger.debug("Ruby environment property ({}) has no value", environmentProperty);
            }
        }

        configureRubyLib(engine);
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
    private void configureSystemProperties(List<OptionalConfigurationElement> optionalConfigurationElements) {
        for (OptionalConfigurationElement configElement : optionalConfigurationElements) {
            String systemProperty = configElement.mappedTo().get();
            if (configElement.getValue().isPresent()) {
                String propertyValue = configElement.getValue().get();
                logger.trace("Setting system property ({}) to ({})", systemProperty, propertyValue);
                System.setProperty(systemProperty, propertyValue);
            } else {
                logger.warn("System property ({}) has no value", systemProperty);
            }
        }
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
            GEM
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

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jruby.runtime.Constants;
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

    private static final String RUBY_ENGINE_REPLACEMENT = "{RUBY_ENGINE}";
    private static final String RUBY_ENGINE_VERSION_REPLACEMENT = "{RUBY_ENGINE_VERSION}";
    private static final String RUBY_VERSION_REPLACEMENT = "{RUBY_VERSION}";
    private static final List<String> REPLACEMENTS = List.of(RUBY_ENGINE_REPLACEMENT, RUBY_ENGINE_VERSION_REPLACEMENT,
            RUBY_VERSION_REPLACEMENT);

    private static final String DEFAULT_GEM_HOME = Paths
            .get(OpenHAB.getConfigFolder(), "automation", "ruby", ".gem", RUBY_ENGINE_VERSION_REPLACEMENT).toString();
    private static final String DEFAULT_RUBYLIB = Paths.get(OpenHAB.getConfigFolder(), "automation", "ruby", "lib")
            .toString();

    private static final String GEM_HOME_CONFIG_KEY = "gem_home";
    private static final String RUBYLIB_CONFIG_KEY = "rubylib";
    private static final String GEMS_CONFIG_KEY = "gems";
    private static final String REQUIRE_CONFIG_KEY = "require";
    private static final String CHECK_UPDATE_CONFIG_KEY = "check_update";
    private static final String DEPENDENCY_TRACKING_CONFIG_KEY = "dependency_tracking";

    // Map of configuration parameters
    private final Map<String, OptionalConfigurationElement> configurationParameters = Map.ofEntries(
            Map.entry("local_context",
                    new OptionalConfigurationElement(OptionalConfigurationElement.Type.SYSTEM_PROPERTY, "singlethread",
                            "org.jruby.embed.localcontext.scope")),

            Map.entry("local_variable",
                    new OptionalConfigurationElement(OptionalConfigurationElement.Type.SYSTEM_PROPERTY, "transient",
                            "org.jruby.embed.localvariable.behavior")),

            Map.entry(GEM_HOME_CONFIG_KEY,
                    new OptionalConfigurationElement(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                            DEFAULT_GEM_HOME, "GEM_HOME")),

            Map.entry(RUBYLIB_CONFIG_KEY,
                    new OptionalConfigurationElement(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT,
                            DEFAULT_RUBYLIB, "RUBYLIB")),

            Map.entry(GEMS_CONFIG_KEY, new OptionalConfigurationElement("openhab-scripting=~>5.0")),

            Map.entry(REQUIRE_CONFIG_KEY, new OptionalConfigurationElement("openhab/dsl")),

            Map.entry(CHECK_UPDATE_CONFIG_KEY, new OptionalConfigurationElement("true")),

            Map.entry(DEPENDENCY_TRACKING_CONFIG_KEY, new OptionalConfigurationElement("true")));

    /**
     * Update configuration
     * 
     * @param config Configuration parameters to apply to ScriptEngine
     * @param factory ScriptEngineFactory to configure
     */
    void update(Map<String, Object> config, ScriptEngineFactory factory) {
        logger.trace("JRuby Script Engine Configuration: {}", config);
        configurationParameters.forEach((k, v) -> v.clearValue());
        config.forEach(this::processConfigValue);

        configureSystemProperties();

        ScriptEngine engine = factory.getScriptEngine();
        configureRubyEnvironment(engine);
        configureGems(engine);
    }

    /**
     * Apply configuration key/value to known configuration parameters
     * 
     * @param key Configuration key
     * @param value Configuration value
     */
    private void processConfigValue(String key, Object value) {
        OptionalConfigurationElement configurationElement = configurationParameters.get(key);
        if (configurationElement != null) {
            configurationElement.setValue(value.toString().trim());
        } else {
            logger.debug("Ignoring unexpected configuration key: {}", key);
        }
    }

    /**
     * Gets a single configuration element.
     */
    private String get(String key) {
        OptionalConfigurationElement configElement = configurationParameters.get(key);

        return Objects.requireNonNull(configElement).getValue();
    }

    /**
     * Gets the concrete gem home to install gems into for this version of JRuby.
     * 
     * {RUBY_ENGINE} and {RUBY_VERSION} are replaced with their current actual values.
     */
    public String getSpecificGemHome() {
        String gemHome = get(GEM_HOME_CONFIG_KEY);
        if (gemHome.isEmpty()) {
            return gemHome;
        }

        gemHome = gemHome.replace(RUBY_ENGINE_REPLACEMENT, Constants.ENGINE);
        gemHome = gemHome.replace(RUBY_ENGINE_VERSION_REPLACEMENT, Constants.VERSION);
        gemHome = gemHome.replace(RUBY_VERSION_REPLACEMENT, Constants.RUBY_VERSION);
        return new File(gemHome).toString();
    }

    /**
     * Get the base for all possible gem homes.
     * 
     * If the configured gem home contains {RUBY_ENGINE} or {RUBY_VERSION},
     * the path is cut off at that point. This means a single configuration
     * value will include the gem homes for all parallel-installed ruby
     * versions.
     * 
     */
    public String getGemHomeBase() {
        String gemHome = get(GEM_HOME_CONFIG_KEY);

        for (String replacement : REPLACEMENTS) {
            int loc = gemHome.indexOf(replacement);
            if (loc != -1) {
                gemHome = gemHome.substring(0, loc);
            }
        }
        return new File(gemHome).toString();
    }

    /**
     * Makes Gem home directory if it does not exist
     */
    private boolean ensureGemHomeExists(String gemHome) {
        File gemHomeDirectory = new File(gemHome);
        if (!gemHomeDirectory.exists()) {
            logger.debug("gem_home directory does not exist, creating");
            if (!gemHomeDirectory.mkdirs()) {
                logger.warn("Error creating gem_home directory");
                return false;
            }
        }
        return true;
    }

    /**
     * Install a gems in ScriptEngine
     * 
     * @param engine Engine to install gems
     */
    private synchronized void configureGems(ScriptEngine engine) {
        String gems = get(GEMS_CONFIG_KEY);
        if (gems.isEmpty()) {
            return;
        }

        String gemHome = getSpecificGemHome();
        if (gemHome.isEmpty()) {
            logger.warn("Gem install requested with empty gem_home, not installing gems.");
            return;
        }

        if (!ensureGemHomeExists(gemHome)) {
            return;
        }

        boolean checkUpdate = "true".equals(get(CHECK_UPDATE_CONFIG_KEY));

        String[] gemsArray = gems.split(",");
        // Set update_native_env_enabled to false so that bundler doesn't leak
        // into other script engines
        String gemCommand = "require 'jruby'\nJRuby.runtime.instance_config.update_native_env_enabled = false\nrequire 'bundler/inline'\nrequire 'openssl'\n\ngemfile("
                + checkUpdate + ") do\n" + "  source 'https://rubygems.org/'\n";
        int validGems = 0;
        for (String gem : gemsArray) {
            gem = gem.trim();
            String[] versions = {};
            if (gem.contains("=")) {
                String[] gemParts = gem.split("=", 2);
                gem = gemParts[0].trim();
                versions = gemParts[1].split(";");
            }

            if (gem.isEmpty()) {
                continue;
            }

            gemCommand += "  gem '" + gem + "'";
            for (String version : versions) {
                version = version.trim();
                if (!version.isEmpty()) {
                    gemCommand += ", '" + version + "'";
                }
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
            logger.warn("Error installing Gems", unwrap(e));
        }
    }

    /**
     * Execute ruby require statement in the ScriptEngine
     * 
     * @param engine Engine to insert the require statements
     */
    public void injectRequire(ScriptEngine engine) {
        String requires = get(REQUIRE_CONFIG_KEY);

        if (requires.isEmpty()) {
            return;
        }

        Stream.of(requires.split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).forEach(script -> {
            final String requireStatement = String.format("require '%s'", script);
            try {
                logger.trace("Injecting require statement: {}", requireStatement);
                engine.eval(requireStatement);
            } catch (ScriptException e) {
                logger.warn("Error evaluating `{}`", requireStatement, unwrap(e));
            }
        });
    }

    /**
     * Configure the optional elements of the Ruby Environment
     * 
     * @param scriptEngine Engine in which to configure environment
     */
    public void configureRubyEnvironment(ScriptEngine scriptEngine) {
        getConfigurationElements(OptionalConfigurationElement.Type.RUBY_ENVIRONMENT).forEach(configElement -> {
            String value;
            if ("GEM_HOME".equals(configElement.mappedTo().get())) {
                // this value has to be post-processed to handle replacements.
                value = getSpecificGemHome();
            } else {
                value = configElement.getValue();
            }
            scriptEngine.put("__key", configElement.mappedTo().get());
            scriptEngine.put("__value", value);
            logger.trace("Setting Ruby environment ENV['{}''] = '{}'", configElement.mappedTo().get(), value);

            try {
                scriptEngine.eval("ENV[__key] = __value");
            } catch (ScriptException e) {
                logger.warn("Error setting Ruby environment", unwrap(e));
            }
            // clean up our temporary variables
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__key");
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__value");
        });

        configureRubyLib(scriptEngine);
    }

    /**
     * Split up and insert ENV['RUBYLIB'] into Ruby's $LOAD_PATH
     * This needs to be called after ENV['RUBYLIB'] has been set by configureRubyEnvironment
     * 
     * @param engine Engine in which to configure environment
     */
    private void configureRubyLib(ScriptEngine engine) {
        String rubyLib = get(RUBYLIB_CONFIG_KEY);
        if (!rubyLib.isEmpty()) {
            final String code = "$LOAD_PATH.unshift *ENV['RUBYLIB']&.split(File::PATH_SEPARATOR)" + //
                    "&.reject(&:empty?)" + //
                    "&.reject { |path| $LOAD_PATH.include?(path) }"; //
            try {
                engine.eval(code);
            } catch (ScriptException exception) {
                logger.warn("Error setting $LOAD_PATH from RUBYLIB='{}'", rubyLib, unwrap(exception));
            }
        }
    }

    public List<String> getRubyLibPaths() {
        String rubyLib = get(RUBYLIB_CONFIG_KEY);
        if (rubyLib.isEmpty()) {
            return List.of();
        }
        return List.of(rubyLib.split(File.pathSeparator));
    }

    public boolean enableDependencyTracking() {
        return "true".equals(get(DEPENDENCY_TRACKING_CONFIG_KEY));
    }

    /**
     * Configure system properties
     * 
     * @param optionalConfigurationElements Optional system properties to configure
     */
    private void configureSystemProperties() {
        getConfigurationElements(OptionalConfigurationElement.Type.SYSTEM_PROPERTY).forEach(configElement -> {
            String systemProperty = configElement.mappedTo().get();
            String propertyValue = configElement.getValue();
            logger.trace("Setting system property ({}) to ({})", systemProperty, propertyValue);
            System.setProperty(systemProperty, propertyValue);
        });
    }

    private Stream<OptionalConfigurationElement> getConfigurationElements(OptionalConfigurationElement.Type type) {
        return configurationParameters.values().stream().filter(element -> element.type.equals(type));
    }

    /**
     * Unwraps the cause of an exception, if it has one.
     *
     * Since a user cares about the _Ruby_ stack trace of the throwable, not
     * the details of where openHAB called it.
     */
    private Throwable unwrap(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }

    /**
     * Inner static companion class for configuration elements
     */
    private static class OptionalConfigurationElement {
        private enum Type {
            SYSTEM_PROPERTY,
            RUBY_ENVIRONMENT,
            OTHER
        }

        private final String defaultValue;
        private final Optional<String> mappedTo;
        private final Type type;
        private @Nullable String value;

        private OptionalConfigurationElement(String defaultValue) {
            this(Type.OTHER, defaultValue, null);
        }

        private OptionalConfigurationElement(Type type, String defaultValue, @Nullable String mappedTo) {
            this.type = type;
            this.defaultValue = defaultValue;
            this.mappedTo = Optional.ofNullable(mappedTo);
        }

        private String getValue() {
            String value = this.value;
            return value != null ? value : this.defaultValue;
        }

        private void setValue(@Nullable String value) {
            this.value = value;
        }

        private void clearValue() {
            this.value = null;
        }

        private Optional<String> mappedTo() {
            return mappedTo;
        }
    }
}

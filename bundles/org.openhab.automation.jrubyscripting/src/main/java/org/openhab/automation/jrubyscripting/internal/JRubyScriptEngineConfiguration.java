/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jruby.runtime.Constants;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes JRuby Configuration Parameters.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Jimmy Tanagra - Add $LOAD_PATH, require injection
 */
@NonNullByDefault
public class JRubyScriptEngineConfiguration {

    public static final Path HOME_PATH = Path.of("automation", "ruby");
    public static final Path HOME_PATH_ABS = Path.of(OpenHAB.getConfigFolder()).resolve(HOME_PATH);
    private static final Path DEFAULT_GEMFILE_PATH = HOME_PATH_ABS.resolve("Gemfile");

    private static final Logger LOGGER = LoggerFactory.getLogger(JRubyScriptEngineConfiguration.class);

    private static final String RUBY_ENGINE_REPLACEMENT = "{RUBY_ENGINE}";
    private static final String RUBY_ENGINE_VERSION_REPLACEMENT = "{RUBY_ENGINE_VERSION}";
    private static final String RUBY_VERSION_REPLACEMENT = "{RUBY_VERSION}";
    private static final List<String> REPLACEMENTS = List.of(RUBY_ENGINE_REPLACEMENT, RUBY_ENGINE_VERSION_REPLACEMENT,
            RUBY_VERSION_REPLACEMENT);

    // The variable names must match the configuration keys in config.xml
    public static class JRubyScriptingConfiguration {
        // Gems
        public String gems = "openhab-scripting=~>5.0";
        public String bundle_gemfile = DEFAULT_GEMFILE_PATH.toString();
        public boolean check_update = true;
        public String require = "openhab/dsl";

        // System Properties
        public String local_context = "singlethread";
        public String local_variable = "transient";

        // Ruby Environment
        public String gem_home = HOME_PATH_ABS.resolve(Path.of(".gem", RUBY_ENGINE_VERSION_REPLACEMENT)).toString();
        public String rubylib = HOME_PATH_ABS.resolve("lib").toString();
        public boolean dependency_tracking = true;

        // Console
        public String console = "irb";
    }

    private JRubyScriptingConfiguration configuration = new JRubyScriptingConfiguration();

    private String specificGemHome = "";
    private File bundleGemfile = DEFAULT_GEMFILE_PATH.toFile();

    /**
     * Update configuration
     *
     * @param config Configuration parameters to apply to ScriptEngine
     * @param factory ScriptEngineFactory to configure
     */
    void update(Map<String, Object> config, ScriptEngineFactory factory) {
        LOGGER.trace("JRuby Script Engine Configuration: {}", config);

        // This converts Map<String, Object> to the configuration class,
        // leaving the default values in place when it's null or not set in the map.
        configuration = new Configuration(config).as(JRubyScriptingConfiguration.class);

        bundleGemfile = resolveGemfile();
        specificGemHome = resolveSpecificGemHome();
        ensureGemHomeExists(specificGemHome);

        configureSystemProperties();

        ScriptEngine engine = factory.getScriptEngine();
        configureRubyEnvironment(engine);
        // The output of the gem install process is usually written to stdout and
        // it's messy without CR. So we capture the output and log it.
        ScriptContext context = engine.getContext();
        StringWriter writer = new StringWriter();
        StringWriter errorWriter = new StringWriter();
        context.setWriter(writer);
        context.setErrorWriter(errorWriter);
        if (bundleGemfile.exists()) {
            bundlerInit(engine, configuration.check_update);
        } else {
            configureGems(engine, configuration.check_update);
        }
        if (writer.toString().length() > 0) {
            LOGGER.debug("{}", writer);
        }
        if (errorWriter.toString().length() > 0) {
            LOGGER.warn("{}", errorWriter);
        }
    }

    /**
     * Returns the current configuration as a map.
     * This is used to display the configuration in the console.
     */
    public Map<String, String> getConfigurations() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> objectMap = (Map<String, Object>) objectMapper.convertValue(configuration,
                new TypeReference<Map<String, Object>>() {
                });
        return objectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            if (entry.getValue() instanceof List<?> listValue) {
                return listValue.stream().map(Object::toString).collect(Collectors.joining("\n"));
            }
            return entry.getValue().toString();
        }));
    }

    /**
     * Returns the console script to be used for the console.
     */
    public String getConsole() {
        return configuration.console;
    }

    public String resolveSpecificGemHome() {
        String gemHome = configuration.gem_home;
        if (gemHome.isEmpty()) {
            return gemHome;
        }

        gemHome = gemHome.replace(RUBY_ENGINE_REPLACEMENT, Constants.ENGINE);
        gemHome = gemHome.replace(RUBY_ENGINE_VERSION_REPLACEMENT, Constants.VERSION);
        gemHome = gemHome.replace(RUBY_VERSION_REPLACEMENT, Constants.RUBY_VERSION);
        return new File(gemHome).toString();
    }

    /**
     * Gets the concrete gem home to install gems into for this version of JRuby.
     *
     * {RUBY_ENGINE} and {RUBY_VERSION} are replaced with their current actual values.
     */
    public String getSpecificGemHome() {
        return specificGemHome;
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
        String gemHome = configuration.gem_home;

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
        if (gemHome.isEmpty()) {
            LOGGER.warn("Gem install requested with empty gem_home, not installing gems.");
            return false;
        }

        File gemHomeDirectory = new File(gemHome);
        if (!gemHomeDirectory.exists()) {
            LOGGER.debug("gem_home directory '{}' does not exist, creating", gemHome);
            if (!gemHomeDirectory.mkdirs()) {
                LOGGER.warn("Error creating gem_home directory: {}", gemHome);
                return false;
            }
        }
        return true;
    }

    private File resolveGemfile() {
        Path gemfilePath = Path.of(configuration.bundle_gemfile);

        if (gemfilePath.equals(Path.of(""))) {
            gemfilePath = DEFAULT_GEMFILE_PATH;
        } else if (!gemfilePath.isAbsolute()) {
            gemfilePath = HOME_PATH_ABS.resolve(gemfilePath);
        }

        File gemfile = gemfilePath.toFile();
        if (gemfile.isDirectory()) {
            gemfile = gemfilePath.resolve("Gemfile").toFile();
            LOGGER.warn(
                    "The Gemfile setting is set to '{}' which is a directory. It should be set to a file. Setting it to '{}'",
                    gemfilePath, gemfile);
        }
        return gemfile;
    }

    /**
     * Returns the absolute path to the Gemfile.
     *
     * @return the path to the Gemfile.
     */
    public File getGemfile() {
        return bundleGemfile;
    }

    /**
     * Run bundle install or update.
     * 
     * This is to be called at start up or configuration change,
     * so that gems are available when user scripts are run.
     *
     * @param engine
     * @param update when true, run Bundler update, otherwise run Bundler install
     */
    public void bundlerInit(ScriptEngine engine, boolean update) {
        String operation = update ? "update" : "install";
        String code = """
                require "jruby"
                JRuby.runtime.instance_config.update_native_env_enabled = false

                require "bundler"
                require "bundler/cli"

                Bundler::CLI.start(["%s"])
                """.formatted(operation);

        try {
            LOGGER.info("Running 'bundle {}' with Gemfile '{}'", operation, bundleGemfile);
            LOGGER.trace("Bundler code:\n{}", code);
            engine.eval(code);
        } catch (ScriptException e) {
            LOGGER.error("Error running Bundler {}: {}", operation, unwrap(e).getMessage());
        }
    }

    /**
     * Run Bundler setup to load gems into the environment.
     *
     * @param engine
     */
    public void bundlerSetup(ScriptEngine engine) {
        if (!bundleGemfile.exists()) {
            LOGGER.debug("No Gemfile is found or configured. Skipping Bundler setup.");
            return;
        }

        String code = """
                require "jruby"
                JRuby.runtime.instance_config.update_native_env_enabled = false
                require  "bundler"

                Bundler.settings.temporary(auto_install: true) do
                  require "bundler/setup"
                  Bundler.require
                end
                """;

        try {
            LOGGER.debug("Running Bundler setup on Gemfile {}", bundleGemfile);
            LOGGER.trace("Bundler code:\n{}", code);
            engine.eval(code);
        } catch (ScriptException e) {
            LOGGER.error("Error running Bundler setup: {}", unwrap(e).getMessage());
        }
    }

    /**
     * Install a gems in ScriptEngine
     * 
     * @param engine Engine to install gems
     */
    synchronized void configureGems(ScriptEngine engine, boolean update) {
        String gems = configuration.gems;
        if (gems.isEmpty()) {
            return;
        }

        if (specificGemHome.isEmpty()) {
            LOGGER.warn("Gem install requested with empty gem_home, not installing gems.");
            return;
        }

        String gemLines = Arrays.stream(gems.split(",")).reduce("", (result, gem) -> {
            gem = gem.trim();
            String[] versions = {};
            if (gem.contains("=")) {
                String[] gemParts = gem.split("=", 2);
                gem = gemParts[0].trim();
                versions = gemParts[1].split(";");
            }

            if (gem.isEmpty()) {
                return result;
            }

            gem = "'" + gem + "'";
            for (String version : versions) {
                version = version.trim();
                if (!version.isEmpty()) {
                    gem += ", '" + version + "'";
                }
            }

            return result + "  gem " + gem + ", require: false\n";
        }).stripTrailing();

        if (gemLines.isEmpty()) {
            return;
        }

        // Set update_native_env_enabled to false so that bundler doesn't leak into other script engines
        String gemCommand = """
                require 'jruby'
                JRuby.runtime.instance_config.update_native_env_enabled = false
                require 'bundler/inline'
                require 'openssl'

                gemfile(%b) do
                  source 'https://rubygems.org/'
                %s
                end
                """.formatted(update, gemLines);

        try {
            LOGGER.info("Checking for {} gems '{}'", update ? "updated" : "installed", gems);
            LOGGER.trace("Gem install code:\n{}", gemCommand);
            engine.eval(gemCommand);
        } catch (ScriptException e) {
            LOGGER.warn("Error installing Gems", unwrap(e));
        }
    }

    /**
     * Execute ruby require statement in the ScriptEngine
     *
     * @param engine Engine to insert the require statements
     */
    public void injectRequire(ScriptEngine engine) {
        String requires = configuration.require;

        if (requires.isEmpty()) {
            return;
        }

        Stream.of(requires.split(",")).map(s -> s.trim()).filter(s -> !s.isEmpty()).forEach(script -> {
            final String requireStatement = String.format("require '%s'", script);
            try {
                LOGGER.trace("Injecting require statement: {}", requireStatement);
                engine.eval(requireStatement);
            } catch (ScriptException e) {
                LOGGER.warn("Error evaluating `{}`", requireStatement, unwrap(e));
            }
        });
    }

    public static void setEnvironmentVariable(ScriptEngine engine, String key, @Nullable String value) {
        if (value == null) {
            return;
        }
        LOGGER.trace("Setting Ruby environment ENV['{}'] = '{}'", key, value);
        engine.put("__key", key);
        engine.put("__value", value);
        try {
            engine.eval("ENV[__key] = __value");
        } catch (ScriptException e) {
            LOGGER.warn("Error setting Ruby environment", unwrap(e));
        } finally {
            // clean up our temporary variables
            engine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__key");
            engine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__value");
        }
    }

    /**
     * Configure the optional elements of the Ruby Environment
     *
     * @param scriptEngine Engine in which to configure environment
     */
    public void configureRubyEnvironment(ScriptEngine scriptEngine) {
        setEnvironmentVariable(scriptEngine, "GEM_HOME", getSpecificGemHome());
        setEnvironmentVariable(scriptEngine, "RUBYLIB", configuration.rubylib);
        if (bundleGemfile.exists()) {
            setEnvironmentVariable(scriptEngine, "BUNDLE_GEMFILE", bundleGemfile.toString());
        }

        configureRubyLib(scriptEngine);
        disallowExec(scriptEngine);
        configureOpenHABGem(scriptEngine);
    }

    /**
     * Split up and insert ENV['RUBYLIB'] into Ruby's $LOAD_PATH
     * This needs to be called after ENV['RUBYLIB'] has been set by configureRubyEnvironment
     *
     * @param engine Engine in which to configure environment
     */
    private void configureRubyLib(ScriptEngine engine) {
        String rubyLib = configuration.rubylib;
        if (!rubyLib.isEmpty()) {
            final String code = "$LOAD_PATH.unshift *ENV['RUBYLIB']&.split(File::PATH_SEPARATOR)" + //
                    "&.reject(&:empty?)" + //
                    "&.reject { |path| $LOAD_PATH.include?(path) }"; //
            try {
                engine.eval(code);
            } catch (ScriptException exception) {
                LOGGER.warn("Error setting $LOAD_PATH from RUBYLIB='{}'", rubyLib, unwrap(exception));
            }
        }
    }

    private void disallowExec(ScriptEngine engine) {
        try {
            engine.eval("""
                      def Process.exec(*)
                        raise NotImplementedError, "You cannot call `exec` from within openHAB"
                      end

                      module Kernel
                        module_function def exec(*)
                          raise NotImplementedError, "You cannot call `exec` from within openHAB"
                        end
                      end
                    """);
        } catch (ScriptException exception) {
            LOGGER.warn("Error preventing exec", unwrap(exception));
        }
    }

    private void configureOpenHABGem(ScriptEngine engine) {
        try {
            engine.eval("""
                    openhab_spec = Gem::Specification.new do |s|
                      s.name    = "openhab"
                      s.version = org.openhab.core.OpenHAB.version.freeze

                      def s.deleted_gem?
                        false
                      end

                      def s.installation_missing?
                        false
                      end
                    end

                    Gem::Specification.add_spec(openhab_spec)
                    Gem.post_reset { Gem::Specification.add_spec(openhab_spec) }
                    """);
        } catch (ScriptException exception) {
            LOGGER.warn("Error creating openHAB gem", unwrap(exception));
        }
    }

    public List<String> getRubyLibPaths() {
        String rubyLib = configuration.rubylib;
        if (rubyLib.isEmpty()) {
            return List.of();
        }
        return List.of(rubyLib.split(File.pathSeparator));
    }

    public boolean enableDependencyTracking() {
        return configuration.dependency_tracking;
    }

    /**
     * Configure system properties
     */
    private void configureSystemProperties() {
        Map.of( //
                "org.jruby.embed.localcontext.scope", configuration.local_context, //
                "org.jruby.embed.localvariable.behavior", configuration.local_variable //
        ).forEach((key, value) -> {
            if (value != null) {
                LOGGER.trace("Setting system property ({}) to ({})", key, value);
                System.setProperty(key, value);
            }
        });
    }

    /**
     * Unwraps the cause of an exception, if it has one.
     *
     * Since a user cares about the _Ruby_ stack trace of the throwable, not
     * the details of where openHAB called it.
     */
    private static Throwable unwrap(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }
}

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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes JRuby Configuration Parameters.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Jimmy Tanagra - Add $LOAD_PATH, require injection
 */
@NonNullByDefault
public class JRubyScriptEngineConfiguration {

    public static final String HOME_PATH = Path.of("automation", "ruby").toString();
    private static final String HOME_PATH_ABS = Path.of(OpenHAB.getConfigFolder(), HOME_PATH).toString();
    private static final String DEFAULT_GEMFILE = Path.of(HOME_PATH_ABS, "Gemfile").toString();
    private static final String UI_GEMFILE_PATH = Path
            .of(OpenHAB.getUserDataFolder(), "tmp", "jrubyscripting", "Gemfile").toString();

    private final Logger logger = LoggerFactory.getLogger(JRubyScriptEngineConfiguration.class);

    private static final String RUBY_ENGINE_REPLACEMENT = "{RUBY_ENGINE}";
    private static final String RUBY_ENGINE_VERSION_REPLACEMENT = "{RUBY_ENGINE_VERSION}";
    private static final String RUBY_VERSION_REPLACEMENT = "{RUBY_VERSION}";
    private static final List<String> REPLACEMENTS = List.of(RUBY_ENGINE_REPLACEMENT, RUBY_ENGINE_VERSION_REPLACEMENT,
            RUBY_VERSION_REPLACEMENT);

    // The variable names must match the configuration keys in config.xml
    public static class JRubyScriptingConfiguration {
        // System Properties
        public String local_context = "singlethread";
        public String local_variable = "transient";

        // Ruby Environment
        public String gem_home = Path.of(HOME_PATH_ABS, ".gem", RUBY_ENGINE_VERSION_REPLACEMENT).toString();
        public String rubylib = Path.of(HOME_PATH_ABS, "lib").toString();
        public boolean dependency_tracking = true;

        // Gems
        public String gems = "openhab-scripting=~>5.0";
        public String require = "openhab/dsl";
        public boolean check_update = true;

        // Bundler
        public String bundle_gemfile_path = DEFAULT_GEMFILE;
        public List<String> bundle_gemfile_content = List.of();
        public boolean bundle_install = true;

        // Console
        public String console = "irb";
    }

    private JRubyScriptingConfiguration configuration = new JRubyScriptingConfiguration();

    /**
     * Update configuration
     *
     * @param config Configuration parameters to apply to ScriptEngine
     * @param factory ScriptEngineFactory to configure
     */
    void update(Map<String, Object> config, ScriptEngineFactory factory) {
        logger.trace("JRuby Script Engine Configuration: {}", config);

        // This converts Map<String, Object> to the configuration class,
        // leaving the default values in place when it's null or not set in the map.
        this.configuration = new Configuration(config).as(JRubyScriptingConfiguration.class);

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
        bundlerInstall(engine);
        configureGems(engine, false);
        logger.debug("{}", writer);
        if (errorWriter.toString().length() > 0) {
            logger.warn("{}", errorWriter);
        }
    }

    /**
     * Returns the current configuration as a map.
     * This is used to display the configuration in the console.
     */
    public Map<String, String> getConfigurations() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> objectMap = objectMapper.convertValue(configuration, Map.class);
        return objectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            if (entry.getValue() instanceof List listValue) {
                return listValue.stream().map(Object::toString).collect(Collectors.joining("\n")).toString();
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

    /**
     * Gets the concrete gem home to install gems into for this version of JRuby.
     *
     * {RUBY_ENGINE} and {RUBY_VERSION} are replaced with their current actual values.
     */
    public String getSpecificGemHome() {
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
     * @return the configured gems
     */
    public String getGems() {
        return configuration.gems;
    }

    /**
     * Install a gems in ScriptEngine
     *
     * @param engine Engine to install gems
     */
    synchronized void configureGems(ScriptEngine engine, boolean force) {
        String gems = getGems();
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

        boolean checkUpdate = force || configuration.check_update;
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
                """.formatted(checkUpdate, gemLines);

        try {
            logger.debug("Installing Gems");
            logger.trace("Gem install code:\n{}", gemCommand);
            engine.eval(gemCommand);
        } catch (ScriptException e) {
            logger.warn("Error installing Gems", unwrap(e));
        }
    }

    /**
     * Returns the path to the Gemfile.
     * 
     * If the bundle_gemfile_path points to an existing file, it will be used as the Gemfile for Bundler.
     * 
     * If the file does not exist, bundle_gemfile_content config value will be saved as
     * USERDATA/tmp/jrubyscripting/Gemfile and Bundler will be run with this Gemfile.
     * 
     * If the content is empty, Bundler will be skipped.
     * 
     * @return the path to the Gemfile or null if no Gemfile is found or configured
     */
    public @Nullable String getGemfilePath() {
        boolean createGemfileFromConfig = false;
        Path gemfilePath = Path.of(configuration.bundle_gemfile_path);

        if (gemfilePath.equals(Path.of(""))) {
            gemfilePath = Path.of(DEFAULT_GEMFILE);
        } else if (!gemfilePath.isAbsolute()) {
            gemfilePath = Path.of(HOME_PATH_ABS).resolve(gemfilePath);
        }

        if (gemfilePath.toFile().exists()) {
            return gemfilePath.toString();
        }

        String configuredGemfileString = String.join("\n", configuration.bundle_gemfile_content);
        if (configuredGemfileString.isBlank()) {
            return null;
        }

        gemfilePath = Path.of(UI_GEMFILE_PATH);
        Path gemfileDirectory = gemfilePath.getParent();
        if (!gemfileDirectory.toFile().exists()) {
            logger.debug("Creating Gemfile directory {}", gemfileDirectory);
            gemfileDirectory.toFile().mkdirs();
        }

        File gemfile = gemfilePath.toFile();
        try {
            List<String> headers = List.of( //
                    "# This Gemfile is auto-generated by the JRuby Scripting add-on", //
                    "# Do not edit this file directly.", //
                    "# Edit the JRuby Scripting configuration 'bundle_gemfile_content' instead.", //
                    "");

            List<String> configuredGemfileContent = new ArrayList<>(headers);
            configuredGemfileContent.addAll(configuration.bundle_gemfile_content);

            if (gemfile.exists()) {
                List<String> fileContent = Files.readAllLines(gemfilePath, StandardCharsets.UTF_8);
                if (fileContent.equals(configuredGemfileContent)) {
                    logger.debug("Gemfile already exists and is up to date.");
                    return gemfilePath.toString();
                }
            }

            Files.write(gemfilePath, configuredGemfileContent, StandardCharsets.UTF_8);
            return gemfilePath.toString();
        } catch (IOException e) {
            logger.warn("Error creating/writing to Gemfile {}: {}", gemfilePath, e.getMessage());
        }
        return null;
    }

    /**
     * Require Bundler setup in the ScriptEngine
     * 
     * @param engine
     */
    public void bundlerSetup(ScriptEngine engine) {
        String gemfilePath = getGemfilePath();
        if (gemfilePath == null) {
            logger.debug("No Gemfile is found or configured. Skipping Bundler setup.");
            return;
        }

        String code = """
                require 'jruby'
                JRuby.runtime.instance_config.update_native_env_enabled = false
                ENV['BUNDLE_GEMFILE'] = '%s'
                require 'bundler/setup'
                """.formatted(gemfilePath);

        try {
            logger.debug("Running Bundler on Gemfile {}", gemfilePath);
            logger.trace("Bundler code:\n{}", code);
            engine.eval(code);
        } catch (ScriptException e) {
            logger.warn("Error running Bundler setup: {}", e.getMessage());
        }
    }

    public void bundlerInstall(ScriptEngine engine) {
        if (!configuration.bundle_install) {
            return;
        }

        String gemfilePath = getGemfilePath();
        if (gemfilePath == null) {
            logger.debug("No Gemfile is found or configured. Skipping Bundler install.");
            return;
        }

        String code = """
                require 'jruby'
                JRuby.runtime.instance_config.update_native_env_enabled = false
                ENV['BUNDLE_GEMFILE'] = '%s'
                require 'bundler'
                Bundler::CLI.start(['install'])
                """.formatted(gemfilePath);

        try {
            logger.debug("Running Bundler on Gemfile {}", gemfilePath);
            logger.trace("Bundler code:\n{}", code);
            engine.eval(code);
        } catch (ScriptException e) {
            logger.warn("Error running Bundler install: {}", e.getMessage());
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
        Map.of( //
                "GEM_HOME", getSpecificGemHome(), //
                "RUBYLIB", configuration.rubylib //
        ).forEach((key, value) -> {
            if (!value.isEmpty()) {
                scriptEngine.put("__key", key);
                scriptEngine.put("__value", value);
                logger.trace("Setting Ruby environment ENV['{}''] = '{}'", key, value);

                try {
                    scriptEngine.eval("ENV[__key] = __value");
                } catch (ScriptException e) {
                    logger.warn("Error setting Ruby environment", unwrap(e));
                } finally {
                    // clean up our temporary variables
                    scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__key");
                    scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__value");
                }
            }
        });

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
                logger.warn("Error setting $LOAD_PATH from RUBYLIB='{}'", rubyLib, unwrap(exception));
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
            logger.warn("Error preventing exec", unwrap(exception));
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
            logger.warn("Error creating openHAB gem", unwrap(exception));
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
                logger.trace("Setting system property ({}) to ({})", key, value);
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
    private Throwable unwrap(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }
}

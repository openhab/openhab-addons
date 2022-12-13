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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jrubyscripting.internal.watch.JRubyDependencyTracker;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptDependencyTracker;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptExtensionManagerWrapper;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Ruby.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Jimmy Tanagra - Add require injection
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class, configurationPid = "org.openhab.automation.jrubyscripting", property = Constants.SERVICE_PID
        + "=org.openhab.automation.jrubyscripting")
@ConfigurableService(category = "automation", label = "JRuby Scripting", description_uri = "automation:jruby")
public class JRubyScriptEngineFactory extends AbstractScriptEngineFactory {
    private final JRubyScriptEngineConfiguration configuration = new JRubyScriptEngineConfiguration();

    private final javax.script.ScriptEngineFactory factory = new org.jruby.embed.jsr223.JRubyEngineFactory();

    private final List<String> scriptTypes = Stream.concat(Objects.requireNonNull(factory.getExtensions()).stream(),
            Objects.requireNonNull(factory.getMimeTypes()).stream()).collect(Collectors.toUnmodifiableList());

    private JRubyDependencyTracker jrubyDependencyTracker;

    // Adds $ in front of a set of variables so that Ruby recognizes them as global
    // variables
    private static Map.Entry<String, Object> mapGlobalPresets(Map.Entry<String, Object> entry) {
        if (Character.isLowerCase(entry.getKey().charAt(0)) && !(entry.getValue() instanceof Class)
                && !(entry.getValue() instanceof Enum)) {
            return Map.entry("$" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    @Activate
    public JRubyScriptEngineFactory(Map<String, Object> config) {
        jrubyDependencyTracker = new JRubyDependencyTracker(this);
        modified(config);
    }

    @Deactivate
    protected void deactivate() {
        jrubyDependencyTracker.deactivate();
    }

    // The modified call updates configuration for the automation
    @Modified
    protected void modified(Map<String, Object> config) {
        configuration.update(config, factory);
        // Re-initialize the dependency tracker's watchers.
        jrubyDependencyTracker.deactivate();
        jrubyDependencyTracker.activate();
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // Empty comments prevent the formatter from breaking up the correct streams
        // chaining
        logger.debug("Scope Values: {}", scopeValues);
        Map<String, Object> filteredScopeValues = //
                scopeValues //
                        .entrySet() //
                        .stream() //
                        .map(JRubyScriptEngineFactory::mapGlobalPresets) //
                        .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())); //

        Map<Boolean, Map<String, Object>> partitionedMap = //
                filteredScopeValues.entrySet() //
                        .stream() //
                        .collect(Collectors.partitioningBy(entry -> (entry.getValue() instanceof Class),
                                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        super.scopeValues(scriptEngine, partitionedMap.getOrDefault(false, new HashMap<>()));
        importClassesToRuby(scriptEngine, partitionedMap.getOrDefault(true, new HashMap<>()));

        Object scriptExtension = scopeValues.get("scriptExtension");
        if (scriptExtension instanceof ScriptExtensionManagerWrapper) {
            ScriptExtensionManagerWrapper wrapper = (ScriptExtensionManagerWrapper) scriptExtension;
            // we inject like this instead of using the script context, because
            // this is executed _before_ the dependency tracker is added to the script
            // context.
            // But we need this set up before we inject our requires
            scriptEngine.put("$dependencyListener", jrubyDependencyTracker.getTracker(wrapper.getScriptIdentifier()));
        }

        // scopeValues is called twice. The first call only passed 'se'. The second call
        // passed the rest of the
        // presets, including 'ir'. We wait for the second call before running the
        // require statements.
        if (scopeValues.containsKey("ir")) {
            configuration.injectRequire(scriptEngine);
        }
    }

    private void importClassesToRuby(ScriptEngine scriptEngine, Map<String, Object> objects) {
        try {
            scriptEngine.put("__classes", objects);
            final String code = "__classes.each { |(name, klass)| Object.const_set(name, klass.ruby_class) unless Object.const_defined?(name, false) }";
            scriptEngine.eval(code);
            // clean up our temporary variable
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__classes");
        } catch (ScriptException e) {
            logger.debug("Error importing java classes", e);
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (!scriptTypes.contains(scriptType)) {
            return null;
        }
        ScriptEngine engine = factory.getScriptEngine();
        configuration.configureRubyEnvironment(engine);
        return engine;
    }

    @Override
    public @Nullable ScriptDependencyTracker getDependencyTracker() {
        return jrubyDependencyTracker;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeChangeTracker")
    public void addChangeTracker(ScriptDependencyTracker.Listener listener) {
        jrubyDependencyTracker.addChangeTracker(listener);
    }

    public void removeChangeTracker(ScriptDependencyTracker.Listener listener) {
        jrubyDependencyTracker.removeChangeTracker(listener);
    }

    public List<String> getRubyLibPaths() {
        return configuration.getRubyLibPaths();
    }

    public boolean isFileInLoadPath(String file) {
        for (String path : getRubyLibPaths()) {
            if (file.startsWith(new File(path).toString() + File.separator)) {
                return true;
            }
        }
        return false;
    }

    public String getGemHome() {
        return configuration.getSpecificGemHome();
    }

    public boolean isFileInGemHome(String file) {
        String gemHome = configuration.getGemHomeBase();
        if (gemHome.isEmpty()) {
            return false;
        }
        return file.startsWith(gemHome + File.separator);
    }
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

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

    // Filter out the File entry to prevent shadowing the Ruby File class which breaks Ruby in spectacularly
    // difficult ways to debug.
    private static final Set<String> FILTERED_PRESETS = Set.of("File", "Files", "Path", "Paths");
    private static final Set<String> INSTANCE_PRESETS = Set.of();

    private final javax.script.ScriptEngineFactory factory = new org.jruby.embed.jsr223.JRubyEngineFactory();

    private final List<String> scriptTypes = Stream
            .concat(factory.getExtensions().stream(), factory.getMimeTypes().stream())
            .collect(Collectors.toUnmodifiableList());

    // Adds @ in front of a set of variables so that Ruby recognizes them as instance variables
    private static Map.Entry<String, Object> mapInstancePresets(Map.Entry<String, Object> entry) {
        if (INSTANCE_PRESETS.contains(entry.getKey())) {
            return Map.entry("@" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    // Adds $ in front of a set of variables so that Ruby recognizes them as global variables
    private static Map.Entry<String, Object> mapGlobalPresets(Map.Entry<String, Object> entry) {
        if (Character.isLowerCase(entry.getKey().charAt(0)) && !(entry.getValue() instanceof Class)
                && !(entry.getValue() instanceof Enum)) {
            return Map.entry("$" + entry.getKey(), entry.getValue());
        } else {
            return entry;
        }
    }

    // The activate call activates the automation and sets its configuration
    @Activate
    protected void activate(Map<String, Object> config) {
        configuration.update(config, factory);
    }

    // The modified call updates configuration for the automation
    @Modified
    protected void modified(Map<String, Object> config) {
        configuration.update(config, factory);
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // Empty comments prevent the formatter from breaking up the correct streams chaining
        logger.debug("Scope Values: {}", scopeValues);
        Map<String, Object> filteredScopeValues = //
                scopeValues //
                        .entrySet() //
                        .stream() //
                        .filter(map -> !FILTERED_PRESETS.contains(map.getKey())) //
                        .map(JRubyScriptEngineFactory::mapInstancePresets) //
                        .map(JRubyScriptEngineFactory::mapGlobalPresets) //
                        .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())); //

        Map<Boolean, Map<String, Object>> partitionedMap = //
                filteredScopeValues.entrySet() //
                        .stream() //
                        .collect(Collectors.partitioningBy(entry -> (entry.getValue() instanceof Class),
                                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        importClassesToRuby(scriptEngine, partitionedMap.getOrDefault(true, new HashMap<>()));
        super.scopeValues(scriptEngine, partitionedMap.getOrDefault(false, new HashMap<>()));

        // scopeValues is called twice. The first call only passed 'se'. The second call passed the rest of the
        // presets, including 'ir'. We wait for the second call before running the require statements.
        if (scopeValues.containsKey("ir")) {
            configuration.injectRequire(scriptEngine);
        }
    }

    private void importClassesToRuby(ScriptEngine scriptEngine, Map<String, Object> objects) {
        try {
            scriptEngine.put("__classes", objects);
            final String code = "__classes.each { |(name, klass)| Object.const_set(name, klass.ruby_class) }";
            scriptEngine.eval(code);
            // clean up our temporary variable
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).remove("__classes");
        } catch (ScriptException e) {
            logger.debug("Error importing java classes", e);
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType) ? configuration.configureRubyEnvironment(factory.getScriptEngine())
                : null;
    }
}

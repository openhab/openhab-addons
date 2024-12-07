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
package org.openhab.automation.groovyscripting.internal;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Groovy.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class GroovyScriptEngineFactory extends AbstractScriptEngineFactory {

    private final org.codehaus.groovy.jsr223.GroovyScriptEngineFactory factory = new org.codehaus.groovy.jsr223.GroovyScriptEngineFactory();

    private final List<String> scriptTypes = Stream.of(factory.getExtensions(), factory.getMimeTypes())
            .flatMap(List::stream) //
            .toList();

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        ImportCustomizer importCustomizer = new ImportCustomizer();
        for (Map.Entry<String, Object> entry : scopeValues.entrySet()) {
            if (entry.getValue() instanceof Class<?> clazz) {
                String canonicalName = clazz.getCanonicalName();
                try {
                    // Only add imports for classes that are available to the classloader
                    getClass().getClassLoader().loadClass(canonicalName);
                    importCustomizer.addImport(entry.getKey(), canonicalName);
                    logger.debug("Added import for {} as {}", entry.getKey(), canonicalName);
                } catch (ClassNotFoundException e) {
                    logger.debug("Unable to add import for {} as {}", entry.getKey(), canonicalName, e);
                }
            } else {
                scriptEngine.put(entry.getKey(), entry.getValue());
            }
        }

        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) scriptEngine;
        CustomizableGroovyClassLoader cl = (CustomizableGroovyClassLoader) gse.getClassLoader();
        cl.addCompilationCustomizers(importCustomizer);
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType) ? new GroovyScriptEngineImpl(new CustomizableGroovyClassLoader())
                : null;
    }
}

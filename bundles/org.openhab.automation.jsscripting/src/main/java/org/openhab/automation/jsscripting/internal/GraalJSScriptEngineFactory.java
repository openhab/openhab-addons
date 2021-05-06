/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.automation.jsscripting.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();
        GraalJSEngineFactory graalJSEngineFactory = new GraalJSEngineFactory();

        scriptTypes.addAll(graalJSEngineFactory.getMimeTypes());
        scriptTypes.addAll(graalJSEngineFactory.getExtensions());

        return Collections.unmodifiableList(scriptTypes);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        // noop; the are retrieved via modules, not injected
    }

    @Override
    public ScriptEngine createScriptEngine(String scriptType) {
        OpenhabGraalJSScriptEngine engine = new OpenhabGraalJSScriptEngine();
        return new DebuggingGraalScriptEngine<>(engine);
    }
}

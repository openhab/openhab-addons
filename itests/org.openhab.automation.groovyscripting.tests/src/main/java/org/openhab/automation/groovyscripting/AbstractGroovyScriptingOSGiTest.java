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
package org.openhab.automation.groovyscripting;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.test.java.JavaOSGiTest;

/**
 * Provides helper methods that can be reused for testing Groovy scripts.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractGroovyScriptingOSGiTest extends JavaOSGiTest {

    protected @NonNullByDefault({}) ScriptEngine engine;

    private final String path = "OH-INF/automation/jsr223/";

    @BeforeEach
    public void init() {
        ScriptEngineManager scriptManager = Objects.requireNonNull(getService(ScriptEngineManager.class),
                "Could not get ScriptEngineManager");
        ScriptEngineContainer container = Objects.requireNonNull(
                scriptManager.createScriptEngine("groovy", "testGroovyEngine"), "Could not create Groovy ScriptEngine");
        engine = container.getScriptEngine();
    }

    protected void evalScript(String fileName) throws ScriptException, IOException {
        URL url = bundleContext.getBundle().getResource(path + fileName);
        engine.eval(new InputStreamReader(url.openStream()));
    }
}

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

import javax.script.ScriptException;

import org.junit.jupiter.api.Test;

/**
 * This tests the script modules using the Groovy scripting engine.
 *
 * @author Wouter Born - Initial contribution
 */
public class ScriptScopeOSGiTest extends AbstractGroovyScriptingOSGiTest {

    @Test
    public void scopeWorking() throws ScriptException, IOException {
        evalScript("scope-working.groovy");
    }
}

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * This tests the JSON, XML and YAML slurpers using the Groovy scripting engine.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SlurperOSGiTest extends AbstractGroovyScriptingOSGiTest {

    @Test
    public void jsonSlurper() throws ScriptException, IOException {
        evalScript("json-slurper.groovy");
    }

    @Test
    public void xmlSlurper() throws ScriptException, IOException {
        evalScript("xml-slurper.groovy");
    }

    @Test
    public void yamlSlurper() throws ScriptException, IOException {
        evalScript("yaml-slurper.groovy");
    }
}

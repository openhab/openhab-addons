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

import java.io.File;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.openhab.core.OpenHAB;

import groovy.lang.GroovyClassLoader;

/**
 * Customizes the {@link GroovyClassLoader} so that {@link CompilationCustomizer}s can be added which allows for
 * importing additional classes via scopes.
 *
 * @author Wouter Born - Initial contribution
 */
public class CustomizableGroovyClassLoader extends GroovyClassLoader {

    private static final String FILE_DIRECTORY = "automation" + File.separator + "groovy";

    private CompilerConfiguration config;

    public CustomizableGroovyClassLoader() {
        this(CustomizableGroovyClassLoader.class.getClassLoader(), new CompilerConfiguration(), true);
    }

    public CustomizableGroovyClassLoader(ClassLoader parent, CompilerConfiguration config,
            boolean useConfigurationClasspath) {
        super(parent, config, useConfigurationClasspath);
        this.config = config;
        addClasspath(OpenHAB.getConfigFolder() + File.separator + FILE_DIRECTORY);
    }

    public void addCompilationCustomizers(CompilationCustomizer... customizers) {
        config.addCompilationCustomizers(customizers);
    }
}

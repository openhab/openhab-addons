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
package org.openhab.automation.pythonscripting.internal.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

/**
 * ScriptExtensionProvider which provides various functions to help scripts to work with OSGi
 *
 * @author Holger Hees - Initial contribution (Reused from jsscripting)
 */
@NonNullByDefault
@Component(immediate = true, service = ScriptExtensionProvider.class)
public class OSGiScriptExtensionProvider extends AbstractScriptExtensionProvider {

    @Override
    protected String getPresetName() {
        return "osgi";
    }

    @Override
    protected void initializeTypes(final BundleContext context) {
        addType("bundleContext", k -> context);
    }
}

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
package org.openhab.automation.jsscripting.internal.scope;

import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;

import org.openhab.automation.jsscripting.internal.scope.binding.BindingItemProviderDelegateFactory;
import org.openhab.core.automation.module.script.ScriptExtensionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * ScriptExtensionProvider which provides support for object providers
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@Component(immediate = true, service = ScriptExtensionProvider.class)
public class ProvidersScriptExtensionProvider extends ScriptDisposalAwareScriptExtensionProvider {

    private BindingItemProviderDelegateFactory bindingItemProviderDelegateFactory;

    @Reference(cardinality = MANDATORY)
    public void setBindingItemProviderDelegateFactory(
            BindingItemProviderDelegateFactory bindingItemProviderDelegateFactory) {
        this.bindingItemProviderDelegateFactory = bindingItemProviderDelegateFactory;
    }

    @Override
    protected String getPresetName() {
        return "provider";
    }

    @Override
    protected void initializeTypes(final BundleContext context) {
        addType("itemBinding", k -> bindingItemProviderDelegateFactory);
    }
}

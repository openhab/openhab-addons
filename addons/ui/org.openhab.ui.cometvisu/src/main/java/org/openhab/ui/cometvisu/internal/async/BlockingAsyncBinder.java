/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.ui.cometvisu.internal.async;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;
import org.glassfish.jersey.servlet.spi.AsyncContextDelegateProvider;

/**
 * An {@link AbstractBinder} implementation that registers our custom
 * {@link BlockingAsyncContextDelegateProvider} class as an implementation of
 * the {@link AsyncContextDelegateProvider} SPI interface.
 * 
 * @author Tobias Bräutigam - Initial Contribution and API
 * 
 */
public class BlockingAsyncBinder extends AbstractBinder {

    @Override
    protected void configure() {
        // the qualifiedBy is needed in order for our implementation to be used
        // if there are multiple implementations of AsyncContextDelegateProvider
        bind(new BlockingAsyncContextDelegateProvider()).to(AsyncContextDelegateProvider.class)
                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
    }

}

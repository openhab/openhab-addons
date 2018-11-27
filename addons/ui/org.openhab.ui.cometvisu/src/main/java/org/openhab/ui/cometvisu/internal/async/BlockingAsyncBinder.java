/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Baseline for tests for the XML protocol implementation.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public abstract class AbstractXMLProtocolTest {

    @Mock
    protected XMLConnection con;

    protected ModelContext ctx;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        ctx = new ModelContext(con);

        onSetUp();
    }

    protected void onSetUp() throws Exception {
    }

}

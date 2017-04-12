/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.connectors;

import java.io.IOException;

import org.openmuc.jsml.structures.SML_File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a basic implementation of a SML device connector.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
abstract class ConnectorBase implements ISmlConnector {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Contructor for basic members.
     *
     * This constructor has to be called from derived classes!
     */
    ConnectorBase() {
    }

    /**
     * Open connection.
     *
     * @throws IOException
     *
     */
    protected abstract void openConnection() throws IOException;

    /**
     * Close connection.
     *
     * @throws ConnectorException
     *
     */
    protected abstract void closeConnection();

    /**
     * Close connection.
     *
     * @throws IOException
     *
     * @throws ConnectorException
     *
     */
    protected abstract SML_File getMeterValuesInternal() throws IOException;

    @Override
    public SML_File getMeterValues() throws IOException {
        SML_File smlFile = null;

        try {
            openConnection();
            smlFile = getMeterValuesInternal();
        } finally {
            closeConnection();
        }

        return smlFile;
    }
}

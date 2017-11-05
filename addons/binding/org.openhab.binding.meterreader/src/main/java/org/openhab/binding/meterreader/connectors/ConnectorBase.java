/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a basic implementation of a SML device connector.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
public abstract class ConnectorBase<T> implements IMeterReaderConnector<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private List<Consumer<T>> valueChangeListeners;

    /**
     * Contructor for basic members.
     *
     * This constructor has to be called from derived classes!
     */
    protected ConnectorBase() {
        this.valueChangeListeners = new ArrayList<>();
    }

    /**
     * Close connection.
     *
     * @throws IOException
     *
     * @throws ConnectorException
     *
     */
    protected abstract T getMeterValuesInternal(byte[] initMessage) throws IOException;

    @Override
    public T getMeterValues(byte[] initMessage) throws IOException {
        T smlFile = null;

        try {
            openConnection();
            smlFile = getMeterValuesInternal(initMessage);
            if (smlFile != null) {
                notifyListeners(smlFile);
            }
        } finally {
            closeConnection();
        }

        return smlFile;
    }

    @Override
    public void addValueChangeListener(Consumer<T> changeListener) {
        this.valueChangeListeners.add(changeListener);
    }

    @Override
    public void removeValueChangeListener(Consumer<T> changeListener) {
        this.valueChangeListeners.remove(changeListener);
    }

    protected void notifyListeners(T newValue) {
        valueChangeListeners.forEach((listener) -> listener.accept(newValue));
    }
}

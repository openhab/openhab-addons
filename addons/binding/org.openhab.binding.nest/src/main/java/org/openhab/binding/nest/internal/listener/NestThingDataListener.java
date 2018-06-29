/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used to track incoming data for Nest things.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface NestThingDataListener<T> {

    /**
     * An initial value for the data was received or the value is send again due to a refresh.
     *
     * @param data the data
     */
    void onNewData(T data);

    /**
     * Existing data was updated to a new value.
     *
     * @param oldData the previous value
     * @param data the current value
     */
    void onUpdatedData(T oldData, T data);

    /**
     * A Nest thing which previously had data is missing. E.g. it was removed from the account.
     *
     * @param nestId identifies the Nest thing
     */
    void onMissingData(String nestId);

}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.test;

import java.util.ArrayList;
import java.util.function.LongSupplier;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Sami Salonen
 *
 * @param <T>
 */
public class ResultCaptor<T> implements Answer<T> {

    private ArrayList<T> results = new ArrayList<>();
    private LongSupplier longSupplier;

    public ResultCaptor(LongSupplier longSupplier) {
        this.longSupplier = longSupplier;

    }

    public ArrayList<T> getAllReturnValues() {
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        T result = (T) invocationOnMock.callRealMethod();
        synchronized (this.results) {
            results.add(result);
        }
        long wait = longSupplier.getAsLong();
        if (wait > 0) {
            Thread.sleep(wait);
        }
        return result;
    }
}
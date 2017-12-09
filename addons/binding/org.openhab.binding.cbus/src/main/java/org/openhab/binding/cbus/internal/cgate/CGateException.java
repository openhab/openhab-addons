/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.cbus.internal.cgate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class CGateException extends Exception {
    private Logger logger = LoggerFactory.getLogger(CGateException.class);

    private final static String new_line = System.getProperty("line.separator");

    /**
     *
     */
    public CGateException() {
        this(null, null);
    }

    /**
     *
     * @param e
     */
    public CGateException(Exception e) {
        this(e.getMessage(), e);
    }

    /**
     *
     * @param response
     */
    public CGateException(String response) {
        this(response, null);
    }

    /**
     *
     * @param response
     * @param e
     */
    public CGateException(String response, Exception e) {
        super(response, e);

        String message = getMessage();

        Throwable traced_exception = e;
        while (traced_exception instanceof InvocationTargetException) {
            InvocationTargetException ite = (InvocationTargetException) traced_exception;
            traced_exception = ite.getTargetException();
        }
        if (traced_exception instanceof CGateException) {
            return;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        printStackTrace(pr);
        message += new_line + new_line + sw.toString();

        // logger.error(message);
        logger.error("{}",response, e == null ? "" : e.getMessage());
    }
}

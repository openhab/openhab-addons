/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
        logger.error(response, e == null ? "" : e.getMessage());
    }
}

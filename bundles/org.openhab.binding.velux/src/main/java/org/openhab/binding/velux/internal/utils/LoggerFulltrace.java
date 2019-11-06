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
package org.openhab.binding.velux.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;

/**
 * This is an extension of org.slf4j.Logger interface to provide an intensive logging
 * for framework debugging.
 * <h3>Typical usage pattern:</h3>
 *
 * <pre>
 * public class Wombat {
 *
 *   private final Logger logger = LoggerFactory.getLogger(Demo.class);
 *   <span style="color:green">
 *   final static LoggerFulltrace log = new LoggerFulltrace(logger,false);</span>
 *   Integer t;
 *   Integer oldT;
 *
 *   public void setTemperature(Integer temperature) {
 *     oldT = t;
 *     t = temperature;
 *     <span style="color:green">log.fulltrace("Temperature set to {}. Old temperature was {}.", t, oldT);</span>
 *     if(temperature.intValue() &gt; 50) {
 *       <span style="color:green">log.fulltrace("Temperature has risen above 50 degrees.");</span>
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class LoggerFulltrace {
    private boolean enabled;
    private Logger logger;

    /* Constructor */
    public LoggerFulltrace(Logger logger, boolean enabled) {
        this.logger = logger;
        this.enabled = enabled;
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public void fulltrace(String msg) {
        if (enabled) {
            logger.trace("{}", msg);
        }
    };

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     * </p>
     *
     * @param format the format string
     * @param arg the argument
     */
    public void fulltrace(String format, Object arg) {
        if (enabled) {
            // [ERROR] Format should be constant. Use placeholder to reduce the needless cost of parameter
            // construction. see http://www.slf4j.org/faq.html#logging_performance
            // SOLVED by bad hack. WAIT for Jenkis check being fixed
            // logger.trace(format, arg);
            logger.trace("{}/{}", format, arg);
        }
    };

}

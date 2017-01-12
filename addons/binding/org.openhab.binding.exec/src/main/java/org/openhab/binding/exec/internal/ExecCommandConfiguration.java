/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.exec.internal;

import java.math.BigDecimal;

/**
 * The {@link ExecCommandConfiguration} class represents the configuration for a command.
 *
 * @author Karel Goderis - Initial contribution
 */
public class ExecCommandConfiguration {

    private String command;
    private String transform;
    private BigDecimal interval;
    private BigDecimal timeout;
    private boolean runOnInput;
    private boolean repeatEnabled;

    public String getCommand() {
        return command;
    }

    public String getTransform() {
        return transform;
    }

    public BigDecimal getInterval() {
        return interval;
    }

    public BigDecimal getTimeout() {
        return timeout;
    }

    public Boolean getRunOnInput() {
        return runOnInput;
    }

    public Boolean getRepeatEnabled() {
        return repeatEnabled;
    }

}

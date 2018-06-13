/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

/**
 * The {@link Ihc2PatternThingConfig} holds the formating pattern used in the
 * dynamic state description.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */

public class Ihc2PatternThingConfig extends Ihc2ReadOnlyThingConfig {
    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}

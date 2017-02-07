/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200;

/**
 * The KM200ServiceTypes enum is representing the known main service paths
 *
 * @author Markus Eckhardt
 *
 */

public enum KM200ServiceTypes {
    DHWCIRCUITS {
        @Override
        public String getRootPath() {
            return "/dhwCircuits";
        }
    },
    GATEWAY {
        @Override
        public String getRootPath() {
            return "/gateway";
        }
    },
    HEATINGCIRCUITS {
        @Override
        public String getRootPath() {
            return "/heatingCircuits";
        }
    },
    HEATSOURCES {
        @Override
        public String getRootPath() {
            return "/heatSources";
        }
    },
    NOTIFICATIONS {
        @Override
        public String getRootPath() {
            return "/notifications";
        }
    },
    RECORDINGS {
        @Override
        public String getRootPath() {
            return "/recordings";
        }
    },
    SOLARCIRCUITS {
        @Override
        public String getRootPath() {
            return "/solarCircuits";
        }
    },
    SYSTEM {
        @Override
        public String getRootPath() {
            return "/system";
        }
    };

    public abstract String getRootPath();

}

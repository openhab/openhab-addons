/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The KM200ServiceTypes enum is representing the things
 *
 * @author Markus Eckhardt
 *
 */

public enum KM200ThingTypes {
    GATEWAY {
        @Override
        public String getRootPath() {
            return "/gateway";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_GATEWAY;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    },
    DHWCIRCUIT {
        @Override
        public String getRootPath() {
            return "/dhwCircuits";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_DHW_CIRCUIT;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            subServices.add("switchPrograms");
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return "status";
        }
    },
    HEATINGCIRCUIT {
        @Override
        public String getRootPath() {
            return "/heatingCircuits";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_HEATING_CIRCUIT;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            subServices.add("switchPrograms");
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return "status";
        }
    },
    HEATSOURCE {
        @Override
        public String getRootPath() {
            return "/heatSources";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_HEAT_SOURCE;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    },
    SOLARCIRCUIT {
        @Override
        public String getRootPath() {
            return "/solarCircuits";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_SOLAR_CIRCUIT;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return "status";
        }
    },
    APPLIANCE {
        @Override
        public String getRootPath() {
            return "/system/appliance";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_SYSTEM_APPLIANCE;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    },
    NOTIFICATIONS {
        @Override
        public String getRootPath() {
            return "/notifications";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_NOTIFICATION;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    },
    SENSOR {
        @Override
        public String getRootPath() {
            return "/system/sensors";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_SYSTEM_SENSOR;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    },
    SWITCHPROGRAM {
        @Override
        public String getRootPath() {
            return "";
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return KM200BindingConstants.THING_TYPE_SWITCH_PROGRAM;
        }

        @Override
        public List<String> ignoreSubService() {
            List<String> subServices = new ArrayList<String>();
            return subServices;
        }

        @Override
        public String getActiveCheckSubPath() {
            return null;
        }
    };
    public abstract String getRootPath();

    public abstract ThingTypeUID getThingTypeUID();

    public abstract List<String> ignoreSubService();

    public abstract String getActiveCheckSubPath();
}

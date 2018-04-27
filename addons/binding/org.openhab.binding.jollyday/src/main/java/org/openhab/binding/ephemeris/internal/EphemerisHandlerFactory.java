/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ephemeris.internal;

import static org.openhab.binding.ephemeris.EphemerisBindingConstants.*;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EphemerisHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class EphemerisHandlerFactory extends BaseThingHandlerFactory {
    private LocaleProvider localeProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUid) {
        return SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUid);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_HOLIDAY)) {
            return new HolidayHandler(thing, localeProvider.getLocale());
        } else if (thingTypeUID.equals(THING_SOTD)) {
            return new SotdHandler(thing, localeProvider.getLocale());
        } else if (thingTypeUID.equals(THING_USERFILE)) {
            return new UserFileHandler(thing, localeProvider.getLocale());
        }

        return null;
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

}

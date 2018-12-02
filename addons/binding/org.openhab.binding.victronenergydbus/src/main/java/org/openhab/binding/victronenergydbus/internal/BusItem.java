/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergydbus.internal;

import java.util.Map;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

/**
 * The {@link VictronEnergyDBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Samuel Lueckoff - Initial contribution
 */

@DBusInterfaceName("com.victronenergy.BusItem")
public interface BusItem extends DBusInterface {
    public static class PropertiesChanged extends DBusSignal {
        public final Map<String, Variant> changes;

        public PropertiesChanged(String path, Map<String, Variant> changes) throws DBusException {
            super(path, changes);
            this.changes = changes;
        }
    }

    public String GetDescription(String language, int length);

    public Variant GetValue();

    public String GetText();

    public int SetValue(Variant value);

    public Variant GetMin();

    public Variant GetMax();

    public int SetDefault();

    public Variant GetDefault();

}

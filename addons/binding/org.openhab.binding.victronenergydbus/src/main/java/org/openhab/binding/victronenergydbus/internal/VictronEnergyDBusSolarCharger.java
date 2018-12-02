/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergydbus.internal;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VictronEnergyDBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
public class VictronEnergyDBusSolarCharger {

    private final Logger logger = LoggerFactory.getLogger(VictronEnergyDBusSolarCharger.class);
    private String port;
    private DBusConnection conn;

    public VictronEnergyDBusSolarCharger(String port) {
        this.port = port;
    }

    public void connect() {
        try {
            this.conn = DBusConnection.getConnection(DBusBusType.SYSTEM);
            logger.debug("Try to get DBUS Connection.");
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            logger.error(e.toString());
        }
    }

    public void disconnect() {
        if (this.conn.isConnected()) {
            this.conn.disconnect();
            logger.debug("Disconnected");
        }
    }

    private <T> T getData(String item, Class<T> _valueClass) {
        BusItem bi;
        try {
            bi = conn.getRemoteObject("com.victronenergy.solarcharger." + this.port, item, BusItem.class);
            return _valueClass.cast(bi.GetValue().getValue());
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            logger.error(e.toString());
            return null;
        } catch (ClassCastException _ex) { // typ ist inkompatibel
            logger.error(_ex.toString());
            return null;
        }
    }

    public Double getDcV() {
        return getData("/Dc/0/Voltage", Double.class);
    }

    public Double getDcI() {
        return getData("/Dc/0/Current", Double.class);
    }

    public Double getPvV() {
        return getData("/Pv/V", Double.class);
    }

    public Double getPvI() {
        return getData("/Pv/I", Double.class);
    }

    public Double getYP() {
        return getData("/Yield/Power", Double.class);
    }

    public Double getYU() {
        return getData("/Yield/User", Double.class);
    }

    public Double getYS() {
        return getData("/Yield/System", Double.class);
    }

    public Double getYT() {
        return getData("/History/Daily/0/Yield", Double.class);
    }

    public Double getMPT() {
        return getData("/History/Daily/0/MaxPower", Double.class);
    }

    public Double getTIBT() {
        return getData("/History/Daily/0/TimeInBulk", Double.class);
    }

    public Double getTIFT() {
        return getData("/History/Daily/0/TimeInFloat", Double.class);
    }

    public Double getTIAT() {
        return getData("/History/Daily/0/TimeInAbsorption", Double.class);
    }

    public Double getMPVT() {
        return getData("/History/Daily/0/MaxPvVoltage", Double.class);
    }

    public Double getMBCT() {
        return getData("/History/Daily/0/MaxBatteryCurrent", Double.class);
    }

    public Double getMinBVT() {
        return getData("/History/Daily/0/MinBatteryVoltage", Double.class);
    }

    public Double getMaxBVT() {
        return getData("/History/Daily/0/MaxBatteryVoltage", Double.class);
    }

    public Double getYY() {
        return getData("/History/Daily/1/Yield", Double.class);
    }

    public Double getMPY() {
        return getData("/History/Daily/1/MaxPower", Double.class);
    }

    public Double getTIBY() {
        return getData("/History/Daily/1/TimeInBulk", Double.class);
    }

    public Double getTIFY() {
        return getData("/History/Daily/1/TimeInFloat", Double.class);
    }

    public Double getTIAY() {
        return getData("/History/Daily/1/TimeInAbsorption", Double.class);
    }

    public Double getMPVY() {
        return getData("/History/Daily/1/MaxPvVoltage", Double.class);
    }

    public Double getMBCY() {
        return getData("/History/Daily/1/MaxBatteryCurrent", Double.class);
    }

    public Double getMinBVY() {
        return getData("/History/Daily/1/MinBatteryVoltage", Double.class);
    }

    public Double getMaxBVY() {
        return getData("/History/Daily/1/MaxBatteryVoltage", Double.class);
    }

    public int getErr() {
        return getData("/ErrorCode", UInt32.class).intValue();
    }

    public String getSerial() {
        return getData("/Serial", String.class);
    }

    public int getFwV() {
        return getData("/FirmwareVersion", UInt32.class).intValue();
    }

    public int getPId() {
        return getData("/ProductId", UInt32.class).intValue();
    }

    public int getDI() {
        return getData("/DeviceInstance", UInt32.class).intValue();
    }

    public String getPn() {
        return getData("/ProductName", String.class);
    }

    public int getState() {
        return getData("/State", UInt32.class).intValue();
        // ggf als Tuple oder anderen Datentyp speichern, um int und String vom State bekommen zu können.
    }

    public String getStateStr() {
        int state = getState();
        if (state == 0) {
            return "Off";
        } else if (state == 2) {
            return "Fault";
        } else if (state == 3) {
            return "Bulk";
        } else if (state == 4) {
            return "Absorption";
        } else if (state == 5) {
            return "Float";
        } else if (state == 6) {
            return "Storage";
        } else if (state == 7) {
            return "Equalize";
        } else if (state == 252) {
            return "Hub";
        }
        return "unknown";
    }

    public String getErrString() {
        int error = getErr();
        if (error == 0) {
            return "No error";
        } else if (error == 1) {
            return "Battery temperature too high";
        } else if (error == 2) {
            return "Battery voltage too high";
        } else if (error == 3) {
            return "Battery temperature sensor miswired (+)";
        } else if (error == 4) {
            return "Battery temperature sensor miswired (-)";
        } else if (error == 5) {
            return "Battery temperature sensor disconnected";
        } else if (error == 6) {
            return "Battery voltage sense miswired (+)";
        } else if (error == 7) {
            return "Battery voltage sense miswired (-)";
        } else if (error == 8) {
            return "Battery voltage sense disconnected";
        } else if (error == 9) {
            return "Battery voltage wire losses too high";
        } else if (error == 17) {
            return "Charger temperature too high";
        } else if (error == 18) {
            return "Charger over-current";
        } else if (error == 19) {
            return "Charger current polarity reversed";
        } else if (error == 20) {
            return "Bulk time limit reached";
        } else if (error == 22) {
            return "Charger temperature sensor miswired";
        } else if (error == 23) {
            return "Charger temperature sensor disconnected";
        } else if (error == 34) {
            return "Input current too high";
        }
        return "unknown";
    }

}

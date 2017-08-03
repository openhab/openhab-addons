package org.openhab.binding.bluetoothsmart.handler;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.smarthome.core.library.types.DateTimeType;

class DateTimeChannelHandler extends SingleChannelHandler<Date, DateTimeType> {

    DateTimeChannelHandler(BluetoothSmartHandler handler, String channelID) {
        super(handler, channelID);
    }

    @Override Date convert(DateTimeType value) {
        if (value == null) {
            return null;
        }
        return value.getCalendar().getTime();
    }

    @Override DateTimeType convert(Date value) {
        if (value == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(value);
        return new DateTimeType(calendar);
    }
}

package org.openhab.binding.luftdateninfo.internal;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.dto.SensorData;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;

import com.google.gson.Gson;

public class DTOTest {

    @Test
    public void testConditions() {
        String result = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        Gson gson = new Gson();
        SensorData[] valueArray = gson.fromJson(result, SensorData[].class);
        // System.out.println(valueArray.length);
        assertEquals("Array size", 2, valueArray.length);

        SensorData d = valueArray[0];
        List<SensorDataValue> sensorDataVaueList = d.getSensordatavalues();
        Iterator<SensorDataValue> iter = sensorDataVaueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            // System.out.println(v.getValue_type() + ":" + v.getValue());
            if (v.getValue_type().equals(HTTPHandler.TEMPERATURE)) {
                assertEquals("Temperature", "22.70", v.getValue());
            } else if (v.getValue_type().equals(HTTPHandler.HUMIDITY)) {
                assertEquals("Humidiry", "61.00", v.getValue());
            }
        }
    }

}

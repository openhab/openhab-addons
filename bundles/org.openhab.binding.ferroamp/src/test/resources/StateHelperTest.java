
package org.openhab.binding.ferroamp.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ferroamp.internal.config.ChannelMapping;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StateHelperTest {

    private ChannelMapping mappingWithUnit(Unit<?> unit) {
        return new ChannelMapping("test-channel", unit, "unknown.path");
    }

    @Test
    void testConvertToState_Hertz() {
        ChannelMapping mapping = mappingWithUnit(Units.HERTZ);
        State state = StateHelper.convertToState(mapping, "50.0");
        assertTrue(state instanceof QuantityType);
        assertEquals("50 Hz", state.toFullString());
    }

    @Test
    void testConvertToState_Percent() {
        ChannelMapping mapping = mappingWithUnit(Units.PERCENT);
        State state = StateHelper.convertToState(mapping, "80.5");
        assertTrue(state instanceof QuantityType);
        assertEquals("80.5 %", state.toFullString());
    }

    @Test
    void testConvertToState_Volt() {
        ChannelMapping mapping = mappingWithUnit(Units.VOLT);
        State state = StateHelper.convertToState(mapping, "230.0");
        assertTrue(state instanceof QuantityType);
        assertEquals("230 V", state.toFullString());
    }

    @Test
    void testConvertToState_Watt() {
        ChannelMapping mapping = mappingWithUnit(Units.WATT);
        State state = StateHelper.convertToState(mapping, "1234.56");
        assertTrue(state instanceof QuantityType);
        assertEquals("1234.56 W", state.toFullString());
    }

    @Test
    void testConvertToState_WattHour() {
        ChannelMapping mapping = mappingWithUnit(Units.WATT_HOUR);
        State state = StateHelper.convertToState(mapping, "10000");
        assertTrue(state instanceof QuantityType);
        assertEquals("10000 Wh", state.toFullString());
    }

    @Test
    void testConvertToState_Ampere() {
        ChannelMapping mapping = mappingWithUnit(Units.AMPERE);
        State state = StateHelper.convertToState(mapping, "16.2");
        assertTrue(state instanceof QuantityType);
        assertEquals("16.2 A", state.toFullString());
    }

    @Test
    void testConvertToState_One_Iso8601() {
        ChannelMapping mapping = mappingWithUnit(Units.ONE);
        String isoDate = "2018-07-17T09:59:51.312Z";
        State state = StateHelper.convertToState(mapping, isoDate);
        assertTrue(state instanceof DateTimeType);
        assertEquals(isoDate, ((DateTimeType) state).getZonedDateTime(ZoneOffset.UTC).toString());
    }

    @Test
    void testConvertToState_One_NonIso8601() {
        ChannelMapping mapping = mappingWithUnit(Units.ONE);
        String value = "not-a-date";
        State state = StateHelper.convertToState(mapping, value);
        assertTrue(state instanceof StringType);
        assertEquals(value, ((StringType) state).toString());
    }

    @Test
    void testConvertToState_UnknownUnit() {
        ChannelMapping mapping = mappingWithUnit(Units.BIT_PER_SECOND); // Unknown unit for this test
        State state = StateHelper.convertToState(mapping, "123");
        assertEquals(UnDefType.UNDEF, state);
    }

    @Test
    void testConvertToState_NullValue() {
        ChannelMapping mapping = mappingWithUnit(Units.HERTZ);
        State state = StateHelper.convertToState(mapping, null);
        assertEquals(UnDefType.NULL, state);
    }
}

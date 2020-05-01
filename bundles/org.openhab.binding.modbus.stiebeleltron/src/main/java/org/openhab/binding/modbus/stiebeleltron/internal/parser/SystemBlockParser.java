package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an SystemBlock
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemBlockParser extends AbstractBaseParser  {

    public SystemBlock parse(ModbusRegisterArray raw) {
        SystemBlock block = new SystemBlock();

        block.temperature_fek = extractInt16(raw, 2, (short)0) ;
        block.temperature_fek_setpoint = extractInt16(raw, 3, (short)0);
        block.humidity_ffk = extractInt16(raw, 4, (short)0);
        block.dewpoint_ffk = extractInt16(raw, 5, (short)0);
        block.temperature_outdoor = extractInt16(raw, 6, (short)0);
        block.temperature_hk1 = extractInt16(raw, 7, (short)0) ;
        block.temperature_hk1_setpoint = extractInt16(raw, 9, (short)0);
        block.temperature_supply = extractInt16(raw, 12, (short)0) ;
        block.temperature_return = extractInt16(raw, 15, (short)0) ;
        block.temperature_water = extractInt16(raw, 21, (short)0) ;
        block.temperature_water_setpoint = extractInt16(raw, 22, (short)0);
        block.temperature_source = extractInt16(raw, 35, (short)0);
        return block;
    }
}
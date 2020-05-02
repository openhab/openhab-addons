package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an SystemBlock
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemParameterBlockParser extends AbstractBaseParser  {

    public SystemParameterBlock parse(ModbusRegisterArray raw) {
        SystemParameterBlock block = new SystemParameterBlock();

        block.operation_mode = extractUInt16(raw, 0, 0) ;
        block.comfort_temperature_heating = extractInt16(raw, 1, (short)0) ;
        block.eco_temperature_heating = extractInt16(raw, 2, (short)0) ;
        block.comfort_temperature_water = extractInt16(raw, 9, (short)0) ;
        block.eco_temperature_water = extractInt16(raw, 10, (short)0) ;
        return block;
    }
}
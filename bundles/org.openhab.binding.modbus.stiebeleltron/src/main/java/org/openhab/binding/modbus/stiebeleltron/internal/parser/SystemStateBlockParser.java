package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an SystemBlock
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemStateBlockParser extends AbstractBaseParser  {

    public SystemStateBlock parse(ModbusRegisterArray raw) {
        SystemStateBlock block = new SystemStateBlock();

        block.state = extractUInt16(raw, 0, 0) ;
        return block;
    }
}
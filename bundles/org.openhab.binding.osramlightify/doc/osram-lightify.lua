----------
-- OSRAM Lightify Packet Dissector
----------

local packet_types = {
    [0x00] = "Device Command",
    [0x01] = "Device Response",
    [0x02] = "Group Command",
    [0x03] = "Group Response"
}

local command_ids = {
    [0x13] = "LIST_PAIRED_DEVICES",
    [0x1E] = "LIST_ZONES",
    [0x26] = "GET_ZONE_INFO",
    [0x31] = "SET_LUMINANCE",
    [0x32] = "SET_SWITCH",
    [0x33] = "SET_TEMPERATURE",
    [0x36] = "SET_COLOR",
    [0x52] = "ACTIVATE_SCENE",
    [0x68] = "GET_DEVICE_INFO",
    [0x6F] = "GET_GATEWAY_FIRMWARE_VERSION",
    [0xE3] = "WIFI_CONFIGURATION"
}

local response_codes = {
    [0x00] = "No Error",
    [0x01] = "Wrong (number of) parameters?",
    [0x14] = "Unknown (0x14)",
    [0x15] = "Command is not a broadcast?",
    [0x16] = "Overrun? (Reset required)",
    [0xA7] = "Unknown (0xA7)",
    [0x0B] = "Unknown (0x0B)",
    [0xC2] = "Unknown (0xC2)",
    [0xD1] = "Unknown (0xD1)",
    [0xFF] = "Unknown Command?"
}

local device_types = {
    [1]  = "Bulb: Fixed white, dimmable, non-soft-switchable",
    [2]  = "Bulb: Tunable white, dimmable, soft-switchable",
    [4]  = "Bulb: Fixed white, dimmable, soft-switchable",
    [10] = "Bulb: RGB, tunable white, dimmable, soft-switchable",
    [16] = "Power socket",
    [32] = "Motion Sensor",
    [64] = "Switch, 2 switches, alike dimmer",
    [65] = "Switch, 4 switches"
}

local power_states = {
    [0x00] = "off",
    [0x01] = "on"
}

local reachable_states = {
    [0x00] = "reachable",
    [0x01] = "unknown status",
    [0xFF] = "non-reachable"
}

local lightify              = Proto("lightify", "OSRAM Lightify Binary Protocol")

local pf_length             = ProtoField.uint16("lightify.num_length", "Length of the packet")
local pf_packet_type        = ProtoField.uint8 ("lightify.packet_type", "Packet type", base.HEX, packet_types, 0xFF)
local pf_command_id         = ProtoField.uint8 ("lightify.command_id", "Command Id", base.HEX, command_ids, 0xFF)
local pf_request_id         = ProtoField.uint32("lightify.request_id", "Request Id")

local pf_response_code      = ProtoField.uint8 ("lightify.response_code", "Response code", base.HEX, response_codes, 0xFF)
local pf_address            = ProtoField.new   ("Address", "lightify.address", ftypes.STRING)

local pf_bytes              = ProtoField.new   ("Bytes", "lightify.bytes", ftypes.BYTES)
local pf_unknown            = ProtoField.new   ("Unknown", "lightify.unknown", ftypes.BYTES, frametype.NONE)
local pf_reserved           = ProtoField.new   ("Reserved", "lightify.reserved", ftypes.BYTES, frametype.NONE)

local pf_devices            = ProtoField.new   ("Devices", "lightify.devices", ftypes.STRING)
local pf_device             = ProtoField.new   ("Device", "lightify.device", ftypes.STRING)
local pf_device_id          = ProtoField.uint16("lightify.device_id", "Device Id")
local pf_device_type        = ProtoField.uint8 ("lightify.device_type", "Device Type", base.HEX, device_types)
local pf_device_firmware    = ProtoField.new   ("Device Firmware", "lightify.device_firmware", ftypes.STRING)
local pf_device_zoneid      = ProtoField.uint16("lightify.device_zone_id", "Zone Id", base.DEC)
local pf_device_power       = ProtoField.uint8 ("lightify.device_power_state", "Power state", base.HEX, power_states, 0xFF)
local pf_device_luminance   = ProtoField.uint8 ("lightify.device_luminance", "Luminance", base.DEC)
local pf_device_temperature = ProtoField.uint16("lightify.device_temperature", "White Temperature", base.DEC)
local pf_device_color       = ProtoField.new   ("Color", "lightify.device_color", ftypes.STRING)
local pf_device_name        = ProtoField.new   ("Name", "lightify.device_name", ftypes.STRING)
local pf_device_unknown_1   = ProtoField.uint32("Unknown1", "lightify.device_unknown1")
local pf_device_unknown_2   = ProtoField.uint32("Unknown2", "lightify.device_unknown2")
local pf_transition_time    = ProtoField.uint16("lightify.transition_time", "Transition Time")
local pf_reachable          = ProtoField.uint8 ("lightify.reachable", "Reachable", base.HEX, reachable_states)

local pf_number             = ProtoField.uint8 ("lightify.number", "Count", base.DEC)

local pf_zone_id            = ProtoField.uint16("lightify.zone_id", "Zone Id")
local pf_zone_name          = ProtoField.new   ("Name", "lightify.zone_name", ftypes.STRING)

lightify.fields = {
    pf_length, pf_packet_type, pf_command_id, pf_request_id, pf_response_code, pf_address, pf_bytes, pf_devices, pf_device,
    pf_unknown, pf_device_id, pf_device_type, pf_device_firmware, pf_device_zoneid, pf_device_power, pf_device_luminance,
    pf_device_temperature, pf_device_color, pf_device_name, pf_device_unknown_1, pf_device_unknown_2,
    pf_transition_time, pf_zone_id, pf_zone_name, pf_reachable,
    pf_reserved,
    pf_number
}

local packet_handlers = {}

local query_tcp_srcport = Field.new("tcp.srcport")

function lightify.dissector(buffer, info, root)
    info.cols.protocol:set("OSRAM")

    local length = buffer:reported_length_remaining()
    local tree = root:add(lightify, buffer:range(0, length))

    local pktCommandType = command_ids[buffer:range(3, 1):uint()]

    tree:add_le(pf_length, buffer:range(0, 2))
    tree:add(pf_packet_type, buffer:range(2, 1))
    tree:add(pf_command_id, buffer:range(3, 1))
    tree:add(pf_request_id, buffer:range(4, 4))

    local subBuffer = buffer:range(8, length - 8):tvb()
    handle(pktCommandType, subBuffer, info, tree)
end

DissectorTable.get("tcp.port"):add(4000, lightify)

---- dissector functions
packet_handlers["LIST_PAIRED_DEVICES"] = (function()
    return {
        function(buffer, info, root)
            root:add(pf_unknown, buffer:range(0, 1), "Unknown, seems to be always 0x01")
        end,

        function(buffer, info, root)
            local numDevices = buffer:range(0, 2):le_uint()
            local devicesRoot = root:add_le(pf_devices, buffer:range(0, numDevices * 50 + 2), "Devices found: "..numDevices)

            local pos = 2
            for i = 1, numDevices do
                local deviceRoot = devicesRoot:add(pf_device, buffer:range(pos, 50), "Lightify Device "..i)

                local deviceType = buffer:range(pos + 10, 1):uint()

                deviceRoot:add_le(pf_device_id, buffer:range(pos, 2))
                add_address(buffer, pos + 2, deviceRoot)
                deviceRoot:add(pf_device_type, buffer:range(pos + 10, 1))
                deviceRoot:add(pf_device_firmware, buffer:range(pos + 11, 5), device_firmware(buffer, pos + 11))
                deviceRoot:add_le(pf_device_zoneid, buffer:range(pos + 16, 2))
                deviceRoot:add(pf_device_power, buffer:range(pos + 18, 1))
                deviceRoot:add(pf_device_luminance, buffer:range(pos + 19, 1))

--              if deviceType == 2 or deviceType == 10 or deviceType == 32 then
                    deviceRoot:add_le(pf_device_temperature, buffer:range(pos + 20, 2))
--              end

--              if deviceType == 10 or deviceType == 32 then
                    deviceRoot:add(pf_device_color, buffer:range(pos + 22, 4), device_color(buffer, pos + 22))
--              end

                if deviceType == 32 then
                    
                end

                deviceRoot:add(pf_device_name, buffer:range(pos + 26, 16), extract_name(buffer, pos + 26, 16))
                deviceRoot:add_le(pf_device_unknown_1, buffer:range(pos + 42, 4))
                deviceRoot:add_le(pf_device_unknown_2, buffer:range(pos + 46, 4))
                pos = pos + 50
            end
        end
    }
end)()

packet_handlers["SET_SWITCH"] = (function()
    return {
        function(buffer, info, root)
            add_address(buffer, 0, root)
            root:add(pf_device_power, buffer:range(8, 1))
        end,

        function(buffer, info, root)
            root:add_le(pf_device_id, buffer:range(0, 2))
            add_address(buffer, 2, root)
        end
    }
end)()

packet_handlers["SET_COLOR"] = (function()
    return {
        function(buffer, info, root)
            add_address(buffer, 0, root)
            root:add(pf_device_luminance, buffer:range(8, 1))
            root:add(pf_device_color, buffer:range(8, 4), device_color(buffer, 8))
            root:add_le(pf_transition_time, buffer:range(12, 2))
        end,

        function(buffer, info, root)
            root:add_le(pf_device_id, buffer:range(0, 2))
            add_address(buffer, 2, root)
            root:add(pf_unknown, buffer:range(10, 1), "Unknown, seems to be always 0x01")
        end
    }
end)()

packet_handlers["SET_LUMINANCE"] = (function()
    return {
        function(buffer, info, root)
            add_address(buffer, 0, root)
            root:add(pf_device_luminance, buffer:range(8, 1))
            root:add_le(pf_transition_time, buffer:range(9, 2))
        end,

        function(buffer, info, root)
            root:add_le(pf_device_id, buffer:range(0, 2))
            add_address(buffer, 2, root)
            root:add(pf_unknown, buffer:range(10, 1), "Unknown, seems to be always 0x01")
        end
    }
end)()

packet_handlers["SET_TEMPERATURE"] = (function()
    return {
        function(buffer, info, root)
            add_address(buffer, 0, root)
            root:add_le(pf_device_temperature, buffer:range(8, 2))
            root:add_le(pf_transition_time, buffer:range(9, 2))
        end,

        function(buffer, info, root)
            root:add_le(pf_device_id, buffer:range(0, 2))
            add_address(buffer, 2, root)
            root:add(pf_unknown, buffer:range(10, 1), "Unknown, seems to be always 0x01")
        end
    }
end)()

packet_handlers["LIST_ZONES"] = (function()
    return {
        function(buffer, info, root)
            -- nothing to extract here
        end,

        function(buffer, info, root)
            local numZones = buffer:range(0, 2):le_uint()
            local zonesRoot = root:add_le(pf_devices, buffer:range(0, numZones * 18 + 2), "Zones found: "..numZones)

            local pos = 2
            for i = 1, numZones do
                local zoneRoot = zonesRoot:add(pf_device, buffer:range(pos, 18), "Lightify Zone "..i)
                zoneRoot:add_le(pf_zone_id, buffer:range(pos, 2))
                zoneRoot:add(pf_zone_name, buffer:range(pos + 2, 16))
                pos = pos + 18
            end
        end
    }
end)()

packet_handlers["GET_ZONE_INFO"] = (function()
    return {
        function(buffer, info, root)
            root:add_le(pf_zone_id, buffer:range(0, 2))
            -- root:add(pf_reserved, buffer:range(2, 6))
        end,

        function(buffer, info, root)
            root:add_le(pf_zone_id, buffer:range(0, 2))
            root:add(pf_zone_name, buffer:range(2, 16))

            local numDevices = buffer:range(18, 1):uint()
            local devicesRoot = root:add(pf_devices, buffer:range(18, numDevices * 8 + 1), "Devices assigned: "..numDevices)

            local pos = 19
            for i = 1, numDevices do
                add_address(buffer, pos, devicesRoot)
                pos = pos + 8
            end
        end
    }
end)()

packet_handlers["GET_DEVICE_INFO"] = (function()
    return {
        function(buffer, info, root)
            add_address(buffer, 0, root)
        end,

        function(buffer, info, root)
            root:add_le(pf_device_id, buffer:range(0, 2))
            add_address(buffer, 2, root)

            root:add(pf_reachable, buffer:range(10, 1))
            local reachable = buffer:range(10, 1):uint() == 0x00
            if not reachable then
                return
            end

            root:add(pf_unknown, buffer:range(11, 1))
            root:add(pf_device_power, buffer:range(12,1))
            root:add(pf_device_luminance, buffer:range(13,1))
            root:add(pf_device_temperature, buffer:range(14, 2))
            root:add(pf_device_color, buffer:range(16, 4), device_color(buffer, 16))
            root:add(pf_unknown, buffer:range(20, 3))
        end
    }
end)()

packet_handlers["WIFI_CONFIGURATION"] = (function()
    return {
        function(buffer, info, root)
        end,

        function(buffer, info, root)
            local numWifis = buffer:range(0, 1):uint()
            local wifiRoot = root:add(pf_devices, buffer:range(0, 1), "Wifis found: "..numWifis)

            local pos = 1
            for i = 1, numWifis do
                wifiRoot:add(pf_device_name, buffer:range(pos, 32), extract_name(buffer, pos, 32))
                pos = pos + 52
            end
        end
    }
end)()

packet_handlers["UNKNOWN"] = function(buffer, info, root)
    local length = buffer:reported_length_remaining()
    root:add(pf_bytes, buffer:range(0, length))
end
---- dissector functions

function isRequest()
    return not(query_tcp_srcport().value == 4000);
end

function add_address(buffer, start, root)
    local range = buffer:range(start, 8)
    local addressInfo = device_address(range:tvb())
    root:add(pf_address, range, addressInfo)
end

function device_address(buffer)
    local dst0 = buffer:range(0, 1):uint()
    local dst1 = buffer:range(1, 1):uint()
    local dst2 = buffer:range(2, 1):uint()
    local dst3 = buffer:range(3, 1):uint()
    local dst4 = buffer:range(4, 1):uint()
    local dst5 = buffer:range(5, 1):uint()
    local dst6 = buffer:range(6, 1):uint()
    local dst7 = buffer:range(7, 1):uint()
    return string.format('%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x', dst7, dst6, dst5, dst4, dst3, dst2, dst1, dst0)
end

function device_firmware(buffer, start)
    local dst0 = buffer:range(start, 1):uint()
    local dst1 = buffer:range(start + 1, 1):uint()
    local dst2 = buffer:range(start + 2, 1):uint()
    local dst3 = buffer:range(start + 3, 1):uint()
    local dst4 = buffer:range(start + 4, 1):uint()
    return string.format('%02d%02d%02d%d%d', dst0, dst1, dst2, dst3, dst4)
end

function device_color(buffer, start)
    local dst0 = buffer:range(start, 1):uint()
    local dst1 = buffer:range(start + 1, 1):uint()
    local dst2 = buffer:range(start + 2, 1):uint()
    local dst3 = buffer:range(start + 3, 1):uint()
    return string.format('rgb[%02d, %02d, %02d], luminance/alpha[%02d]', dst0, dst1, dst2, dst3)
end

function extract_name(buffer, start, length)
    return buffer:range(start, length):stringz(ENC_UTF_8)
end

function handle(handler, buffer, info, root)
    local handlerArray = packet_handlers[handler];

    local func
    if isRequest() then
        info.cols.info:set("Request (".. handler ..")")
        func = handlerArray == nil and {} or handlerArray[1]
    else
        info.cols.info:set("Response (".. handler ..")")
        root:add(pf_response_code, buffer(0, 1))
        buffer = buffer:range(1):tvb()
        func = handlerArray == nil and {} or handlerArray[2]
    end

    if type(func) == "function" then
        func(buffer, info, root)
    else
        packet_handlers["UNKNOWN"](buffer, info, root)
    end
end

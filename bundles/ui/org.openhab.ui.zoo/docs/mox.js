/**
 *
 * Test script for mox gateway broadcast decoding.
 * Life is easy with redirecting the broadcasts to yur dev machine:
 * ssh pi@27.32.235.221 socat udp4-listen:6666,reuseaddr,fork STDOUT| socat STDIN udp-sendto:localhost:6667
 * Then you'll receive the broadcasts on localhost:6667.
 *
 * @author Sebastian Janzen <sebastian.janzen@innoq.com>
 *
 */

var SUB_OIDS = {
	0x11: 'Channel 1',
	0x12: 'Channel 2',
	0x13: 'Channel 3',
	0x14: 'Channel 4',
	0x15: 'Channel 5',
	0x16: 'Channel 6',
	0x17: 'Channel 7',
	0x18: 'Channel 8',

	0x31: 'REM Data'
};


var CMD_CODES = {
	GET_POWER_ACTIVE:	[0x2, 0x102],
	GET_POWER_REACTIVE:	[0x3, 0x102],
	GET_POWER_APARENT:	[0x4, 0x102],
	GET_POWER_FACTOR:	[0x5, 0x102],
	GET_POWER_ACTIVE_ENERGY: [0x6, 0x102],

	POWER_ACTIVE:		[0x2, 0x306],
	POWER_REACTIVE:		[0x3, 0x306],
	POWER_APARENT:		[0x4, 0x306],
	POWER_FACTOR:		[0x5, 0x306],
	POWER_ACTIVE_ENERGY:[0x6, 0x306],

	// Query and receive actor status
	GET: 				[0x1, 0x102],
	LUMINOUS_GET:		[0x3, 0x304], // Docs fail: says 0x102
	STATUS:				[0x1, 0x303],

	// Modify status
	ONOFF:				[0x1, 0x203],
	LUMINOUS_SET:		[0x2, 0x206],
	INCREASE:			[0x1, 0x406],
	DECREASE: 			[0x2, 0x406]
};

var VAR_CODES = {
	ADDRESS: 			0x1,
	SUBOID: 			0x11,
	CMD_SET_CODE:		0x204,
	CMD_GET_CODE: 		0x102
};

var BROADCAST_CODES = {
	ADDRESS: 			0x0,
	SUBOID: 			0x2,
	CMD_CODE: 			[0x1, 0x406]
};

var GATEWAY_IP = '192.168.0.253';
var GATEWAY_PORT = 6670;

var dgram = require('dgram');

if (process.argv.length === 5) {
	var id = process.argv[2];
	var cmd = process.argv[3];
	var value = process.argv[4];
	console.log('Sending to OID:SUBOID %s value %s', id, cmd, value);
	sendBusCommand(id, cmd, value, function (err, msg) {
		if (err) console.error('Error sending command', err);
		console.log('Sent message:', msg);
		process.exit(0);
	});
} else if (process.argv.length !== 2) {
	console.error('Usage: "%s %s" To print the broadcasts or', process.argv[0], process.argv[1]);
	console.error('Usage: "%s %s <OID>:<SUBOID> <CMD> <VALUE>" to send a command to a device.', process.argv[0], process.argv[1]);
	process.exit(1);
}

//readFromFile('./moxDiscoveryData');
return;

function readFromFile(file) {
	var conv = require(file)
	console.log('>>>>>>> Host 0:');
	for(var i=0; i<conv.host0.length; i++ ) {
		var buf = new Buffer(conv.host0[i]);
		console.log(bufToObject(buf).int_string);
	}
	console.log('>>>>>>> Host 1:');
	for(var i=0; i<conv.host1.length; i++ ) {
		var buf = new Buffer(conv.host0[i]);
		console.log(bufToObject(buf).int_string);
	}
}


var current_states = { /*"OID:SUBOID:VALUE_TYPE":{values}*/ };

var socket = dgram.createSocket('udp4');
socket.bind(6666);

socket.on('message', function(buf, rinfo) {
	var msg = bufToObject(buf);

	if (!msg.oid) {return;}
	
	var id = msg.oid + ':' + msg.suboid;
	if (msg.value_type) id += ':' + msg.value_type;

	if (!current_states[id]) {
		current_states[id] = {};
	}

	if (msg.oid != 210) return;

	if (msg.value_type && msg.value_type!=='POWER_ACTIVE') {
		return;
	}

	if (msg.value && !msg.eventName && current_states[id].value != msg.value) {
		console.log('Item with ID %s\t has changed from %s \tto \t%s \t\t%s', 
			id + (id.length <= 16 ? '\t' : ''),
			current_states[id].value,
			msg.value,
			msg.hex_string);
		current_states[id].value = msg.value;
	}

	if (msg.eventName) {
		console.log('\tEvent %s with value %s fired to ID %s', msg.eventName, msg.value, id);
	}
	
});


function bufToObject(buf) {
	if (!buf) return {};
	
	var result = {
		hex_string: 	formatHex(buf.toString('hex')),
		int_string: 	formatInt(buf.toString('hex')),
		priority: 		buf.readUInt8(0),
		oid: 			(buf.readUInt8(1) << 16) + buf.readUInt16BE(2),
		suboid: 		buf.readUInt8(4),
		sub_fn: 		buf.readUInt8(5),
		zero_padding: 	buf.readUInt16BE(6), // should always be 0x0
		fn_code:		buf.readUInt16BE(8),
		errors: 		[]
	};

	if (result.zero_padding > 0) {
		result.errors.push(">>>>>> Unexpected data: sould be 0, was " + result.zero_padding);
		//console.error(">>>>>> Unexpected data: sould be 0, was %s!", result.zero_padding);
	} else {
		delete result.zero_padding;
	}

	//calculateHex(result);
	var codeFound = false;

	Object.keys(CMD_CODES).forEach(function(code) {
		if (result.eventName) return;

		var bytes = CMD_CODES[code];
		if (result.fn_code === bytes[1] && result.sub_fn === bytes[0]) {
			codeFound = true;

			switch(code) {
				case 'POWER_ACTIVE':
				case 'POWER_REACTIVE':
				case 'POWER_APPARENT':
					result.value_type = code;
					result.value = buf.readUInt32LE(10);
					result.value /= 1000;
					result.value += 'W';
					break;

				case 'POWER_FACTOR':
				case 'POWER_ACTIVE_ENERGY':
					result.value_type = code;
					result.value = buf.readUInt32LE(10);
					result.value /= 1000;
					break;

				case 'LUMINOUS_GET':
					result.value_type = code;
					result.value = buf.readUInt8(10);
					break;

				case 'LUMINOUS_SET':
					result.dimmer_time = buf.readUInt16LE(12);
				case 'INCREASE':
				case 'DECREASE':
				case 'STATUS':
				case 'ONOFF':
					result.eventName = code;
					result.value = buf.readUInt8(10);
					break;

			}

			if (result.value === NaN) {
				delete result.value;
			}
		}
	});

	if (!codeFound) {
		if (VAR_CODES.ADDRESS === result.oid) {
			codeFound = true;
			result.index = result.sub_fn;
			if (VAR_CODES.CMD_GET_CODE === result.fn_code) {
				result.eventName = 'VARIABLE_GET';
			} else {
				result.eventName = 'VARIABLE_SET';
				result.value = buf.readUInt16LE(10);
			}
			result.value = buf.readUInt16LE(10);
		} else if (
			BROADCAST_CODES.ADDRESS === result.oid &&
			BROADCAST_CODES.SUBOID === result.suboid &&
			BROADCAST_CODES.CMD_CODE[0] === result.sub_fn &&
			BROADCAST_CODES.CMD_CODE[1] === result.fn_code) {

			codeFound = true;
			result.domains0 = buf.readUInt8(10);
			result.domains1 = buf.readUInt8(11);
			result.domains2 = buf.readUInt8(12);
			result.value = buf.readUInt8(13);
		}
	}

	if (!SUB_OIDS[result.suboid]) {
		result.errors.push('Unknown SUBOID 0x' + result.suboid);
		//console.error('Unknown SUBOID 0x%s', result.suboid);
	} else {
		result.channel = (result.suboid - 0x11) + 1;
		result.channel_name = SUB_OIDS[result.suboid];
	}

	if (!codeFound) {
		result.errors.push('Could not decode package with subfn=0x'+
			result.sub_fn.toString(16) + ' and code=0x' + 
			result.fn_code.toString(16));
		/*console.error('>>>>>>>>> Could not decode package with subfn=0x%s and code=0x%s !!\n', 
			result.sub_fn.toString(16),
			result.fn_code.toString(16),
			result);*/
	}

	return result;
}

// Add space every byte for readability
function formatHex(str) {
	var result = '', i;
	for (i=0;i<str.length; i+=2) 
		result += str.slice(i,i+2) + ' '
	return result;
}

// Add space every byte for readability
function formatInt(str) {
	return formatHex(str).split(' ').map( function(code){
		return parseInt(code,16);
	}).join('\t');
}

function sendBusCommand(id, cmd, value, cb) {
	var idParts = id.split(':');
	var oid = idParts[0];
	var suboid = idParts[1];
	var cmdCodes = CMD_CODES[cmd];
	value = parseInt(value, 10);
	var messa = new Buffer([
		0x3, // priority
		Math.floor(oid/512),
		Math.floor(oid/256),
		oid%256,
		suboid,
		cmdCodes[0],
		0x0,
		0x0,
		Math.floor(cmdCodes[1]/256),
		cmdCodes[1]%256,
		value
	]);

	var message = new Buffer([ // dim to 100%
		0x3, // priority
		0x0,
		0x0,
		206,
		0x11,
		0x02,
		0x0,
		0x0,
		0x02,
		0x06,
		100,
		0,
		0, 0
	]);

	var me22ssage = new Buffer([ // switch dimmer on
		0x3, // priority
		0x0,
		0x0,
		206,
		0x11,
		0x01,
		0x0,
		0x0,
		0x02,
		0x03,
		0
	]);

	try {
		var client = dgram.createSocket('udp4');
		client.on('error', function (err) {console.error("Error!!", err);});
		client.send(message, 0, message.length, GATEWAY_PORT, GATEWAY_IP, function(err) {
			cb.call(client, err, message);
			client.close();
		});
	} catch(e) {
		console.error("Fatal error: ", e);
		if (client && client.close) client.close();
	}

}

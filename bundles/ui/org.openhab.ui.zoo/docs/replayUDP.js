	var dgram = require('dgram');
	var sin = dgram.createSocket('udp4');
	var sout = dgram.createSocket('udp4');
	sin.bind(6666);
	var targetIp = '127.0.0.1';
	var destinationPorts = [6667, 6668];
	sin.on('message', function(buf, rinfo) {
		destinationPorts.forEach(function(port){
			sout.send(buf, 0, buf.length, port, targetIp);
		});
	});
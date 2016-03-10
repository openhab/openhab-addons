var SERVER = 'http://localhost:8080'

var request = require('request');
var EventSource = require('eventsource');

var es = new EventSource(SERVER + '/rest/events');
es.onmessage = function(e) {
	var ev = JSON.parse(e.data);
	console.log("Received new state for item %s: %s", ev.topic, ev.object);
	handleEvent(ev);
};
es.onerror = function() {
  console.log('ERROR! On server sent event');
};

var itemData = null;
var mapping = {
	'light_office': 208,
	208: 'light_office'
}

request(SERVER + '/rest/items', function (err, response, body) {
	if (err!==null) {
		console.error('Could not fetch items', err, response.status);
		process.exit(1);
	}
	itemData = JSON.parse(body);
});


function handleEvent(data) {

}
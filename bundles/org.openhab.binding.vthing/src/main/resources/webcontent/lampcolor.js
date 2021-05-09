

window.onload = function() {
	var host = window.location.host;
	console.log("onload. href: " + host);

	connectToWS();
};


function connectToWS() {
	var endpoint = "ws://" + window.location.host + "/vlampws";

	//	var thingID = window.location.hostsearch

	console.log("endpoint: " + endpoint);

	const queryString = window.location.search;

	const urlParams = new URLSearchParams(queryString);


	console.log("window.location.search: " + queryString);

	const thingUID = urlParams.get('thingUID');

	myWebSocket = new WebSocket(endpoint);


	myWebSocket.onmessage = function(event) {

		console.log("onmessage.content: " + event.data);

		const messageParams = new URLSearchParams(event.data);

		if (messageParams.has('color')) {
			var color = messageParams.get('color');

			document.getElementById("lampcolor").style.background = color;
		}

		if (messageParams.has('state')) {
			var visibility = "visible";
			if (messageParams.get("state") === "OFF") {
				visibility = "hidden";
			}

			document.getElementById("lampcolor").style.visibility = visibility;
		}

	}

	myWebSocket.onopen = function(evt) {
		console.log("onopen.");
		myWebSocket.send("thingUID=" + thingUID);
	};

	myWebSocket.onclose = function(evt) {
		console.log("onclose.");
	};

	myWebSocket.onerror = function(evt) {
		console.log("Error!");
	};

	
}

function closeConn() {
	myWebSocket.close();
}

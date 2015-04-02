'use strict'

angular.module('SmartHome.services.event', []).factory 'eventService', [->

	eventSrc = new EventSource '/rest/events'

	new class EventService

		onEvent: (topic, callback) ->
			topicRegex = topic.replace('/', '\/').replace('*', '.*')
			eventSrc.addEventListener 'message', (event) ->
				data = JSON.parse event.data
				if (data.topic.match(topicRegex))
					callback data.topic, data.object

]
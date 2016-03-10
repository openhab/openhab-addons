'use strict'

angular.module('SmartHome.services.event', []).factory 'eventService', ->

	eventSrc = new EventSource '/rest/events'

	new class EventService

		createRegexFromTopic: (topic) ->
			topic.replace('/', '\/').replace('*', '.*')

		onEvent: (topic, callback) ->
			topicRegex = @createRegexFromTopic topic
			eventSrc.addEventListener 'message', (event) ->
				data = JSON.parse event.data
				if (data.topic.match(topicRegex))
					callback data.topic, data.object


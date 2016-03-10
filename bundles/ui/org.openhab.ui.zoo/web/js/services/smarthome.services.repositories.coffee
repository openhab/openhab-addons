'use strict'

angular.module('SmartHome.services.repositories', [ 'SmartHome.services.datacache' ])

.factory 'bindingRepository', (DataCache, bindingService) ->
	DataCache.init bindingService


.factory 'thingTypeRepository', (DataCache, thingTypeService) ->
	DataCache.init thingTypeService


.factory 'discoveryResultRepository', (DataCache, inboxService, eventService) ->
	DataCache.init inboxService
	eventService.onEvent 'smarthome/inbox/added/*', (topic, discoveryResult) ->
		DataCache.add discoveryResult # TODO Always add???
		return
	DataCache


.factory 'thingRepository', (DataCache, thingSetupService) ->
	DataCache.init thingSetupService


.factory 'homeGroupRepository', (DataCache, groupSetupService) ->
	DataCache.init groupSetupService


.factory 'itemRepository', (DataCache, itemService, $log, $q) ->

	GROUP_ROOMS = 'gRooms'
	GROUP_SCENES = 'gScenes'

	new class ItemRepository

		constructor: ->
			@rooms = {}
			@itemsActive = []

		hasTag: (item, tag) ->
			item.tags?.indexOf(tag) >= 0

		hasGroup: (item, group) ->
			item.groupNames?.indexOf(group) >= 0

		getRooms: ->
			defered = $q.defer()
			itemService.getByName itemName: GROUP_ROOMS, (data) =>
				result = {}
				data.members.forEach (member) ->
					result[member.name] = member
				defered.resolve result
			defered.promise


		getScenes: ->
			defered = $q.defer()
			itemService.getByName itemName: GROUP_SCENES, (data) ->
#				debugger
				scenes = data.members.map (scene) ->
					sceneData = if scene.state is 'Uninitialized' then {} else JSON.parse(scene.state)
					name:scene.name, data: sceneData

				defered.resolve scenes
			, (err) ->
				if err.status is 404
					$log.debug "Group for Scenes #{GROUP_SCENES} did not exist, creating it"
					itemService.create
						itemName: GROUP_SCENES
						type: 'GroupItem'
						category: 'scenes'
						groupNames: []
						tags: []
					, null, ((err) -> $log.error "Error creating group #{GROUP_SCENES}", err)
					defered.resolve members:[]
				else
					$log.error "Error on loading group " + GROUP_SCENES
					$log.debug err
			defered.promise

		createNewScene: (name, items) ->
			defered = $q.defer()
			sceneData = items.map (item) -> name: item.name, state: item.state

			fnError = (err) ->
				defered.reject(err)

			fnSuccessItemCreate = ->
				itemService.updateState
					itemName: name
					state: JSON.stringify(sceneData)
				, defered.resolve, fnError

			itemService.create
				itemName: name
				type: 'StringItem'
				category: 'scenes'
				tags: []
				groupNames: [GROUP_SCENES]
			, fnSuccessItemCreate, fnError

			defered.promise

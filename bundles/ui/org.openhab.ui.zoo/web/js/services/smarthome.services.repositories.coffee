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

	TAG_ROOM = 'room'
	TAG_MASTER = 'master-switch'
	GROUP_ROOMS = 'gRooms'

	new class ItemRepository

		constructor: ->
			DataCache.init itemService
			@rooms = {}
			@itemsActive = []

		hasTag: (item, tag) ->
			item.tags?.indexOf(tag) >= 0

		hasGroup: (item, group) ->
			item.groupNames?.indexOf(group) >= 0

		getRooms: (force) ->
			defered = $q.defer()
			DataCache.getAll(force).then (data) =>
				@itemsActive = []
				data.forEach (item) =>
					if item.type is 'GroupItem' and @hasGroup(item, GROUP_ROOMS)
						# This group represents a Zoo Group
						@rooms[item.name] = angular.copy item
				defered.resolve @rooms

			defered.promise

		getConsumptions: (force) ->
			defered = $q.defer()
			DataCache.getAll(force).then (data) =>

			defered.promise

		getMasterSwitchFromGroup: (group) ->
			return null unless group
			members = group.map (member) -> member if @hasTag member, TAG_MASTER
			unless members.length is 1
				$log.error "Group #{group.name} has more than one member tagged as master!"
			else
				members[0]


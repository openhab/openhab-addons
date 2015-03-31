angular.module('ZooLib.controllers.room', []).controller 'RoomController', (itemRepository, $log, $scope, $stateParams, $filter, $state) ->
	console.log 'state params: ', $stateParams

	TAG_ROOM = 'room'
	activeItemFilter = $filter('activeItems')

	@rooms = []
	@items = {}


	@refreshItems = ->
		itemRepository.getAll().then (data) =>
			data.forEach (item) ->
				if item.type is 'GroupItem' and item.tags.indexOf(TAG_ROOM)>=0
					@rooms.push item
				else if item.groupNames.length > 0
					if item.groupNames.length > 1
						$log.warn "Item #{item.label} is in multiple groups, thats not implemented", item.groupNames
					groupName = item.groupNames[0]
					@items[groupName] ?= []
					@items[groupName].push item
			, @

			$log.debug 'rooms: ', @rooms
			$log.debug 'items: ', @items

			for room, items of @items
				activeItems = activeItemFilter(items)
				@items[room + 'Active'] = activeItems

			if not $stateParams.room and @rooms.length > 0
				$log.debug "Initial redirect to room #{@rooms[0].name}"
				$state.go "rooms.room", active:true, room:@rooms[0].name

		return

	@switch = (item) ->
		$log.debug 'Handle Switch Item', item

	@switchMaster = (room) ->
		$log.debug 'Handle Master Switch Item', room

	@initialize = ->
		@refreshItems()

	@initialize()
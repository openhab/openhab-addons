'use strict'

angular.module('ZooLib.directives.switchItem', []).directive 'switchItem', ($log, itemService, $rootScope, iconResolver) ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/switchItem.html'
	scope:
		item: '='
	link: (scope) ->

		scope.local =
			state: null

		scope.options =
			cssIconClass: ''

		updateItem = (newState) ->
			scope.options.cssIconClass = iconResolver(scope.item)
			scope.local.state = newState

		scope.handleChange = ->
			$log.debug "Switch Item #{scope.item.name} changed to #{scope.local.state}"
			itemService.sendCommand itemName: scope.item.name, scope.local.state

		handleBroadcast = (event, newState) ->
			scope.item.state = newState
			updateItem newState
			$rootScope.$broadcast "updateMasterSwitch/#{scope.item.groupNames[0]}"

		scope.$watch 'item', (item) ->
			return unless item?
			updateItem item.state
			scope.$on "smarthome/command/#{item.name}", handleBroadcast
			scope.$on "smarthome/update/#{item.name}", handleBroadcast

		return

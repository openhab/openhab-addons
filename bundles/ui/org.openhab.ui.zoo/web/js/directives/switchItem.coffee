'use strict'

angular.module('ZooLib.directives.switchItem', []).directive 'switchItem', ($log, itemService) ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/switchItem.html'
	scope:
		item: '='
	link: (scope) ->

		scope.options = {}

		updateItem = (newState) ->
			scope.item.state = newState
			scope.$apply()

		# TODO Make service!
		for tag in scope.item.tags
			scope.options.cssIconClass = switch tag
				when 'power' then 'i-power'
				when 'light' then 'i-light-on-small'

		scope.$watch 'item.state', (state, oldState) ->
			return unless state isnt oldState
			itemService.sendCommand itemName: scope.item.name, state
			$log.debug "Model change: #{scope.item.label} from #{oldState} to #{state}"

		scope.$watch 'item', (item) ->
			return unless item?
			scope.$on "smarthome/command/#{item.name}", (event, newState) ->
				updateItem newState

		return

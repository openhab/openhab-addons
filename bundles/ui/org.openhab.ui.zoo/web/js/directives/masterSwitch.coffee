'use strict'

angular.module('ZooLib.directives.masterSwitch', []).directive 'masterSwitch', ($log, itemService) ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/masterSwitch.html'
	scope:
		item: '='
	link: (scope) ->

		updateItem = (newState) ->
			scope.item.state = newState
			scope.$apply()

		scope.$watch 'item.state', (state, oldState) ->
			return unless state isnt oldState
			itemService.sendCommand itemName: scope.item.name, state
			$log.log "Changed state of #{scope.item.label} from #{oldState} to #{state}"

		scope.$watch 'item', (item) ->
			return unless item?
			scope.$on "smarthome/command/#{item.name}", (event, newState) ->
				updateItem newState

		return

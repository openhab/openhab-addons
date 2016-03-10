'use strict'

angular.module('ZooLib.directives.masterSwitch', []).directive 'masterSwitch', ($log, itemService) ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/masterSwitch.html'
	scope:
		item: '='
	link: (scope) ->

		scope.local =
			state: null

		updateItemState = (newState) ->
			scope.local.state = newState

		scope.handleChange = ->
			$log.log "MasterSwitch: Fire event #{scope.item.name} to #{scope.local.state}"
			itemService.sendCommand itemName: scope.item.name, scope.local.state

		scope.$watch 'item', (item, oldItem) ->
			return if item is oldItem
			return unless item?
			updateItemState item.state

			scope.$on "smarthome/command/#{item.name}", (event, newState) ->
				scope.item.state = newState
				updateItemState newState

			scope.$on "updateMasterSwitch/#{item.name}", ->
				itemService.getByName (itemName: scope.item.name), (item) ->
					$log.info "Reloaded new masterSwitch state #{item.name}", item
					updateItemState item.state

		return

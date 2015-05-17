'use strict'

angular.module('ZooLib.directives.dimmerItem', []).directive 'dimmerItem', ($log, $timeout, itemService, $rootScope, iconResolver) ->

	translateState = (state) ->
		return state if angular.isNumber(state)
		if angular.isString state
			switch state
				when 'ON' then 100
				when 'OFF' then 0
				else parseInt state, 10
		else 0

	translateStateOnOff = (state) ->
		return state if state is 'ON' or state is 'OFF'
		stateNum = parseInt(state, 10)
		if stateNum > 0 then 'ON' else 'OFF'


	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/dimmerItem.html'
	scope:
		item: '='
	link: (scope, elem, attrs) ->

		eventBuffer = null

		scope.local =
			stateOnOff: translateStateOnOff(scope.item.state)
			dimValue: translateState(scope.item.state)
			opacity: .5

		scope.options =
			cctv: attrs.cctv?

		updateItem = (newState) ->
			scope.local.dimValue = translateState(newState)
			scope.local.stateOnOff = translateStateOnOff(newState)
			updateOpacity()
			ranger.setStart scope.local.dimValue
			scope.options.cssIconClass = iconResolver scope.item
			$rootScope.$broadcast "updateMasterSwitch/#{scope.item.groupNames[0]}"

		updateOpacity = ->
			newOpacity = scope.local.dimValue / 100
			if newOpacity < .1 then newOpacity = .1
			if newOpacity > .9 then newOpacity = .9
			scope.local.opacity = newOpacity

		options =
#			callback: updateOpacity
			decimal: no
			min: 0
			max: 100
			start: scope.local.dimValue

		ranger = new Powerange $('.js-opacity', elem)[0], options


		# TODO Transclude this
		if scope.options.cctv
			$('.popup-with-move-anim', elem).magnificPopup
				type: 'inline'
				fixedContentPos: true
				fixedBgPos: false
				closeBtnInside: true
				preloader: false
				midClick: true
				removalDelay: 300
				mainClass: 'my-mfp-slide-bottom'

		# Send command from UI to backend
		# Will receive update via broadcast which will fire updateItem()
		scope.handleChangeSwitch = ->
			$log.debug "Dimmer: Switching #{scope.item.name} to #{scope.local.stateOnOff}"
			itemService.sendCommand itemName: scope.item.name, scope.local.stateOnOff

		# Send command from UI to backend
		# Will receive update via broadcast which will fire updateItem()
		scope.handleChangeSlider = ->
			$timeout.cancel eventBuffer
			eventBuffer = $timeout ->
				$log.debug "Dimmer: Change #{scope.item.name} to #{scope.local.dimValue}"
				itemService.sendCommand itemName: scope.item.name, scope.local.dimValue
			, 100, false

		handleBroadcast = (event, newState) ->
			$log.debug "Dimmer: Command #{scope.item.name} to #{newState}"
			scope.item.state = newState
			updateItem newState

		# If item's state is changed, either by filling this isol. scope
		# or by reload, re-initialize all values.
		scope.$watch 'item', (item) ->
			return unless item?
			updateItem item.state
			scope.$on "smarthome/command/#{item.name}", handleBroadcast
			scope.$on "smarthome/update/#{item.name}", handleBroadcast

		return

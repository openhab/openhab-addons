'use strict'

angular.module('ZooLib.directives.dimmerItem', []).directive 'dimmerItem', ($log, $timeout, itemService) ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/dimmerItem.html'
	scope:
		item: '='
	link: (scope, elem, attrs) ->

		eventBuffer = null

		scope.local =
			stateOnOff: if scope.item.state > 0 then 'ON' else 'OFF'
			dimValue: parseInt(scope.item.state, 10) or 0
			opacity: parseInt(scope.item.state, 10) / 100

		scope.options =
			cctv: attrs.cctv?

		# TODO Make service!
		for tag in scope.item.tags
			scope.options.cssIconClass = switch tag
				when 'power' then 'i-power'
				when 'light' then 'i-light-on-small'

		updateItem = (newState) ->
			scope.item.state = parseInt(newState, 10)
			scope.$apply()

		setOpacity = ->
			newOpacity = scope.local.dimValue / 100
			if newOpacity < .1 then newOpacity = .1
			if newOpacity > .9 then newOpacity = .9
			scope.local.opacity = newOpacity

		options =
			callback: setOpacity
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

		scope.$watch 'local.stateOnOff', (state, oldState) ->
			return unless state isnt oldState
			itemService.sendCommand itemName: scope.item.name, state
			#$log.debug "Changed state of #{scope.item.label} from #{oldState} to #{state}"

		scope.$watch 'local.dimValue', (state, oldState) ->
			return unless state isnt oldState
			$timeout.cancel eventBuffer
			eventBuffer = $timeout ->
				itemService.sendCommand itemName: scope.item.name, state
				#$log.debug "Changed state of #{scope.item.label} from #{oldState} to #{state}"
			, 100, false

		scope.$watch 'item.state', (state, oldState) ->
			return unless state isnt oldState
			ranger.setStart state
			$log.debug "Changed state form #{scope.item.label} from #{oldState} to #{state}"

		scope.$watch 'item', (item) ->
			return unless item?
			scope.$on "smarthome/command/#{item.name}", (event, newState) ->
				updateItem newState

		return

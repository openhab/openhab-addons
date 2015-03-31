angular.module('ZooLib.directives.DimmerItem', []).directive 'dimmerItem', ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/dimmerItem.html'
	scope:
		item: '='
		onoff: '='
		dim: '='
	link: (scope, elem, attrs) ->

		scope.options =
			cctv: attrs.cctv?
			opacity: .5

		for tag in scope.item.tags
			if tag is 'power' then scope.options.cssIconClass = 'i-power'
			if tag is 'light' then scope.options.cssIconClass = 'i-light-on-small'

		setOpacity = ->
			newOpacity = scope.dim / 100
			if newOpacity < .1 then newOpacity = .1
			if newOpacity > .9 then newOpacity = .9
			scope.options.opacity = newOpacity

		options =
			callback: setOpacity
			decimal: yes
			min: 0
			max: 100
			start: scope.dim or 0

		new Powerange $('.js-opacity', elem)[0], options


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

		return

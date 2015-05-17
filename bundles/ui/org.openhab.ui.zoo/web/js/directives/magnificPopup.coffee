'use strict'

angular.module('ZooLib.directives.magnificPopup', []).directive 'magnificPopup', ($compile, $rootScope) ->

	defaults =
		type:'inline'
		fixedContentPos:true
		fixedBgPos: false
		closeBtnInside: true
		preloader: false
		midClick: true
		removalDelay: 300
		mainClass: 'my-mfp-slide-bottom'

	# Dirty, clean this pollution up!
	$rootScope.closePopup = ->
		$.magnificPopup.close()

	restrict: 'A'
	scope: {}
	link: ($scope, element, attr) ->
		attr.callbacks =
			ajaxContentAdded: ->
				content = @content
				scope = content.scope()
				$compile(content) scope
				scope.$digest()

		$scope.closePopup = ->
			debugger

		element.magnificPopup angular.extend {}, defaults, attr
		return
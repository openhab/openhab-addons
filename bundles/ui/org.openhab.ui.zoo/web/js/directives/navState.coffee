'use strict'

angular.module('ZooLib.directives.navState', ['ui.router']).directive 'navState', ($state) ->

	clearWatch = null

	link = (scope) ->
		scope.icon ?= scope.name
		scope.state ?= scope.name
		clearWatch = scope.$watch 'state', (val) ->
			if val then scope.linkToState = $state.href(val)
			clearWatch()

		scope.$on '$stateChangeSuccess', ->
			scope.isActive = $state.$current.name.indexOf(scope.name) is 0

	restrict: 'E'
	replace: yes
	template: '<li ng-class="{active:isActive}"><a href="{{linkToState}}"><i class="fa fa-{{icon}}"></i>{{title}}</a></li>'
	scope:
		name: '@'
		title: '@'
		icon: '@'
		state: '@'
	link: link

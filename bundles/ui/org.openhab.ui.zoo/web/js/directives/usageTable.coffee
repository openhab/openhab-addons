'use strict'

angular.module('ZooLib.directives.usageTable', []).directive 'usageTable', (itemService, influxDb, $q) ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/usageTable.html'
	link: (scope, elem, attr) ->

		consumptionItems = $q.defer()
		itemService.getByName itemName:'gConsumptions', (data) ->
			consumptionItems.resolve
				globalConsumption: data.state
				rooms: data.members

		activePowerItems = $q.defer()
		itemService.getByName itemName: 'gPower', (data) ->
			activePowerItems.resolve
				globalPower: data.state
				rooms: data.members

		addZ = (n) ->	if n < 10 then '0' + n else '' + n

		queryDiff = (minusDay=0, minusMonth=0) ->
			date = new Date()
			date.setHours(0,0,0,0);
			date.setDate date.getDate() - minusDay
			date.setMonth date.getMonth() - minusMonth

			y = date.getFullYear()
			m = addZ (date.getMonth() + 1)
			d = addZ date.getDate()
			influxDb.query query:"select difference(value) as diff from /^consum/i where time > '#{y}-#{m}-#{d}'", (data) ->
				console.log "Influx response to time #{date} arrived. #{data?.length} rows."

		queries = $q.all
			day: queryDiff(0).$promise
			week: queryDiff(7).$promise
			month: queryDiff(0, 1).$promise
			items: consumptionItems.promise
			power: activePowerItems.promise

		queries.then (data) ->
			console.log data
			scope.data = data.items

			lut = rearrangeConsumptionData data

			data.items.rooms.forEach (room) ->
				room.currentPower = 0 #data.power.rooms.find((r)->r.name is room.name).state
				room.consumptionDay = room.consumptionWeek = room.consumptionMonth = 0
				room.members?.forEach (roomMember) ->
					room.consumptionDay += Math.abs lut.day[roomMember.name]
					room.consumptionWeek += Math.abs (lut.week[roomMember.name] + room.consumptionDay)
					room.consumptionMonth += Math.abs (lut.month[roomMember.name] + room.consumptionWeek)
					room.cost = room.consumptionMonth * .3


		rearrangeConsumptionData = (data) ->
			day = {}
			week = {}
			month = {}
			data.day.forEach (entry) ->
				day[entry.name] = entry.points[0][1]
			data.week.forEach (entry) ->
				week[entry.name] = entry.points[0][1]
			data.month.forEach (entry) ->
				month[entry.name] = entry.points[0][1]
			{day, week, month}
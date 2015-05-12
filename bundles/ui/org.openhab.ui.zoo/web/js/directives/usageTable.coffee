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

		getMonday = (date) ->
			day = date.getDay() or 7 # 0 -> sunday
			if day isnt 1
				date.setHours(-24 * (day - 1));
			date

		queryDiff = (mode='d') ->
			date = new Date()
			date.setHours(0,0,0,0)
			switch mode
				when 'w'
					getMonday date
				when 'm'
					date.setDate 1

			y = date.getFullYear()
			m = addZ (date.getMonth() + 1)
			d = addZ date.getDate()
			influxDb.query query:"select difference(value) as diff from /^consum/i where time > '#{y}-#{m}-#{d}'", (data) ->
				console.log "Influx response to time #{mode} #{date} arrived. #{data?.length} rows."

		queries = $q.all
			day: queryDiff('d').$promise
			week: queryDiff('w').$promise
			month: queryDiff('m').$promise
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
					d = Math.abs lut.day[roomMember.name] or 0
					w = Math.abs lut.week[roomMember.name] or 0
					m = Math.abs lut.month[roomMember.name] or 0
					room.consumptionDay += d
					room.consumptionWeek += w
					room.consumptionMonth += m
					room.cost = room.consumptionMonth * .3
					return


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
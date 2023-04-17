<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<update:update-descriptions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:update="https://openhab.org/schemas/update-description/v1.0.0"
	xsi:schemaLocation="https://openhab.org/schemas/update-description/v1.0.0 https://openhab.org/schemas/update-description-1.0.0.xsd">

	<thing-type uid="meteoalerte:department">

		<instruction-set targetVersion="1">
			<update-channel id="vague-submersion">
				<type>meteoalerte:vague-submersion</type>
			</update-channel>
			<update-channel id="vent">
				<type>meteoalerte:vent</type>
			</update-channel>
			<update-channel id="pluie-inondation">
				<type>meteoalerte:pluie-inondation</type>
			</update-channel>
			<update-channel id="orage">
				<type>meteoalerte:orage</type>
			</update-channel>
			<update-channel id="inondation">
				<type>meteoalerte:inondation</type>
			</update-channel>
			<update-channel id="neige">
				<type>meteoalerte:neige</type>
			</update-channel>
			<update-channel id="canicule">
				<type>meteoalerte:canicule</type>
			</update-channel>
			<update-channel id="grand-froid">
				<type>meteoalerte:grand-froid</type>
			</update-channel>
			<update-channel id="avalanches">
				<type>meteoalerte:avalanches</type>
			</update-channel>
		</instruction-set>

	</thing-type>

</update:update-descriptions>

#!/usr/bin/awk -f

function ltrim(s) { sub(/^[ \t\r\n]+/, "", s); return s }
function rtrim(s) { sub(/[ \t\r\n]+$/, "", s); return s }
function trim(s) { return rtrim(ltrim(s)); }

BEGIN{
	FS=";"
	
	print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	print "<thing:thing-descriptions bindingId=\"nibeheatpump\""
	print "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	print "xmlns:thing=\"http://eclipse.org/smarthome/schemas/thing-description/v1.0.0\" "
	print "xsi:schemaLocation=\"http://eclipse.org/smarthome/schemas/thing-description/v1.0.0 http://eclipse.org/smarthome/schemas/thing-description-1.0.0.xsd\">"
	print ""
}
NR>5{

	title=$1
	info=$2
	id=$3
	unit=$4
	size=$5
	factor=$6
	min=$7
	max=$8
	default=$9
	mode=$10

	gsub("\"", "", title)
	gsub("&","\\&amp;", title) 
	gsub(/</,"\\&lt;", title) 
	gsub(">","\\&gt;", title) 

	gsub("\"", "", unit)

	gsub("\"", "", info)
	gsub("&","\\&amp;", info) 
	gsub(/</,"\\&lt;", info) 
	gsub(">","\\&gt;", info) 

	optionsData=info
	delete keys
	delete values
	
	# parse options from info field
	if (index(optionsData, "=") > 0)
	{
		gsub(",", "=", optionsData)
		gsub(" ", "=", optionsData)
		
		key=""
		value=""
		keyCount=1
		
		count = split(optionsData, t, "=")
		
		for (i = 0; ++i <= count;)
		{
			if (t[i] ~ /^[0-9]+$/)
			{
				if (key != "")
				{
					if (trim(value) == "")
					{
						values[keyCount-1] = values[keyCount-1] " " key
					}
					else
					{
						keys[keyCount] = trim(key)
						values[keyCount] = trim(value)
						keyCount++
						key=""
						value=""
					}
				}
				
				key = t[i]
			}
			else
			{
				if (key != "")
					value = value " " t[i]
			}
		}
		
		if (key != "")
		{
			keys[keyCount] = trim(key)
			values[keyCount] = trim(value)
		}
	}


	printf("	<channel-type id=\"type-%s\" advanced=\"true\">\n", id)

	if (min == 0 && max == 1)
	{
		printf("		<item-type>Switch</item-type>\n")
		printf("		<label>%s</label>\n", title)
		printf("		<description>%s</description>\n", info)
		
		if (mode == "R")
			readOnly="true"
		else 
			readOnly="false"

		printf("		<state readOnly=\"%s\"></state>\n", readOnly)

	}
	else
	{
		if (keys[1] != "")
			printf("		<item-type>String</item-type>\n")
		else
			printf("		<item-type>Number</item-type>\n")
			
		printf("		<label>%s</label>\n", title)
		printf("		<description>%s</description>\n", info)
		
		if (min == 0 && max == 0) 
		{
			min="0";
			if (size == "u8")
				max="255"
			if (size == "u16")
				max="65535"
			if (size == "u32")
				max="4294967295"

			if (size == "s8")
			{
				min="-128"
				max="127"
			}
			if (size == "s16")
			{
				min="-32767"
				max="32767"
			}
			if (size == "s32")
			{
				min="-2147483648"
				max="2147483647"
			}
		}
		
		if (factor == 1)
			pattern = "%d"
		if (factor == 10)
			pattern = "%.1f"
		if (factor == 100)
			pattern = "%.2f"
		if (factor == 1000)
			pattern = "%.3f"

		if (unit != "")
			pattern = pattern " " unit

		if (mode == "R")
			printf("		<state pattern=\"%s\" readOnly=\"true\">\n", pattern)
		else
			printf("		<state min=\"%s\" max=\"%s\" step=\"1\" pattern=\"%s\" readOnly=\"false\">\n", min, max, pattern)

		if (keys[1] != "")
		{
			printf("			<options>\n")
			for (x = 1; x <= keyCount; x++) 
				printf("				<option value=\"%s\">%s</option>\n", keys[x], values[x])
			printf("			</options>\n")
		}
		printf("		</state>\n")
	}

	printf("	</channel-type>\n")
}
END{
	print ""
	print "</thing:thing-descriptions>"
}
#!/usr/bin/awk -f

function ltrim(s) { sub(/^[ \t\r\n]+/, "", s); return s }
function rtrim(s) { sub(/[ \t\r\n]+$/, "", s); return s }
function trim(s) { return rtrim(ltrim(s)); }

BEGIN{
	FS=";"
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
	idefault=$9
	mode=$10

	gsub("\"", "", title)
	gsub("\"", "", info)
	gsub("\"", "", unit)

	if (mode == "R")
		type="Sensor"
	else
		type="Setting"

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

  
	if (min == 0 && max == 1)
	{
    javaType="Boolean.class"
	}
	else
	{
		if (keys[1] != "")
    {
      javaType="String.class"
    }
		else
    {
      javaType="Double.class"
    }
  }
  
  printf "CH_%s(\"%s\", \"%s\", ChannelType.%s, ChannelGroup.%s, %s),\n", id, id, title, type, type, javaType
  
}
END{
}

#!/usr/bin/awk -f

function ltrim(s) { sub(/^[ \t\r\n]+/, "", s); return s }
function rtrim(s) { sub(/[ \t\r\n]+$/, "", s); return s }
function trim(s) { return rtrim(ltrim(s)); }

BEGIN{
	FS=";"
	printf("<channel-group-type id=\"%s-sensors\">\n", model)
	print "    <label>Sensors</label>"
	print "    <description>Sensor Channels (read only)</description>"
	print "    <channels>"
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
        printf("        <channel id=\"%s\" typeId=\"%s-type-%s\"/>\n", id, model, id)
}
END{
    print "    </channels>"
    print "</channel-group-type>"
}

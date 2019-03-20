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
	default=$9
	mode=$10

	gsub("\"", "", title)
	gsub("\"", "", info)
	gsub("\"", "", unit)
	
	printf("	<channel id=\"%s\" typeId=\"type-%s\"/>\n", id, id)
}
END{
}
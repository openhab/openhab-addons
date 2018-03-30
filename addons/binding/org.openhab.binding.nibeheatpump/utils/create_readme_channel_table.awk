#!/usr/bin/awk -f

function ltrim(s) { sub(/^[ \t\r\n]+/, "", s); return s }
function rtrim(s) { sub(/[ \t\r\n]+$/, "", s); return s }
function trim(s) { return rtrim(ltrim(s)); }

BEGIN{
    FS=";"
    
    print "| Channel Type ID | Item Type    | Min          | Max          | Type    | Description                         | Values                         |"
    print "|-----------------|--------------|--------------|--------------|---------|-------------------------------------|--------------------------------|"
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
        channelType="Switch"

        if (mode == "R")
            readOnly="true"
        else 
            readOnly="false"
    }
    else
    {
        if (keys[1] != "")
            channelType="String"
        else
            channelType="Number"
            
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
        
        vals = ""
        
        if (keys[1] != "")
        {
            for (x = 1; x <= keyCount; x++)
            {
                if (vals != "")
                    vals = vals ", "
                    
                vals = vals keys[x] "=" values[x]
            }
        }
    }
    
    if (readOnly == "true")
        type="Sensor"
    else
        type="Setting"
    
    printf("| %s | %s | %d | %d | %s | %s | %s |\n", id, channelType, min, max, type, title, vals)
}
END{
}
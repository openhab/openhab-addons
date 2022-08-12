#!/bin/sh

for BASE_DIR in $(find . -maxdepth 1 -type d)
do
	#echo "Processing bundle ${BASE_DIR}..."
	
	ADDON_TYPE=$(echo "$BASE_DIR" | sed -e "s/.*openhab\.\([a-z0-9]*\).*/\1/g")
	
	BINDING_DIR="${BASE_DIR}/src/main/resources/OH-INF/binding"
	ADDON_DIR="${BASE_DIR}/src/main/resources/OH-INF/addon"
	ADDON_XML="${ADDON_DIR}/addon.xml"

	if [ -d "$BINDING_DIR" ]; then
  		echo "... found binding.xml"
  	  	if [ ! -d "$ADDON_DIR" ]; then
	  		echo "...creating ${ADDON_DIR}"
	  	  	mkdir -p "$ADDON_DIR"
  	  	fi
  	    echo "... copying binding.xml to addon.xml"
  	    cp "$BINDING_DIR/binding.xml" "$ADDON_XML"
  	    echo "... renaming XML tag from binding to addon"
  	    sed -i "" "s/binding:binding/addon:addon/g" "${ADDON_XML}"
  	    echo "... adjusting schema"
  	    sed -i "" "s/xmlns:binding/xmlns:addon/g" "${ADDON_XML}"
  	    sed -i "" "s+schemas/binding+schemas/addon+g" "${ADDON_XML}"
		echo "... setting add-on type ${ADDON_TYPE}"
		sed -i "" -e "/\<name>/i\\
	\<type>${ADDON_TYPE}\<\/type>" "${ADDON_XML}"
	else 
		echo "... didn't find binding.xml"
  	fi
done

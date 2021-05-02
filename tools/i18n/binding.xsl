<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:binding="https://openhab.org/schemas/binding/v1.0.0"
xsi:schemaLocation="https://openhab.org/schemas/binding/v1.0.0 https://openhab.org/schemas/binding-1.0.0.xsd">
<xsl:template match="binding:binding">
# binding
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="name">
binding.<xsl:value-of select="/binding:binding/@id"/>.name = <xsl:value-of select="."/>
</xsl:template>

<xsl:template match="description">
binding.<xsl:value-of select="/binding:binding/@id"/>.description = <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>


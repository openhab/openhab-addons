<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:config-description="https://openhab.org/schemas/config-description/v1.0.0"
xsi:schemaLocation="https://openhab.org/schemas/config-description/v1.0.0 https://openhab.org/schemas/config-description-1.0.0.xsd">
<xsl:template match="config-description:config-descriptions">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="config-description">
# parameter groups
<xsl:for-each select="parameter-group">
config.group.<xsl:value-of select="../@uri"/>.<xsl:value-of select="@name"/>.label = <xsl:value-of select="label"/>
config.group.<xsl:value-of select="../@uri"/>.<xsl:value-of select="@name"/>.description = <xsl:value-of select="description"/>
</xsl:for-each>

# parameters
<xsl:for-each select="parameter">
config.<xsl:value-of select="../@uri"/>.<xsl:value-of select="@name"/>.label = <xsl:value-of select="label"/>
config.<xsl:value-of select="../@uri"/>.<xsl:value-of select="@name"/>.description = <xsl:value-of select="description"/>
<xsl:for-each select="options/option">
config.<xsl:value-of select="../../../@uri"/>.<xsl:value-of select="../../@name"/>.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/></xsl:for-each>
</xsl:for-each>
</xsl:template>

</xsl:stylesheet>


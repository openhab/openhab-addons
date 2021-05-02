<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:thing="https://openhab.org/schemas/thing-description/v1.0.0"
xsi:schemaLocation="https://openhab.org/schemas/thing-description/v1.0.0 https://openhab.org/schemas/thing-description-1.0.0.xsd">
<xsl:template match="thing:thing-descriptions">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="bridge-type">
# bridge types
<xsl:for-each select=".">
thing-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.label = <xsl:value-of select="label"/>
thing-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.description = <xsl:value-of select="description"/>
</xsl:for-each>

# bridge type configuration
<xsl:for-each select="thing:thing-descriptions/bridge-type/config-description/parameter">
bridge-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.label = <xsl:value-of select="label"/>
bridge-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.description = <xsl:value-of select="description"/>
<xsl:for-each select="options/option">
bridge-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="../../@name"/>.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/>
</xsl:for-each>
</xsl:for-each>

</xsl:template>

<xsl:template match="thing-type">
# thing types
<xsl:for-each select=".">
thing-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.label = <xsl:value-of select="label"/>
thing-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.description = <xsl:value-of select="description"/>
</xsl:for-each>

# thing type configuration
<xsl:for-each select="thing:thing-descriptions/thing-type/config-description/parameter">
thing-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.label = <xsl:value-of select="label"/>
thing-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.description = <xsl:value-of select="description"/>
<xsl:for-each select="options/option">
thing-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="../../@name"/>.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/>
</xsl:for-each>
</xsl:for-each>

</xsl:template>

<xsl:template match="channel-group-type">
# channel group type
<xsl:for-each select=".">
channel-group-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.label = <xsl:value-of select="label"/>
</xsl:for-each>

</xsl:template>

<xsl:template match="channel-type">
# channel type
<xsl:for-each select=".">
channel-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.label = <xsl:value-of select="label"/>
channel-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="@id"/>.description = <xsl:value-of select="description"/>
<xsl:for-each select="state/options/option">
channel-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../../@id"/>.state.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/>
</xsl:for-each>
<xsl:for-each select="command/options/option">
channel-type.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../../@id"/>.state.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/>
</xsl:for-each>
</xsl:for-each>

# channel type configuration
<xsl:for-each select="thing:thing-descriptions/channel-type/config-description/parameter">
channel-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.label = <xsl:value-of select="label"/>
channel-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../@id"/>.<xsl:value-of select="@name"/>.description = <xsl:value-of select="description"/>
<xsl:for-each select="options/option">
channel-type.config.<xsl:value-of select="/thing:thing-descriptions/@bindingId"/>.<xsl:value-of select="../../../../@id"/>.<xsl:value-of select="../../@name"/>.option.<xsl:value-of select="@value"/> = <xsl:value-of select="."/></xsl:for-each>
</xsl:for-each>

</xsl:template>

</xsl:stylesheet>


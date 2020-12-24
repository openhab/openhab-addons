    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
        <xsl:strip-space elements="*"/>

        <!-- modified identity transform -->
        <xsl:template match="/domain_objects">
            <xsl:element name="{local-name()}">        
                <xsl:apply-templates select="gateway" />
                <xsl:apply-templates select="appliance" />
                <xsl:apply-templates select="location" />
                <xsl:apply-templates select="module" />
            </xsl:element>
        </xsl:template>

        <xsl:template match="node()">
            <!-- prevent duplicate siblings -->
            <xsl:if test="count(preceding-sibling::node()[name()=name(current())])=0">
                <!-- copy element -->
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:if>        
        </xsl:template>

        <xsl:template match="appliance">
            <!-- copy element -->
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>     
        </xsl:template>

        <xsl:template match="location">
            <!-- copy element -->
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>     
        </xsl:template>

        <xsl:template match="module">            
            <!-- copy element -->
            <xsl:copy>
                <xsl:apply-templates select="protocols/node()[name()='zig_bee_node']"/>
                <xsl:apply-templates select="@*|node()[name()!='protocols']"/>
            </xsl:copy>     
        </xsl:template>

        <xsl:template match="location/appliances">
            <!-- Apply identity transform on child elements of appliances -->
            <xsl:for-each select="appliance">
                <xsl:copy>
                    <xsl:value-of select="@id"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:template>

        <xsl:template match="module/services">
            <xsl:for-each select="./node()">
                <xsl:element name="service">
                    <xsl:element name="point_log">
                        <xsl:value-of select="functionalities/point_log/@id"/>
                    </xsl:element>                    
                    <xsl:apply-templates select="@*|node()[name()!='functionalities']"/>
                </xsl:element>
            </xsl:for-each>
        </xsl:template>

        <!-- This matches 'appliance/logs' or 'location/logs' -->
        <xsl:template match="*[name() = 'location' or name()='appliance']/logs">        
            <!-- Apply identity transform on child elements of logs -->
            <xsl:variable name="meter_id" select="point_log/*[substring(local-name(), string-length(local-name()) - string-length('_meter')+1) = '_meter']/@id"/>            
            <xsl:apply-templates select="/domain_objects/module/services/*[@id=$meter_id]/../../protocols/zig_bee_node"/>
            
            <xsl:for-each select="point_log">            
                <xsl:copy>                
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:template>

        <xsl:template match="appliance/location">
            <!-- Apply identity transform on child elements of location -->        
            <xsl:copy>
                <xsl:value-of select="@id"/>
            </xsl:copy>        
        </xsl:template>

        <xsl:template match="logs/point_log/period">    
            <xsl:element name="measurement_date">
                <xsl:value-of select="measurement/@log_date"/>
            </xsl:element>
            <xsl:element name="measurement">
                <xsl:value-of select="measurement/text()"/>
            </xsl:element>
        </xsl:template>

        <xsl:template match="*[name() = 'location' or name()='appliance']/actuator_functionalities">
            <xsl:for-each select="./*">
                <xsl:element name="actuator_functionality">
                    <xsl:if test="not(type)">
                    <xsl:choose>
                            <xsl:when test="local-name()='relay_functionality'">
                                <xsl:element name="type">
                                    <xsl:text>relay</xsl:text>
                                </xsl:element>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:element name="type">
                                    <xsl:value-of select="local-name()"/>
                                </xsl:element>
                            </xsl:otherwise>
                        </xsl:choose>                    
                    </xsl:if>
                    <xsl:for-each select=".">
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:for-each>
                </xsl:element>            
            </xsl:for-each>
        </xsl:template>

        <!-- attributes to elements -->
        <xsl:template match="@*">
            <xsl:element name="{name()}">
                <xsl:value-of select="."/>
            </xsl:element>
        </xsl:template>

    </xsl:stylesheet>
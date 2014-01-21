<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xxx="my:dummyNS" exclude-result-prefixes="xxx"
 >
 
 <xsl:output omit-xml-declaration="yes" indent="yes"/>
 <xsl:namespace-alias result-prefix="xsl" stylesheet-prefix="xxx"/>
 <xsl:param name="templateName" />
 
  
  <xsl:template match="node()|@*">
  <xsl:copy>
   <xsl:apply-templates select="node()|@*"/>
  </xsl:copy>
 </xsl:template>
  
  <xsl:template match="//*/text()">
      <xsl:call-template name="replace">
   		<xsl:with-param name="input" select="."/>
   </xsl:call-template>
  </xsl:template>

 
  <xsl:template match="//*[not(node())]">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
  </xsl:copy>
 </xsl:template>
 
  <xsl:template match="widget/y/text()">
  <xsl:variable name="currentY" select="."/>
  <xsl:choose>
  		<xsl:when test="contains($templateName,'_header')">
  			<xxx:value-of select="{$currentY}"/>
  		</xsl:when>
  		<xsl:otherwise>
  			<xxx:value-of select="{$currentY}+$ymax_header+$counter*$ymax"/>
  		</xsl:otherwise>
  </xsl:choose>		
  
 </xsl:template>
 
	<xsl:variable name="xmax">
		<xsl:for-each select="//widget/x">
			<xsl:sort data-type="number" order="descending" />
			<xsl:if test="position()=1">
				<xsl:value-of select="sum((.| ../width)[number(.) = .])" />
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:variable name="ymax">
		<xsl:for-each select="//widget/y">
			<xsl:sort data-type="number" order="descending" />
			<xsl:if test="position()=1">
				<xsl:value-of select="sum((.| ../height)[number(.) = .])" />
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
 
  <xsl:template match="display">
  <opitemplate>
  	<xsl:choose>
  		<xsl:when test="contains($templateName,'_header')">
  			<xxx:variable name="ymax_header" select="{$ymax}"/>
    		<xxx:variable name="xmax_header" select="{$xmax}"/>
    		<xxx:template match="properties/properties" name="{$templateName}">
    			<xsl:apply-templates select="/display/widget"/>
			</xxx:template>
  		</xsl:when>
  		<xsl:otherwise>
    		<xxx:variable name="ymax" select="{$ymax}"/>
    		<xxx:variable name="xmax" select="{$xmax}"/>
    		<xxx:template match="property" name="{$templateName}">

    		<xxx:variable name="counter">
      			<xxx:value-of select="count(preceding-sibling::property)"/>
    		</xxx:variable>

			<xsl:apply-templates select="/display/widget"/>
			</xxx:template>
    	</xsl:otherwise>
    </xsl:choose>
  </opitemplate>
  </xsl:template> 

<xsl:template name="replace">
  <xsl:param name="input" />

  <xsl:variable name="before" select="substring-before( $input, '${' )" />
  <xsl:variable name="after" select="substring-after( $input, '}' )" />
  <xsl:variable name="replace" select="substring-after( substring-before( $input, '}' ), '${' )" />

  <xsl:choose>
    <xsl:when test="$replace">

<!--       moved outside the recursive call -->
      <xsl:value-of select="$before" />
		<xsl:element name="xsl:value-of">
		<xsl:attribute name="select">
        <xsl:value-of select="$replace" />
        </xsl:attribute>
        </xsl:element>

      <xsl:call-template name="replace">
        <xsl:with-param name="input">
          <xsl:value-of select="$after" />
        </xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="$input" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  </xsl:stylesheet>
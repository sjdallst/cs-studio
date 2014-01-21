<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
<display>
<height><xsl:value-of select="count(//property)*$ymax+$ymax_header"/></height>
<width><xsl:value-of select="$xmax"/></width>

<xsl:apply-templates select="properties/properties">
</xsl:apply-templates>

<xsl:apply-templates select="properties/properties/property">
</xsl:apply-templates>
</display>
</xsl:template>
</xsl:stylesheet>

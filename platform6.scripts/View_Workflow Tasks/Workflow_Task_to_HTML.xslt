<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html" indent="yes" omit-xml-declaration="yes" />
    
    <xsl:template match="/WorkflowTask">


		<div class="message-info-view-wrapper">
			<div class="row">
                <div class="col-xs-12 text-medium" style="margin-bottom: 5px;">
                    <div>
                        <span class="right-spaced"><xsl:value-of select="itemAttributes[key='TransactionType']/values/_text"/><xsl:text> # </xsl:text><xsl:value-of select="itemAttributes[key='TransactionNumber']/values/_text"/></span>
                    </div>
                </div>
                
                <div class="col-xs-12 text-medium" style="margin-bottom: 5px;">
                    <div>
                        <span class="right-spaced">Assigned to:</span>
                        <a href="#" data-toggle="modal" data-target="#assigneesDialog"><xsl:value-of select="itemAttributes[key='AssignedTo']/values/EN"/></a>
                    </div>
                </div>
                
				<div class="col-xs-6 col-lg-4">
					<div class="message-info-label">Status</div>
	                <div class="message-info-value"><xsl:value-of select="itemAttributes[key='Status']/values/EN"/></div>
				</div>

				<div class="col-xs-6 col-lg-4">
					<div class="message-info-label">Start date</div>
	                <div class="message-info-value"><xsl:value-of select="itemAttributes[key='StartDate']/values/_text"/></div>
				</div>
			</div>
		</div>
        
        <div id="assigneesDialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">
                            <span>&#215;</span>
                        </button>
                        <h4 class="modal-title">
                            <span class="right-spaced">Assigned to:</span>
                            <xsl:value-of select="itemAttributes[key='AssignedTo']/values/EN"/>
                        </h4>
                    </div>
                    <div class="modal-body">
                        <ul class="basic-list no-bottom-margin">
                            <xsl:for-each select="assignees/assignee">
                                <li><xsl:value-of select="."/></li>
                            </xsl:for-each>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

	</xsl:template>

</xsl:stylesheet>
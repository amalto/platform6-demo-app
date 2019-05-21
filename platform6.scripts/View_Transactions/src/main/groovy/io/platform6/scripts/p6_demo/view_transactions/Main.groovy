import java.text.DecimalFormat;

log.debug 'P6_DEMO'

DecimalFormat df = new DecimalFormat("###,###.00");

// Display pipeline variables
// pipeline.variables().each() { 
//     log.debug "PIPELINE VAR ${it}" 
// }

// Get and display raw Xml for test
def prettyXml = p6.pipeline.get 'xml'

// Get slurped xml
def transactionInfo = p6.pipeline.getXml 'xml'

// Return file extension
def get_file_extension = { String path -> path.substring(path.lastIndexOf('.') + 1) }

// Return file name
def get_file_name = { String path -> path.substring(path.lastIndexOf('/') + 1) } 

// Key
def Id = transactionInfo.Id.text()
def BusinessDocName = transactionInfo.TransactionType.text()
def BusinessDocNumber = transactionInfo.TransactionNumber.text()
def Endpoint = transactionInfo.DeliveredVia.text()
//def Message = messageInfo.Message.text()

// TransactionStatusInformation block start
def TechnicalStatusCode = transactionInfo.TechnicalStatusCode.text()
def TechnicalStatusDate = transactionInfo.TechnicalStatusDate.text()
def TechnicalStatusMessage = transactionInfo.TechnicalStatusMessage.text()


def FunctionalStatusCode = transactionInfo.FunctionalStatusCode.text()
def FunctionalStatusDate = transactionInfo.FunctionalStatusDate.text()
def FunctionalStatusMessage = transactionInfo.FunctionalStatusMessage.text()
// TransactionStatusInformation block end

def SourceDocumentURI = transactionInfo.SourceDocumentURI.text()
def srcDocExtension = get_file_extension(SourceDocumentURI)
def srcDocFileName = get_file_name(SourceDocumentURI)

def SourceDocumentContentType = transactionInfo.SourceDocumentContentType.text()

def CurrentDocumentURI = transactionInfo.TargetDocumentURI.text()
def curDocExtension = get_file_extension(CurrentDocumentURI)
def curDocFileName = get_file_name(CurrentDocumentURI)

def CurrentDocumentContentType = transactionInfo.TargetDocumentContentType.text()

def Attachments = transactionInfo.Attachments
def DisplayableAttachments = ""

Attachments.each {
    def URI = it.Attachment.URI.text()
    def ContentType = it.Attachment.ContentType.text()
    def attachmentExtension = get_file_extension(URI)
    def attachmentFileName = get_file_name(URI)
    def currentAttachment = """
    <div class="inline-block">
        <div class="message-info-label"><center>Attachment</center></div>
        <div class="message-info-value">

            <div class="message-info-file">

                <div onclick="platform6.transactionDownload(this)"
                    class="ft-icon ft-icon-${attachmentExtension}"
                    data-extension="${attachmentExtension}"
                    data-ids=${Id}
                    data-uri="$URI}"
                    data-content-type="${ContentType}"
                    data-view="{{VIEWNAME}}">
                </div>

                <xsl:variable name="attachmentFileName">
                    <xsl:call-template name="get-file-name">
                        <xsl:with-param name="path" select="URI"/>
                        </xsl:call-template>
                    </xsl:variable>

                <div class="message-info-file-name" title="${attachmentFileName}">
                    ${attachmentFileName}
                </div>
            </div>

        </div>
    </div>
    """
    
    DisplayableAttachments = "${DisplayableAttachments}${currentAttachment}"
}

// Will display info on Errors captured the "old" way
def Issue = transactionInfo.Issue
def IssueLevelERROR = Issue.findAll { it.level == 'ERROR' }
def errors = []

IssueLevelERROR.each {
    if (it.level == 'ERROR') {
        errors = tokenize( it.Text, '&#xA;' )
    }
}

def IssueErrorLine = ""

errors.each {
    def line = """
    <div class="mi-line">
        <strong><span class="message-info-value">${tokenize(it,'\\|')[1]}</span></strong> <span> - </span>
        <span class="message-info-value">${tokenize(it,'\\|')[2]}</span>
    </div>
    """
    
    IssueErrorLine = "${IssueErrorLine}${line}"
}

def IssueLevelErrorOldWay = (IssueLevelERROR.size() != 0) ? """
    <div class="col-xs-12 message-info-section">
        <div class="message-info-header">Invalid document due to <strong>${errors.size()} error(s)</strong></div>
        
        ${IssueErrorLine}

    </div>
""" : ""

// Will display info on Errors captured the "new" way
def Issues = transactionInfo.Issues
def IssuesLevelERROR = Issues.Issue.findAll { it.level == 'ERROR' }

def IssuesErrorLine = ""

IssuesLevelERROR.each {
    def line = """
        <div class="mi-line">
            <span class="message-info-value">• ${it.Message}</span>
        </div>
    """
    
    issuesLevelErrorNewWay = "${issuesLevelErrorNewWay}${line}"
}

def issuesLevelErrorNewWay = (IssuesLevelERROR.size() != 0) ? """
    <div class="col-xs-12 message-info-section">
        <div class="message-info-header">Content check has identified <strong>${IssuesLevelERROR.size()} error(s)</strong></div>
                    
        ${issuesLevelErrorNewWay}
                            
    </div>
""" : ""

def AuditTrail = transactionInfo.AuditTrail
def AudioTrailRecord = AuditTrail.Record ?: []
def AudioTrailRecordDisplay = ""

AudioTrailRecord.each {
    def auditType = (it.Type == "FunctionalStatus") ? "Functional status" : (it.Type == "TechnicalStatus") ? "Technical status" : ''
    def line = """
    <div class="row row-item-separated">

        <div class="col-xs-6 col-md-4 col-lg-1">
            <div class="message-info-label">Type</div>
            <div class="message-info-value">
                ${auditType}
            /div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Status</div>
            <div class="message-info-value">${it.Code}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-3">
            <div class="message-info-label">Date</div>
            <div class="message-info-value">${it.Date}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-6">
            <div class="message-info-label">Message</div>
            <div class="message-info-value">${it.Message}</div>
        </div>


    </div>
    """
    
    AudioTrailRecordDisplay = "${AudioTrailRecordDisplay}${line}"
}

def AudioTrailDisplay = (AudioTrailRecord.size() > 0) ? """
    <div class="row top-margin">
        <div class="col-xs-12 message-info-section">
            <div class="message-info-header" style="margin-bottom: 5px;">
                <button class="btn btn-xs btn-success" data-toggle="collapse" data-target="#auditTrail">Status history</button>
            </div>
            <div id="auditTrail" class="collapse side-padded"><div></div>
                ${AudioTrailRecordDisplay}
            </div>
        </div>
            
    </div>
""" : ""

def Workflow = transactionInfo.Workflow
def WorkflowInstance = transactionInfo.Workflow.Instance
def WorkflowInstanceDisplay = ""


WorkflowInstance.each {
    def line = """
    <div class="row row-item-separated">

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Workflow Type</div>
            <div class="message-info-value">${it.step.stepId}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Start Date</div>
            <div class="message-info-value">${it.startDate}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">End Date</div>
            <div class="message-info-value">${it.endDate}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Assigned to</div>
            <div class="message-info-value">${it.step.assignedToName}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Handled by</div>
            <div class="message-info-value">${it.step.actionedByEmail}</div>
        </div>

        <div class="col-xs-6 col-md-4 col-lg-2">
            <div class="message-info-label">Status</div>
            <div class="message-info-value">${it.status}</div>
        </div>
    </div>
    """
    
    WorkflowInstanceDisplay = "${WorkflowInstanceDisplay}${line}"
}

def WorkflowDisplay = (WorkflowInstance.size() > 0) ? """
    <div class="row top-margin">
        <div class="col-xs-12 message-info-section">
            <div class="message-info-header" style="margin-bottom: 5px;">
                <button class="btn btn-xs btn-success" data-toggle="collapse" data-target="#historyTrail">Workflow history</button>
            </div>
            <div id="historyTrail" class="collapse side-padded"><div></div>
                ${WorkflowInstanceDisplay}
            </div>
        </div>
            
    </div>
""" : ""

// KeyValue
def SellerName = transactionInfo.KeyValue.find{it.Key == "Seller Name"}.Value.text()
def BuyerName = transactionInfo.KeyValue.find{it.Key == "Buyer Name"}.Value.text()
def IssueDate = transactionInfo.KeyValue.find{it.Key == "Issue Date"}.Value.text()
def Currency = transactionInfo.KeyValue.find{it.Key == "Currency"}.Value.text()

def TotalAmount = transactionInfo.KeyValue.find{it.Key == "Total Amount"}.Value.text()

print "TOTAL AMOUNT" + TotalAmount
def FormattedTotalAmout = TotalAmount ? df.format(Double.valueOf(TotalAmount)) : ""

log.debug 'P6_DEMO FormattedTotalAmout = '+FormattedTotalAmout


def LineItems = transactionInfo.KeyValue.find{it.Key == "Line items"}.Value.text()

def TransactionStatusInformation = (TechnicalStatusCode != '')
    ? """
    <div class="row">
        <div class="col-xs-12 col-md-4">
            <div class="message-info-label">Technical status</div>
            <div class="message-info-value">${TechnicalStatusCode}</div>
        </div>
        <div class="col-xs-12 col-md-8">
            <div class="message-info-label">Associated message - as of ${TechnicalStatusDate}</div>
            <div class="message-info-value">${TechnicalStatusMessage}</div>
        </div>
    </div>
    """
    : """
    <div class="row">
        <div class="col-xs-12 col-md-4">
            <div class="message-info-label">Status</div>
            <div class="message-info-value"></div>
        </div>
        <div class="col-xs-12 col-md-8">
            <div class="message-info-label">Associated message</div>
            <div class="message-info-value"></div>
        </div>
    </div>
    """
    
log.debug 'P6DEMO View_Demo Transaction script LINE 306'  
    
def FunctionalStatusInformation = (FunctionalStatusCode != '')
    ? """
    <hr/>	
    <div class="row">

        <div class="col-xs-12 col-md-4">
            <div class="message-info-label">Functional status</div>
            <div class="message-info-value">${FunctionalStatusCode}</div>

        </div>

        <div class="col-xs-12 col-md-8">
        
            <div class="message-info-label">Associated message - as of ${FunctionalStatusDate}</div>
            <div class="message-info-value">${FunctionalStatusMessage}</div>
        </div>

    </div> 
    """
    : ''

// def my_html = """<div>TEST</div>"""

log.debug 'P6DEMO View_Demo Transaction script LINE 331'

def my_html = """
<div class="message-info-view-wrapper">

    <div class="row">
        <div class="col-xs-12 col-sm-6 message-info-section">
            <div class="message-info-header">Transaction: <strong><span class="right-spaced">${BusinessDocName}</span> ${BusinessDocNumber}</strong></div>

            <div class="row">

                <div class="col-xs-12 col-md-4">

                    <div class="message-info-label">Seller name</div>
                    <div class="message-info-value">${SellerName}</div>

                </div>

                <div class="col-xs-12 col-md-4">
                    
                    <div class="message-info-label">Buyer name</div>
                    <div class="message-info-value">${BuyerName}</div>

                </div>
            
                <div class="col-xs-12 col-md-4">
                    
                    <div class="message-info-label">Delivered via</div>
                    <div class="message-info-value">${Endpoint}</div>
                    
                </div>

            </div>
            
        </div>

        <div class="col-xs-12 xs-stacked-spacer col-sm-6 message-info-section">
            <div class="message-info-header">Additional information</div>

            <div class="row">

                
                    <div class="col-xs-12 col-md-4">
                        
                        <div class="message-info-label">Date</div>
                        <div class="message-info-value">${IssueDate}</div>

                    </div>
                
                    <div class="col-xs-12 col-md-4">
                        
                        <div class="message-info-label">Total Amount</div>
                            <div class="message-info-value">
                            <span class="right-spaced">${Currency}</span>
                            ${FormattedTotalAmout}
                        </div>
                        
                    </div>

                    <div class="col-xs-12 col-md-4">
                        
                        <div class="message-info-label">Line items</div>
                        <div class="message-info-value">${LineItems}</div>                                
                        
                    </div>

            </div>
            
        </div>


        
    </div>

    <div class="row top-margin">

        <div class="col-xs-12 xs-stacked-spacer col-sm-6 message-info-section">
            <div class="message-info-header">Transaction status information</div>
            
            
            ${TransactionStatusInformation} 
            
            ${FunctionalStatusInformation}
            
        </div>
        
        <div class="col-xs-12 col-sm-6 message-info-section">
            <div class="message-info-header">Documents and Attachment(s)</div>

            <div>

                <div class="inline-block">
                    <div class="message-info-label"><center>Source document</center></div>
                    <div class="message-info-value">
                        <div class="message-info-file">
                            <div onclick="platform6.transactionDownload(this)"
                                class="ft-icon ft-icon-${srcDocExtension}"
                                data-extension="${srcDocExtension}"
                                data-ids="${Id}"
                                data-uri="${SourceDocumentURI}"
                                data-content-type="${SourceDocumentContentType}"
                                data-view="{{VIEWNAME}}">
                            </div>

                            <div class="message-info-file-name" title="${srcDocFileName}">
                                ${srcDocFileName}
                            </div>
                        </div>
                    </div>
                </div>

                <div class="inline-block">
                    <div class="message-info-label"><center>Target document</center></div>
                    <div class="message-info-value">
                        <div class="message-info-file">

                            <div onclick="platform6.transactionDownload(this)"
                                class="ft-icon ft-icon-${curDocExtension}"
                                data-extension="${curDocExtension}"
                                data-ids="${Id}"
                                data-uri="${CurrentDocumentURI}"
                                data-content-type="${CurrentDocumentContentType}"
                                data-view="{{VIEWNAME}}">
                            </div>

                            <xsl:variable name="curDocFileName">
                                <xsl:call-template name="get-file-name">
                                    <xsl:with-param name="path" select="CurrentDocumentURI"/>
                                </xsl:call-template>
                            </xsl:variable>

                            <div class="message-info-file-name" title="${curDocFileName}">
                                ${curDocFileName}
                            </div>
                        </div>
                    </div>
                </div>
                

                ${DisplayableAttachments}

            </div>
            
        </div>


        <!-- Will display info on Errors captured the "old" way -->
        
        ${IssueLevelErrorOldWay} 

        <!-- Will display info on Errors captured the "new" way -->
        
        ${issuesLevelErrorNewWay}

    
    </div>

    ${AudioTrailDisplay}            
    
    ${WorkflowDisplay}             
    
    
</div>
"""

p6.pipeline.put 'portalHtml', my_html, 'text/html'

log.debug 'P6DEMO View_Demo Transaction script execution is OVER'
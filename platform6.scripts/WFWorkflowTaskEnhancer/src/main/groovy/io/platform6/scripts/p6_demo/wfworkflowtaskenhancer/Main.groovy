// This script copies information from the transaction to the workflow task.
// All variables inserted in the pipeline will appear as itemAttributes in the workflow task XML.

def dataType = p6.pipeline.get 'platform6.request.dataType'
def ids = p6.pipeline.get 'platform6.request.ids'

def ipk = p6.transaction.buildPK(dataType, ids)
def item = p6.transaction.get(ipk)

def transactionInfo = new XmlSlurper().parseText(item)

def transactionNumber = transactionInfo.TransactionNumber.text()
p6.pipeline.put 'TransactionNumber', transactionNumber, 'text/plain'

def transactionType = transactionInfo.TransactionType.text()
p6.pipeline.put 'TransactionType', transactionType, 'text/plain'

// Add common work item attributes required by the standard P6 Core 'Work Items' view
def stepXml = p6.pipeline.get '_wf_stepXml'
def statusMap = p6.workflow.localeText stepXml, "StatusLabels/Label[@name='" + p6.pipeline.get('_wf_statusId') + "']"
p6.pipeline.put 'Status', statusMap, 'application/p6core.i18n'

def assigneeMap = p6.workflow.localeText stepXml, 'Assignee/Label'
p6.pipeline.put 'AssignedTo', assigneeMap, 'application/p6core.i18n'
p6.pipeline.put 'StartDate', p6.pipeline.get('_wf_instanceStartDate'), 'text/plain'

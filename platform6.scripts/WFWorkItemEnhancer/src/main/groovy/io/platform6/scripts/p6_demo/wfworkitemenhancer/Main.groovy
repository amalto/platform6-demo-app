def concept = p6.pipeline.get 'platform6.request.dataType'
def ids = p6.pipeline.get 'platform6.request.ids'

def ipk = p6.transaction.buildPK(concept, ids)
def item = p6.transaction.get(ipk)

def transactionInfo = new XmlSlurper().parseText(item)

def transactionNumber = transactionInfo.TransactionNumber.text()
p6.pipeline.put 'TransactionNumber', transactionNumber, 'text/plain'

def transactionType = transactionInfo.TransactionType.text()
p6.pipeline.put 'TransactionType', transactionType, 'text/plain'

def endpoint = transactionInfo.Endpoint.text()
p6.pipeline.put 'Endpoint', endpoint, 'text/plain'

// Add common work item attributes required by the standard b2box 'Work Items' view
def stepXml = p6.pipeline.get '_wf_stepXml'
def statusMap = p6.workflow.localeText stepXml, "StatusLabels/Label[@name='" + p6.pipeline.get('_wf_statusId') + "']"
p6.pipeline.put 'Status', statusMap, 'application/b2box.i18n'

def assigneeMap = workflow.localeText stepXml, 'Assignee/Label'
p6.pipeline.put 'AssignedTo', assigneeMap, 'application/b2box.i18n'
p6.pipeline.put '_ASSIGNEDTONAME', pipeline.get('_wf_assignedToId'), 'text/plain'
p6.pipeline.put 'StartDate', p6.pipeline.get('_wf_instanceStartDate'), 'text/plain'

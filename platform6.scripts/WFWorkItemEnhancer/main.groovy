def concept = pipeline.get 'platform6.request.concept'
def ids = pipeline.get 'platform6.request.ids'

def ipk = transaction.buildPK(concept, ids)
def item = transaction.get(ipk)

def transactionInfo = new XmlSlurper().parseText(item)

def transactionNumber = transactionInfo.TransactionNumber.text()
pipeline.put 'TransactionNumber', transactionNumber, 'text/plain'

def transactionType = transactionInfo.TransactionType.text()
pipeline.put 'TransactionType', transactionType, 'text/plain'

def endpoint = transactionInfo.Endpoint.text()
pipeline.put 'Endpoint', endpoint, 'text/plain'

// Add common work item attributes required by the standard b2box 'Work Items' view
def stepXml = pipeline.get '_wf_stepXml'
def statusMap = workflow.localeText stepXml, "StatusLabels/Label[@name='" + pipeline.get('_wf_statusId') + "']"
pipeline.put 'Status', statusMap, 'application/b2box.i18n'

def assigneeMap = workflow.localeText stepXml, 'Assignee/Label'
pipeline.put 'AssignedTo', assigneeMap, 'application/b2box.i18n'
pipeline.put '_ASSIGNEDTONAME', pipeline.get('_wf_assignedToId'), 'text/plain'
pipeline.put 'StartDate', pipeline.get('_wf_instanceStartDate'), 'text/plain'

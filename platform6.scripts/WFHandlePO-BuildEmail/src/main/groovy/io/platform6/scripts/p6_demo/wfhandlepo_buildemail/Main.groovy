import java.net.URLEncoder

p6.pipeline.put 'Subject', 'Platform 6 Demo App - Purchase Order to review'

p6.pipeline.put 'From', 'no-reply@platform6.io'

def instanceId = p6.configuration.get("instance.id")

def dataType = p6.pipeline.get 'platform6.request.dataType'
def itemIds = p6.pipeline.get 'platform6.request.ids'

def itemPk = p6.transaction.buildPK(dataType, itemIds)

def transactionInfoContent = p6.transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

def transactionId = transactionInfo.Id.text()

def portalBaseURL = 'portal.platform6.io'
if ('dev'.equals(instanceId)) {
    portalBaseURL = 'dev.portal.amalto.com'
}

def htmlLink = "https://$portalBaseURL/#/edit-transaction/$INSTANCE_ID/p6_demo.Transactions/"+URLEncoder.encode('["'+transactionId+'"]', 'utf-8')
p6.pipeline.put 'href', htmlLink, 'text/plain'

def docNumber = transactionInfo.BusinessDocNumber.text()
def totalAmount = transactionInfo.KeyValue.find{it.Key == 'Total Amount'}.Value.text()

if (docNumber == null) {
    docNumber = ''
}

if (totalAmount == null) {
    totalAmount = ''
}


p6.pipeline.put 'docNumber', docNumber, 'text/plain'
p6.pipeline.put 'totalAmount', totalAmount, 'text/plain'

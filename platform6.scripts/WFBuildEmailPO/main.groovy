import java.net.URLEncoder

pipeline.put 'Subject', 'Platform 6 Demo App - Purchase Order to review'

pipeline.put 'From', 'no-reply@platform6.io'

def INSTANCE_ID = configuration.get("applicationid")

def concept = pipeline.get 'platform6.request.concept'
def itemIds = pipeline.get 'platform6.request.ids'

def itemPk = transaction.buildPK(concept, itemIds)

def transactionInfoContent = transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

def transactionId = transactionInfo.Id.text()

def portalBaseURL = 'portal.platform6.io'
if ('dev'.equals(INSTANCE_ID)) {
    portalBaseURL = 'dev.portal.amalto.com'
}

def htmlLink = "https://$portalBaseURL/#/edit-transaction/$INSTANCE_ID/p6_demo.Transactions/"+URLEncoder.encode('["'+transactionId+'"]', 'utf-8')
pipeline.put 'href', htmlLink, 'text/plain'

def docNumber = transactionInfo.BusinessDocNumber.text()
def totalAmount = transactionInfo.KeyValue.find{it.Key == 'Total Amount'}.Value.text()

if (docNumber == null) {
    docNumber = ''
}

if (totalAmount == null) {
    totalAmount = ''
}


pipeline.put 'docNumber', docNumber, 'text/plain'
pipeline.put 'totalAmount', totalAmount, 'text/plain'

import groovy.json.*
import java.text.SimpleDateFormat

def concept = pipeline.get 'platform6.request.concept'
def itemIds = pipeline.get 'platform6.request.ids'

def itemPk = transaction.buildPK(concept, itemIds)

def transactionInfoContent = transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

def currentDate = new Date()
SimpleDateFormat transaction_sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss")

def formatedDate = transaction_sdf.format(currentDate)

def reason = pipeline.get 'reason'
transactionInfo.FunctionalStatusCode = 'Rejected'
transactionInfo.FunctionalStatusMessage = reason
transactionInfo.FunctionalStatusDate = formatedDate

transactionInfo.TechnicalStatusCode = 'Handled'
transactionInfo.TechnicalStatusMessage = ''
transactionInfo.TechnicalStatusDate = formatedDate

def transactionId = transactionInfo.Id.text()

def ipk = transaction.buildPK('TransactionInfo', transactionId)

transaction.p6projectAndRoute(groovy.xml.XmlUtil.serialize(transactionInfo), 'p6_demo.TransactionInfo', ipk, 'direct:p6router.p6_demo_Dispatcher')

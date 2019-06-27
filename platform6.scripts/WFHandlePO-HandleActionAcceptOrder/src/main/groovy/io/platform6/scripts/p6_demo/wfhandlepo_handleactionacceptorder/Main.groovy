import groovy.json.*
import java.text.SimpleDateFormat

def dataType = p6.pipeline.get 'platform6.request.dataType'
def itemIds = p6.pipeline.get 'platform6.request.ids'

def itemPk = p6.transaction.buildPK(dataType, itemIds)

def transactionInfoContent = p6.transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

def currentDate = new Date()
SimpleDateFormat transaction_sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss")

def formatedDate = transaction_sdf.format(currentDate)

transactionInfo.FunctionalStatusCode = 'Accepted'
transactionInfo.FunctionalStatusMessage = ''
transactionInfo.FunctionalStatusDate = formatedDate

transactionInfo.TechnicalStatusCode = 'Handled'
transactionInfo.TechnicalStatusMessage = ''
transactionInfo.TechnicalStatusDate = formatedDate


def salesordernumber = p6.pipeline.get 'salesordernumber'

if (salesordernumber!=null && !''.equals(salesordernumber)) {
    
    int nbKeyValues = transactionInfo.KeyValue.size()
    transactionInfo.KeyValue[nbKeyValues-1] + {
             KeyValue {
            Key("Sales Order Number")
            Value(salesordernumber)
        }
    }
}

def transactionId = transactionInfo.Id.text()

def ipk = p6.transaction.buildPK('TransactionInfo', transactionId)

p6.transaction.saveAndRoute(groovy.xml.XmlUtil.serialize(transactionInfo), 'p6_demo.TransactionInfo', ipk, 'direct:p6router.p6_demo_Dispatcher')

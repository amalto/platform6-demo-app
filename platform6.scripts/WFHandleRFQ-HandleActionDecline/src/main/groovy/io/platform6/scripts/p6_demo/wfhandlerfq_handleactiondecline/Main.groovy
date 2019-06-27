/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import java.time.format.DateTimeFormatter
import java.util.UUID


def helper = new RFQSmartContractHelper(this)

def dataType = p6.pipeline.get 'platform6.request.dataType'
def rfqId = p6.pipeline.get 'platform6.request.ids'
def reason = p6.pipeline.get 'reason'

def now = helper.now()

// Fetch the RFQ transaction info
def rfqPk = p6.transaction.buildPK(dataType, rfqId)
def transactionInfoContent = p6.transaction.get(rfqPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

// Decline RFQ locally
def formattedDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss").format(now)

transactionInfo.FunctionalStatusCode = 'Declined'
transactionInfo.FunctionalStatusMessage = reason
transactionInfo.FunctionalStatusDate = formattedDate

transactionInfo.TechnicalStatusCode = 'Handled'
transactionInfo.TechnicalStatusMessage = ''
transactionInfo.TechnicalStatusDate = formattedDate

p6.transaction.save(groovy.xml.XmlUtil.serialize(transactionInfo), 'p6_demo.TransactionInfo', rfqPk)

// Decline RFQ on the blockchain
def declineId = UUID.randomUUID()
helper.declineRFQ(declineId, UUID.fromString(rfqId), now)

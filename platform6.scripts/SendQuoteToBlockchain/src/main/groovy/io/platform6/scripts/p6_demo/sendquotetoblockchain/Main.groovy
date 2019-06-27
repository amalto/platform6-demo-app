/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import java.time.format.DateTimeFormatter
import java.util.UUID
import org.apache.commons.io.FileUtils


def helper = new RFQSmartContractHelper(this)

def dataType = p6.pipeline.get 'platform6.request.dataType'
def quoteIdString = p6.pipeline.get 'platform6.request.ids'

// Fetch the quote transaction info
def quotePk = p6.transaction.buildPK(dataType, quoteIdString)
def transactionInfoContent = p6.transaction.get(quotePk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

def srcFile = p6.uri.fileFromUrl(transactionInfo.SourceDocumentURI.text())
String quoteContent = FileUtils.readFileToString(srcFile)

// Update transaction info status
def now = helper.now()
def formattedDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss").format(now)

transactionInfo.TechnicalStatusCode = 'Sent'
transactionInfo.TechnicalStatusMessage = ''
transactionInfo.TechnicalStatusDate = formattedDate

p6.transaction.save(groovy.xml.XmlUtil.serialize(transactionInfo), 'p6_demo.TransactionInfo', quotePk)

// Write quote in the blockchain
def quoteId = UUID.fromString(quoteIdString)

def quoteXML = slurper.parseText(quoteContent)
def rfqId = UUID.fromString(quoteXML.RequestForQuotationDocumentReference.UUID.text())

helper.submitQuote(quoteId, rfqId, now, quoteContent)

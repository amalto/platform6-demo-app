/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import java.time.format.DateTimeFormatter


def helper = new RFQSmartContractHelper(this)

def topics = pipeline.get('logTopics').split(',')
def eventHash = topics[0]
def data = pipeline.get('logData')

if (eventHash == RFQSmartContractHelper.RFQReceivedEventHash) {
    def (rfqId, issuedAt, ubl) = helper.readRFQReceivedEvent(topics, data)

    pipeline.put("ubl", ubl)
    control.call 'p6_demo.ReceiveRFQTransaction'
}
else if (eventHash == RFQSmartContractHelper.RFQDeclinedEventHash) {
    def (supplier, rfqId, quoteId, issuedAt) = helper.readRFQDeclinedEvent(topics, data)

    log.info "RFQ " + rfqId + " was declined."
}
else if (eventHash == RFQSmartContractHelper.QuoteReceivedEventHash) {
    def (supplier, rfqId, quoteId, issuedAt, ubl) = helper.readQuoteReceivedEvent(topics, data)

    pipeline.put("ubl", ubl)
    control.call 'p6_demo.ReceiveQuoteTransaction'
}

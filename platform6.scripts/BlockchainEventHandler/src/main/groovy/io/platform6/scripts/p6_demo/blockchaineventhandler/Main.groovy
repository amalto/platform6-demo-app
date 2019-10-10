/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

def helper = new RFQSmartContractHelper(this)

def topics = p6.pipeline.get('logTopics').split(',')
def eventHash = topics[0]
def data = p6.pipeline.get('logData')

if (eventHash == RFQSmartContractHelper.RFQReceivedEventHash) {
    def (rfqId, issuedAt, ubl) = helper.readRFQReceivedEvent(topics, data)

    p6.pipeline.put("ubl", ubl)
    p6.script.call 'p6_demo.ReceiveRFQTransaction'
}
else if (eventHash == RFQSmartContractHelper.RFQDeclinedEventHash) {
    def (supplier, rfqId, quoteId, issuedAt) = helper.readRFQDeclinedEvent(topics, data)

    log.info "RFQ " + rfqId + " was declined."
}
else if (eventHash == RFQSmartContractHelper.QuoteReceivedEventHash) {
    def (supplier, rfqId, quoteId, issuedAt, ubl) = helper.readQuoteReceivedEvent(topics, data)

    p6.pipeline.put("ubl", ubl)
    p6.script.call 'p6_demo.HandleQuoteTransaction'
}

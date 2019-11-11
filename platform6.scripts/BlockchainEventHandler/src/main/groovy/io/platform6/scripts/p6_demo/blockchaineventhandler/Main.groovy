/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.web3j.protocol.core.methods.response.Log


def helper = new RFQSmartContractHelper(this)

def mapper = new ObjectMapper();
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

def ethLog = mapper.readerFor(Log.class).readValue(p6.pipeline.get('body'))
def topics = ethLog.getTopics()
def eventHash = topics[0]
def data = ethLog.getData()

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

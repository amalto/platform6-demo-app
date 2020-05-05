import com.fasterxml.jackson.databind.ObjectMapper

import io.platform6.demo.sc.RequestForQuotations

import java.time.*

import org.web3j.abi.EventEncoder
import org.web3j.abi.EventValues
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.*
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.*
import org.web3j.utils.Numeric


enum RFQStatus {
    Received, Declined, QuoteProvided
}

enum QuoteStatus {
    Offer, Decline
}

class RFQSmartContractHelper {

    // !!! For demo purposes, don't do this in production! Always ask the user for his/her password or store your private key in a vault/HD wallet!
    static def password = "ADummyPassword"

    // Helper state
    def context
    def web3j
    def smartContract

    RFQSmartContractHelper(def context) {
        this.context = context

        def ethClientURL  = context.p6.appconfig.get('ethClientURL')
        this.web3j = context.p6.web3ethereum.build(ethClientURL)

        // Define a custom transaction manager with a polling frequency of 2 seconds
        def transactionManager = context.p6.web3ethereum.pollingTransactionManager(web3j, readCredentials(), 2000L)

        def contractAddress = context.p6.appconfig.get('contractAddress')
        this.smartContract = RequestForQuotations.load(contractAddress, web3j, transactionManager, context.p6.web3ethereum.DEFAULT_GAS_PROVIDER)
    }

    Credentials readCredentials() {
        return context.p6.web3ethereum.getCredentials(context.p6.appconfig.get("demoWallet"), password)
    }

    TransactionReceipt submitRFQ(UUID id, ZonedDateTime now, String ubl) {
        return smartContract.submitRFQ(
            context.p6.conversion.getBytesFromUUID(id),
            context.p6.conversion.zdtToBigInteger(now),
            ubl).send()
    }

    Tuple getRFQ(UUID id) {
        def result = smartContract.getRFQ(context.p6.conversion.getBytesFromUUID(id)).send()

        def issuedAt = context.p6.conversion.bigIntegerToZDT(result.getValue1())
        def ubl = result.getValue2()
        def status = RFQStatus.values()[result.getValue3().intValue()]

        return new Tuple(issuedAt, ubl, status)
    }

    TransactionReceipt declineRFQ(UUID declineId, UUID rfqId, ZonedDateTime now) {
        return smartContract.declineRFQ(
            context.p6.conversion.getBytesFromUUID(declineId),
            context.p6.conversion.getBytesFromUUID(rfqId),
            context.p6.conversion.zdtToBigInteger(now)).send()
    }

    Tuple getQuote(UUID id) {
        def result = smartContract.getQuote(context.p6.conversion.getBytesFromUUID(id)).send()

        def issuedAt = context.p6.conversion.bigIntegerToZDT(result.getValue1())
        def ubl = result.getValue2()
        def status = QuoteStatus.values()[result.getValue3().intValue()]
        def rfqId = context.p6.conversion.getUUIDFromBytes(result.getValue4())
        def supplierAddress = result.getValue5()

        return new Tuple(issuedAt, ubl, status, rfqId, supplierAddress)
    }

    TransactionReceipt submitQuote(UUID id, UUID rfqId, ZonedDateTime now, String ubl) {
        return smartContract.submitQuote(
            context.p6.conversion.getBytesFromUUID(id),
            context.p6.conversion.getBytesFromUUID(rfqId),
            context.p6.conversion.zdtToBigInteger(now), ubl).send()
    }

    // Read events

    final static String RFQReceivedEventHash = EventEncoder.encode(RequestForQuotations.RFQRECEIVED_EVENT)

    Tuple readRFQReceivedEvent(List<String> logTopics, String logData) {
        def event = extractEvent(RequestForQuotations.RFQRECEIVED_EVENT, logTopics, logData)

        def rfqId = context.p6.conversion.getUUIDFromBytes((byte[]) event.getNonIndexedValues().get(0).getValue())
        def issuedAt = context.p6.conversion.bigIntegerToZDT((BigInteger) event.getNonIndexedValues().get(1).getValue())
        def ubl = (String) event.getNonIndexedValues().get(2).getValue()

        return new Tuple(rfqId, issuedAt, ubl)
    }

    final static String RFQDeclinedEventHash = EventEncoder.encode(RequestForQuotations.RFQDECLINED_EVENT)

    Tuple readRFQDeclinedEvent(List<String> logTopics, String logData) {
        def event = extractEvent(RequestForQuotations.RFQDECLINED_EVENT, logTopics, logData)

        def supplier = (String) event.getIndexedValues().get(0).getValue()
        def rfqId = context.p6.conversion.getUUIDFromBytes((byte[]) event.getNonIndexedValues().get(0).getValue())
        def quoteId = context.p6.conversion.getUUIDFromBytes((byte[]) event.getNonIndexedValues().get(1).getValue())
        def issuedAt = context.p6.conversion.bigIntegerToZDT((BigInteger) event.getNonIndexedValues().get(2).getValue())

        return new Tuple(supplier, rfqId, quoteId, issuedAt)
    }

    final static String QuoteReceivedEventHash = EventEncoder.encode(RequestForQuotations.QUOTERECEIVED_EVENT)

    Tuple readQuoteReceivedEvent(List<String> logTopics, String logData) {
        def event = extractEvent(RequestForQuotations.QUOTERECEIVED_EVENT, logTopics, logData)

        def supplier = (String) event.getIndexedValues().get(0).getValue()
        def rfqId = context.p6.conversion.getUUIDFromBytes((byte[]) event.getNonIndexedValues().get(0).getValue())
        def quoteId = context.p6.conversion.getUUIDFromBytes((byte[]) event.getNonIndexedValues().get(1).getValue())
        def issuedAt = context.p6.conversion.bigIntegerToZDT((BigInteger) event.getNonIndexedValues().get(2).getValue())
        def ubl = (String) event.getNonIndexedValues().get(3).getValue()

        return new Tuple(supplier, rfqId, quoteId, issuedAt, ubl)
    }

    // Event helper methods

    static EventValues extractEvent(Event event, List<String> logTopics, String logData) {
        String encodedEventSignature = EventEncoder.encode(event)

        if (logTopics[0] != encodedEventSignature) {
            return null
        }

        List<Type> indexedValues = new ArrayList<>()
        List<Type> nonIndexedValues = FunctionReturnDecoder.decode(logData, event.getNonIndexedParameters())

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters()
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(logTopics[i + 1], indexedParameters.get(i))
            indexedValues.add(value)
        }

        return new EventValues(indexedValues, nonIndexedValues)
    }

    static generateWalletJSONFromPrivateKey(String privateKeyInHex, String password) {
        def privateKey = new BigInteger(Numeric.cleanHexPrefix(privateKeyInHex), 16)
        def keyPair = ECKeyPair.create(privateKey)
        def walletFile = Wallet.createStandard(password, keyPair)
        def objectMapper = new ObjectMapper()
        return objectMapper.writeValueAsString(walletFile)
    }

    // Date / Time helper functions

    static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.systemDefault())
    }
}

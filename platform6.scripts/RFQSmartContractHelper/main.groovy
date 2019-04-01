import io.platform6.demo.sc.RequestForQuotations

import org.web3j.abi.EventEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName


def helper = new RFQSmartContractHelper(this)

// Test protecting private key with a password in a wallet
def password = "ADummyPassword"
def privateKey = "0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3"
log.debug "Wallet file " + helper.generateWalletJSONFromPrivateKey(privateKey, password)

// Test reading credentials
def credentials = helper.readCredentials()
log.debug "Account address is: " + credentials.getAddress()
log.debug "Account private key is: 0x" + credentials.getEcKeyPair().getPrivateKey().toString(16)

// Test Eth client connection
def contract = helper.getSmartContract()
log.debug "Get number of RFQs"
def nbrOfRFQs = contract.nbrOfRFQs().send()
log.debug "Current number of RFQs is: " + nbrOfRFQs

log.debug "Get number of quotes"
def nbrOfQuotes = contract.nbrOfQuotes().send()
log.debug "Current number of quotes is: " + nbrOfQuotes

// Test RFQ submission
def rfqId = UUID.randomUUID()
log.debug "RFQ id is " + rfqId

def now = helper.now()
def ubl = "<...>"

def receipt = helper.submitRFQ(rfqId, now, ubl)
log.debug receipt.toString()

def (readIssuedAt, readUBL, readStatus) = helper.getRFQ(rfqId)
log.debug "RFQ is issued at: " + readIssuedAt
log.debug "RFQ UBL is: " + readUBL
log.debug "RFQ status is: " + readStatus

// Read RFQReceived events
//def topicOfRFQReceived = Hash.sha3String("RFQReceived(bytes16,uint256,string)")
log.debug "Topic of RFQReceived: " + helper.RFQReceivedEventHash

// Test declining a RFQ
def declineId = UUID.randomUUID()
receipt = helper.declineRFQ(declineId, rfqId, now)
log.debug receipt.toString()

def (readDeclineIssuedAt, readDeclineUBL, readDeclineStatus, readDeclineRFQId, readDeclineSupplierAddress) = helper.getQuote(declineId)
log.debug "Decline is issued at: " + readDeclineIssuedAt
log.debug "Decline UBL is: " + readDeclineUBL // should be empty
log.debug "Decline status is: " + readDeclineStatus // should be Decline
log.debug "Declined RFQ is: " + readDeclineRFQId // should be rfqId
log.debug "Decline supplier address is: " + readDeclineSupplierAddress

def readDeclinedRFQStatus = helper.getRFQ(rfqId)[2]
log.debug "Declined RFQ status is: " + readDeclinedRFQStatus // should be Declined

// Test submitting a quote
def quoteId = UUID.randomUUID()
def quoteUbl = "<quote />"
receipt = helper.submitQuote(quoteId, rfqId, now, quoteUbl)
log.debug receipt.toString()

def (readQuoteIssuedAt, readQuoteUBL, readQuoteStatus, readQuoteRFQId, readQuoteSupplierAddress) = helper.getQuote(quoteId)
log.debug "Quote is issued at: " + readQuoteIssuedAt
log.debug "Quote UBL is: " + readQuoteUBL
log.debug "Quote status is: " + readQuoteStatus // should be Offer
log.debug "Quote RFQ is: " + readQuoteRFQId // should be rfqId
log.debug "Quote supplier address is: " + readQuoteSupplierAddress

def readRFQStatus = helper.getRFQ(rfqId)[2]
log.debug "New RFQ status is: " + readRFQStatus // should be QuoteProvided

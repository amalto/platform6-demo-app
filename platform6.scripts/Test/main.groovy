//bundled.deploy('p6_demo.POReview')
//bundled.deploy('p6_demo.DemoSmartContract')

// Get the ClassLoader class
//def cl = ClassLoader.getSystemClassLoader() // Throws SandboxException: b2box/Groovy: Use of the Classloader class not allowed
//def clazz = cl.getClass()

// Get the protected addURL method from the parent URLClassLoader class
//def method = clazz.getSuperclass().getDeclaredMethod("addURL", [ URL.class ] as Class[])

// Run projected addURL method to add JAR to classpath
//method.setAccessible(true);

//def jar = new URL("file:///$B2BOX_DATA/lib/demo-app-smart-contract-1.0.jar")
//method.invoke(cl, [ jar ] as Object [])

/*XmlSlurper slurper = new XmlSlurper()

def quoteUBL = """<?xml version="1.0" encoding="UTF-8"?>
<Quotation xmlns="urn:oasis:names:specification:ubl:schema:xsd:Quotation-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
  <cbc:UBLVersionID>2.0</cbc:UBLVersionID>
  <cbc:CustomizationID>OIOUBL-2.1</cbc:CustomizationID>
  <cbc:ProfileID schemeAgencyID="320" schemeID="urn:oioubl:id:profileid-1.2">Procurement-QuoSim-1.0</cbc:ProfileID>
  <cbc:ID>QIY7655</cbc:ID>
  <cbc:CopyIndicator>false</cbc:CopyIndicator>
  <cbc:UUID>4D07786B-DA6D-439F-82D1-6FFFC7F4E3B1</cbc:UUID>
  <cbc:Note languageID="da-dk">Bestilling af computere</cbc:Note>
  <cac:RequestForQuotationDocumentReference>
    <cbc:ID>G867B</cbc:ID>
    <cbc:UUID>4D07786B-DA6D-439F-82D1-6FFFC7F4E3B1</cbc:UUID>
  </cac:RequestForQuotationDocumentReference>
</Quotation>
"""

def quoteXML = slurper.parseText(quoteUBL)
def rfqId = UUID.fromString(quoteXML.RequestForQuotationDocumentReference.UUID.text())
log.debug rfqId.toString()*/

//def ethClientURL = "http://host.docker.internal:9545"
/*import com.fasterxml.jackson.databind.ObjectMapper
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.crypto.*

def ethClientURL = "http://172.13.0.20:8545"
def web3j = ethereumrpc.build(ethClientURL)

// Test account (private key: 0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3)
def account = "0x627306090abaB3A6e1400e9345bC60c78a8BEf57"

def password = "ADummyPassword"
def privateKey = "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3"
def privateKeyBigInteger = new BigInteger(privateKey, 16)
def keyPair = ECKeyPair.create(privateKeyBigInteger)
def walletFile = Wallet.createStandard(password, keyPair)
def objectMapper = new ObjectMapper()
log.debug "Wallet file " + objectMapper.writeValueAsString(walletFile) // outputs the wallet file as JSON*/

// email.sendEmail("emailing@amalto.net", "choucri.fahed@platform6.io", "Hello World!")

/*import io.platform6.demo.sc.RequestForQuotations
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.*
import org.web3j.tx.response.PollingTransactionReceiptProcessor

def ethClientURL = "http://172.13.0.20:8545"
def web3j = ethereumrpc.build(ethClientURL)
def credentials = ethereumrpc.getCredentials("p6_demo.AppConfig", [key: "demo.wallet"], "value", "ADummyPassword")

log.debug "Credentials: " + credentials

def processor = new PollingTransactionReceiptProcessor(web3j, 2000L, TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH)
def tm = new RawTransactionManager(web3j, credentials, ChainId.NONE, processor)

def before = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance()
def contract = RequestForQuotations.deploy(web3j, tm, ethereumrpc.DEFAULT_GAS_PRICE, ethereumrpc.DEFAULT_GAS_LIMIT).send()
// Before 0xf12b5dd4ead5f743c6baa640b0216200e89b60da
log.debug "Contract: " + contract.getContractAddress()

def after = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance()
log.debug "Balance:" + after
log.debug "Diff:" + (before - after)*/

def t  = table.lookup('p6_demo.AppConfig', [key: 'ethClientURL']).value[0]

log.debug "hey " + t

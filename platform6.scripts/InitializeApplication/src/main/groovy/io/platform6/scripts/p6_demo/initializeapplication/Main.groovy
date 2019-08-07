// This script is launched when the application is installed on the instance


import io.platform6.demo.sc.RequestForQuotations

import org.web3j.tx.*
import org.web3j.tx.response.PollingTransactionReceiptProcessor


// Deploy (copy to local file system) bundled resources
p6.bundled.deploy('p6_demo.DemoSmartContract')
p6.bundled.deploy('p6_demo.POReview')
p6.bundled.deploy('p6_demo.TableItemsData')
log.debug 'Loaded bundled resources'

// Populate the Items table from the provided CSV file
String P6_TMP = System.getenv('B2BOX_TMP')
def csvFile = [ skipLines: 0, separator: ',', useFirstLineHeaders: true, uri: 'file:///' + P6_TMP + '/p6_demo_items.csv']

p6.csv.parse(csvFile) { row ->
    def records = new ArrayList()
    records[0] = row
    table.upsert('p6_demo.Items', records)
    true
}
log.debug 'Populated the Items table from the CSV file'

// Save wallet to interact with smart contract
p6.appconfig.save("p6_demo", [
    'demoWallet': '{"address":"627306090abab3a6e1400e9345bc60c78a8bef57","id":"c913867f-e5e4-4be6-a7da-654ce833bfb4","version":3,"crypto":{"cipher":"aes-128-ctr","ciphertext":"4c0604cca3314838d63847b4ed02e7a2d17e511efd44f6050e09c238a5d50095","cipherparams":{"iv":"b48d817320ed5b459bbddf140e5f0453"},"kdf":"scrypt","kdfparams":{"dklen":32,"n":262144,"p":1,"r":8,"salt":"35d5339acf6a8f15462ffc0bb981cf319794ae239c51341fb854c18ee458d5c7"},"mac":"76c1a47f7124cd63460ad25fbb0b9c6e3eab8ed50d36f66e2dc40c038881123e"}}'
])

// Deploy the smart contract
def ethClientURL = "http://172.13.0.20:8545"
def web3j = p6.ethereumrpc.build(ethClientURL)
def credentials = p6.ethereumrpc.getCredentials("p6_demo.AppConfig", [key: "demoWallet"], "value", "ADummyPassword")

// Define a custom transaction manager with a polling frequency of 2 seconds
def processor = new PollingTransactionReceiptProcessor(web3j, 2000L, TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH)
def tm = new RawTransactionManager(web3j, credentials, ChainIdLong.NONE, processor)
def contract = RequestForQuotations.deploy(web3j, tm, p6.ethereumrpc.DEFAULT_GAS_PROVIDER).send()

// Save the contract address and Ethereum client URL in config table
p6.appconfig.save("p6_demo", [
    "ethClientURL": ethClientURL,
    "contractAddress": contract.getContractAddress(),
])

// Restart services to activate routes and workflow steps
p6.service.restartService('platform6.workflowsteps')
p6.service.restartService('platform6.routes')
p6.service.restartService('platform6.views')
p6.service.restartService('platform6.transactions')
log.debug 'Services restarted to loads routes and workflow steps'

log.debug 'Demo app initialized successfully!'

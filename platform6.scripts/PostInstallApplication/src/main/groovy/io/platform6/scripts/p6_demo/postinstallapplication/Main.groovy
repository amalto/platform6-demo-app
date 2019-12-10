// This script is launched when the application is installed on the instance
import io.platform6.demo.sc.RequestForQuotations

import org.web3j.tx.*

// Populate the Items table from the provided CSV file
def csvFile = [ skipLines: 0, separator: ',', useFirstLineHeaders: true, uri: 'file://${TMP}/p6_demo_items.csv' ]

p6.csv.parse(csvFile) { row ->
    def records = new ArrayList()
    records[0] = row
    p6.table.upsert('p6_demo.Items', records)
    true
}
log.debug 'Populated the Items table from the CSV file'

// Deploy the smart contract
def web3j = p6.ethereumrpc.build(p6.appconfig.get("ethClientURL"))
def credentials = p6.ethereumrpc.getCredentials(p6.appconfig.get("demoWallet"), "ADummyPassword")

// Define a custom transaction manager with a polling frequency of 2 seconds
def tm = p6.ethereumrpc.pollingTransactionManager(web3j, credentials, 2000L)
def contract = RequestForQuotations.deploy(web3j, tm, p6.ethereumrpc.DEFAULT_GAS_PROVIDER).send()

// Save the contract address and Ethereum client URL in config table
p6.appconfig.override("contractAddress", contract.getContractAddress())

// Notify P6 that installation was successful
log.debug 'Demo app initialized successfully!'
p6.pipeline.put("installScriptResult", "true")

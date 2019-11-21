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

// Save wallet to interact with smart contract
p6.appconfig.save([
    'demoWallet': '{"address":"627306090abab3a6e1400e9345bc60c78a8bef57","id":"c913867f-e5e4-4be6-a7da-654ce833bfb4","version":3,"crypto":{"cipher":"aes-128-ctr","ciphertext":"4c0604cca3314838d63847b4ed02e7a2d17e511efd44f6050e09c238a5d50095","cipherparams":{"iv":"b48d817320ed5b459bbddf140e5f0453"},"kdf":"scrypt","kdfparams":{"dklen":32,"n":262144,"p":1,"r":8,"salt":"35d5339acf6a8f15462ffc0bb981cf319794ae239c51341fb854c18ee458d5c7"},"mac":"76c1a47f7124cd63460ad25fbb0b9c6e3eab8ed50d36f66e2dc40c038881123e"}}'
])

// Deploy the smart contract
def ethClientURL = "http://172.13.0.20:8545"
def web3j = p6.ethereumrpc.build(ethClientURL)
def credentials = p6.ethereumrpc.getCredentials("p6_demo.AppConfig", [key: "demoWallet"], "value", "ADummyPassword")

// Define a custom transaction manager with a polling frequency of 2 seconds
def tm = p6.ethereumrpc.pollingTransactionManager(web3j, credentials, 2000L)
def contract = RequestForQuotations.deploy(web3j, tm, p6.ethereumrpc.DEFAULT_GAS_PROVIDER).send()

// Save the contract address and Ethereum client URL in config table
p6.appconfig.save([
    "ethClientURL": ethClientURL,
    "contractAddress": contract.getContractAddress(),
])

// Notify P6 that installation was successful
log.debug 'Demo app initialized successfully!'
p6.pipeline.put("installScriptResult", "true")

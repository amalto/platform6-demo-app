def ethClientURL = appconfig.get('p6_demo', 'ethClientURL')
def contractAddress =  appconfig.get('p6_demo', 'contractAddress')

camel.getCtx().addRoutes(new RouteBuilder() {
    void configure() {
        from("p6web3j://" + ethClientURL + "?address=" + contractAddress + "&operation=ETH_LOG_OBSERVABLE")
            .to("p6cmb://scripts?platform6.request.action=execute&id=p6_demo.BlockchainEventListener")
            .routeId("p6_demo Web3j - BlockchainEventListener")
            .description("p6_demo Blockchain Event Listener")
    }
})

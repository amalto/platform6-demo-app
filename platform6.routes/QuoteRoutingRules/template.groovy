camel.getCtx().addRoutes(new RouteBuilder() {
    
    void configure() {
        from('direct:p6router.p6_demo_Quote')
            .choice()
                .when(xpath("/TransactionInfo/TechnicalStatusCode='Created'"))
                    .setHeader( 'platform6.request.action').constant('execute')
                    .setHeader("appkey").constant("p6_demo")
                    .setHeader('id').constant('p6_demo.SendQuoteToBlockchain')
                    .setProperty('execute.async').constant(true)
                    .to( 'p6route://scripts')
                .otherwise()
                    .throwException(com.amalto.b2box.core.api.B2boxException,'No matching rule found for item!')
            .end()
            .routeId('p6_demo Routing Rules - Quote')
            .description("p6_demo Quote Routing Rule")
    }
})
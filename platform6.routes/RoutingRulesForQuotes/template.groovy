camel.getCtx().addRoutes(new RouteBuilder() {
    
    void configure() {
        from('direct:p6router.p6_demo_Quote')
            .choice()
                .when(xpath("/TransactionInfo/TechnicalStatusCode='Created'"))
                    .setHeader( 'platform6.request.action').constant('execute')
                    .setHeader("appkey").constant("p6_demo")
                    .setHeader('id').constant('p6_demo.SendQuoteToBlockchain')
                    .setProperty('execute.async').constant(true)
                    .to("p6route://platform6.scripts")
                .otherwise()
                    .throwException(io.platform6.common.util.P6Exception,'No matching rule found for item!')
            .end()
            .routeId('p6_demo Routing rules for Quotes')
            .description("p6_demo Routing rules for Quotes")
    }
})

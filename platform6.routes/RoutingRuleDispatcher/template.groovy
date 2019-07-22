camel.getCtx().addRoutes(new RouteBuilder() {
    void configure() {
        from('direct:p6router.p6_demo_Dispatcher')
            .choice()
                .when(xpath("/TransactionInfo/TransactionType='Request For Quotation'"))
                    .to('direct:p6router.p6_demo_RequestForQuotation')
                .when(xpath("/TransactionInfo/TransactionType='Quote'"))
                    .to('direct:p6router.p6_demo_Quote')
                .when(xpath("/TransactionInfo/TransactionType='Purchase Order'"))
                    .to('direct:p6router.p6_demo_PurchaseOrder')
                .otherwise()
                    .throwException(io.platform6.common.util.P6Exception,'No matching rule found for item!')
            .end()
            .routeId("p6_demo Routing Rules Dispatcher")
            .description("p6_demo Routing Rules Dispatcher")
    }
})

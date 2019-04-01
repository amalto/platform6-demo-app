camel.getCtx().addRoutes(new RouteBuilder() {

    void configure() {

        from('direct:p6router.p6_demo_PurchaseOrder')
            .choice()
                .when(xpath("/TransactionInfo/TechnicalStatusCode='Received'"))
                    .setHeader("platform6.request.action").constant("invoke")
                    .setHeader("status").constant("Received")
                    .setHeader("step").constant("HandlePurchaseOrder")
                    .setHeader("appkey").constant("p6_demo")
                    .setHeader("flowname").constant("UUID")
                    .setHeader("script").constant("p6_demo.WFStepBuilder")
                    .to("p6route://workflowsteps")
            .end()
            .routeId('p6_demo Routing Rules - PurchaseOrder')
            .description("p6_demo Purchase Order Routing Rules")
    }
})
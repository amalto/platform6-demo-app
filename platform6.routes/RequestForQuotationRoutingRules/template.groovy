camel.getCtx().addRoutes(new RouteBuilder() {

    void configure() {
        from('direct:p6router.p6_demo_RequestForQuotation')
            .choice()
                .when(xpath("/TransactionInfo/TechnicalStatusCode='Received'"))
                    .setHeader("platform6.request.action").constant("invoke")
                    .setHeader("status").constant("Received")
                    .setHeader("step").constant("HandleRequestForQuotation")
                    .setHeader("appkey").constant("p6_demo")
                    .setHeader("flowname").constant("UUID")
                    .setHeader("script").constant("p6_demo.WFStepBuilder")
                    .to("p6route://workflowsteps")
                .otherwise()
                    .throwException(com.amalto.b2box.core.api.B2boxException,'No matching rule found for item!')
            .end()
            .routeId("p6_demo Routing Rules - RequestForQuotation")
            .description("p6_demo Request For Quotation Routing Rule")
    }
})

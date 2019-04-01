def routeId = "p6_demo Scheduler - CreateRFQTransaction"

try {
    camel.getCtx().addRoutes(new RouteBuilder() {
        void configure() {
            from("timer://myTimer?period=10m")
                .to('p6cmb://scripts?platform6.request.action=execute&id=p6_demo.CreateRFQTransaction')
                .routeId(routeId)
                .description("p6_demo Scheduled Route to generate RFQ");
        }
    })

    pause()
} finally {
  camel.destroyRoute(routeId)
}

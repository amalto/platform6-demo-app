def routeId = "p6_demo Scheduled creation of RFQs"

try {
    camel.getCtx().addRoutes(new RouteBuilder() {
        void configure() {
            from("timer://myTimer?period=10m")
                .to('p6cmb://scripts?platform6.request.action=execute&id=p6_demo.CreateRFQTransaction')
                .routeId(routeId)
                .description(routeId)
        }
    })

    pause()
} finally {
  camel.destroyRoute(routeId)
}

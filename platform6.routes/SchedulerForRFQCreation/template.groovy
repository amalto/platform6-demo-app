def routeId = "p6_demo Scheduled creation of RFQs"

try {
    p6.camel.getCtx().addRoutes(new RouteBuilder() {
        void configure() {
            from("timer://myTimer?period=10m")
                .to('p6cmb://scripts?platform6.request.action=execute&id=p6_demo.CreateRFQTransaction')
                .routeId(routeId)
                .description(routeId)
        }
    })

    p6.utils.pause()
} finally {
  p6.camel.destroyRoute(routeId)
}

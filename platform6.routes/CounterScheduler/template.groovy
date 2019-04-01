camel.getCtx().addRoutes(new RouteBuilder() {
	def void configure() {
		from("quartz2://platform6/p6route_Count_RFQs?cron=0+1+*+*+*+?+*")
			.to('p6cmb://counters?platform6.request.action=synchronize&id=p6_demo.CountRFQs')
			.routeId('p6_demo Refresh CountRFQs')
			.description("p6_demo Scheduled refresh of the CountRFQs counter")

		from("quartz2://platform6/p6route_Count_Quotes?cron=0+2+*+*+*+?+*")
			.to('p6cmb://counters?platform6.request.action=synchronize&id=p6_demo.CountQuotes')
			.routeId('p6_demo Refresh CountQuotes')
			.description("p6_demo Scheduled refresh of the CountQuotes counter")

        from("quartz2://platform6/p6route_Count_POs_Accepted?cron=0+3+*+*+*+?+*")
			.to('p6cmb://counters?platform6.request.action=synchronize&id=p6_demo.CountAcceptedPOs')
			.routeId('p6_demo Refresh CountAcceptedPOs')
			.description("p6_demo Scheduled refresh of the CountAcceptedPOs counter")

        from("quartz2://platform6/p6route_Count_POs_Rejected?cron=0+4+*+*+*+?+*")
			.to('p6cmb://counters?platform6.request.action=synchronize&id=p6_demo.CountRejectedPOs')
			.routeId('p6_demo Refresh CountRejectedPOs')
			.description("p6_demo Scheduled refresh of the CountRejectedPOs counter")

		from("quartz2://platform6/p6route_Count_FailedRoutingOrders?cron=0+0/15+*+*+*+?+*")
			.to('p6cmb://counters?platform6.request.action=synchronize&id=p6_demo.CountFailedRoutingOrders')
			.routeId('p6_demo Refresh CountFailedRoutingOrders')
			.description("p6_demo Scheduled refresh of the CountFailedRoutingOrders counter")
	}
})

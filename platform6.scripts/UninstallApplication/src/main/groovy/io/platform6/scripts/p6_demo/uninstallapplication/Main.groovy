// This script is executed to clean up all the applications resources

// Delete all transactions and workflow tasks
p6.sproc.execute('p6_demo.DeleteP6DemoTransactions')
log.debug 'Demo app transactions and workflow tasks deleted'

// Restart services
p6.service.restartService('platform6.workflowsteps')
p6.service.restartService('platform6.views')
p6.service.restartService('platform6.transactions')
log.debug 'Services restarted to finish cleaning up transaction data'

// Destroy routes
p6.camel.destroyRoute('p6_demo Web3j - BlockchainEventListener')
p6.camel.destroyRoute('p6_demo Scheduler - CreateRFQTransaction')
p6.camel.destroyRoute('p6_demo Routing Rules - PurchaseOrder')
p6.camel.destroyRoute('p6_demo Routing Rules - Quote')
p6.camel.destroyRoute('p6_demo Routing Rules - RequestForQuotation')
p6.camel.destroyRoute('p6_demo Routing Rules Dispatcher')
p6.camel.destroyRoute('p6_demo Refresh CountRFQs')
p6.camel.destroyRoute('p6_demo Refresh CountQuotes')
p6.camel.destroyRoute('p6_demo Refresh CountAcceptedPOs')
p6.camel.destroyRoute('p6_demo Refresh CountRejectedPOs')
p6.camel.destroyRoute('p6_demo Refresh CountFailedRoutingOrders')

// Undeploy bundled resources
p6.bundled.undeploy('p6_demo.POReview')
p6.bundled.undeploy('p6_demo.TableItemsData')
log.debug 'Bundled resources deleted from the local file system'
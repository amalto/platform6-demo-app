// This script is executed to clean up all the applications resources

// Delete all transactions and workflow tasks
sproc.execute('p6_demo.DeleteP6DemoTransactions')
log.debug 'Demo app transactions and workflow tasks deleted'

// Restart services
service.restartService('platform6.workflowsteps')
service.restartService('platform6.views')
service.restartService('platform6.transactions')
log.debug 'Services restarted to finish cleaning up transaction data'

// Destroy routes
camel.destroyRoute('p6_demo Web3j - BlockchainEventListener')
camel.destroyRoute('p6_demo Scheduler - CreateRFQTransaction')
camel.destroyRoute('p6_demo Routing Rules - PurchaseOrder')
camel.destroyRoute('p6_demo Routing Rules - Quote')
camel.destroyRoute('p6_demo Routing Rules - RequestForQuotation')
camel.destroyRoute('p6_demo Routing Rules Dispatcher')
camel.destroyRoute('p6_demo Refresh CountRFQs')
camel.destroyRoute('p6_demo Refresh CountQuotes')
camel.destroyRoute('p6_demo Refresh CountAcceptedPOs')
camel.destroyRoute('p6_demo Refresh CountRejectedPOs')
camel.destroyRoute('p6_demo Refresh CountFailedRoutingOrders')

// Undeploy bundled resources
bundled.undeploy('p6_demo.POReview')
bundled.undeploy('p6_demo.TableItemsData')
log.debug 'Bundled resources deleted from the local file system'

// Deploy (copy to local file system and load JAR classes into classpath) bundled resources
p6.bundled.deploy('p6_demo.DemoSmartContract')
p6.bundled.deploy('p6_demo.POReview')
p6.bundled.deploy('p6_demo.TableItemsData')
log.debug 'Deployed bundled resources'

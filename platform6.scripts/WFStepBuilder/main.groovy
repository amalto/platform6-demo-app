pipeline.variables().each() { log.debug "PIPELINE VAR ${it}" }

def workflowStep = pipeline.getXml 'templateStepXml'

def concept = pipeline.get 'platform6.request.concept'
def itemIds = pipeline.get 'platform6.request.ids'

def itemPk = transaction.buildPK(concept, itemIds)

def transactionInfoContent = transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

//def transactionInfo = pipeline.getXml '_DEFAULT_'


/*******************************************/
/*  ADD ONE PARAMETER BY LINE TO ACTION   */
/*******************************************/

def myAction = workflowStep.Actions.Action.find{it.@id = 'send'}

int nbLines = Integer.parseInt(transactionInfo.KeyValue.find{it.Key == 'Line items'}.Value.text())

def stepXml = groovy.xml.XmlUtil.serialize( workflowStep )

pipeline.put 'stepXml', stepXml

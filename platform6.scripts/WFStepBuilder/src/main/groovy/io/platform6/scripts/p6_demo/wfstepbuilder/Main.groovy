p6.pipeline.variables().each() { log.debug "PIPELINE VAR ${it}" }

def workflowStep = p6.pipeline.getXml 'templateStepXml'

def concept = p6.pipeline.get 'platform6.request.dataType'
def itemIds = p6.pipeline.get 'platform6.request.ids'

def itemPk = p6.transaction.buildPK(concept, itemIds)

def transactionInfoContent = p6.transaction.exists(itemPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

//def transactionInfo = pipeline.getXml '_DEFAULT_'


/*******************************************/
/*  ADD ONE PARAMETER BY LINE TO ACTION   */
/*******************************************/

def myAction = workflowStep.Actions.Action.find{it.@id = 'send'}

int nbLines = Integer.parseInt(transactionInfo.KeyValue.find{it.Key == 'Line items'}.Value.text())

def stepXml = groovy.xml.XmlUtil.serialize( workflowStep )

p6.pipeline.put 'stepXml', stepXml

def results = p6.xslt.process("View_Worklow Tasks", p6.resource.get('Workflow_Task_to_HTML'), p6.pipeline.get( 'xml'))

p6.pipeline.put 'portalHtml', results, 'text/html'

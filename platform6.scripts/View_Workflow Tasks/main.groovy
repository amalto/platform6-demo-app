def results = xslt.process("View_Worklow Tasks", resource.get('Workflow_Task_to_HTML'), pipeline.get( 'xml'))

pipeline.put 'portalHtml', results, 'text/html'

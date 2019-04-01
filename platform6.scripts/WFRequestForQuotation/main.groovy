import groovy.json.*
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.binary.Base64;

@B2RequiredEndpointScope(feature = "approvalworkflow", action = "read")
@Field def boolean endpointEnabled;

def xml 		= pipeline.getXml 'xml'
log.debug 'XML ======> '+ xml

pipeline.variables().each() { log.debug "PIPELINE VAR ${it}" }

def mpk 		= transaction.buildPK(xml.itemConceptName.text(), xml.itemIds.text().split('\\.'))
def message 	= transaction.get(mpk)

log.debug 'MESSAGE ======> '+ message

XmlSlurper slurper = new XmlSlurper(false, true)
def transaction = slurper.parseText(message)

def myURI = transaction.SourceDocumentURI.text()
        
def  srcFile = fileFromUri(myURI)

  
        
String rfqContent = FileUtils.readFileToString(srcFile)

def rfq = slurper.parseText(rfqContent)

def rfqLines = ""
rfq.RequestForQuotationLine.each{ rfqLine -> 

    def itemCategory = rfqLine.LineItem.Item.Name.text()
    
    def itemRecords = table.lookup('p6_demo.Items', ['Category':itemCategory]) 

    def items= ''
    for(myRecord in itemRecords) {
        def description = myRecord.Description
        def id = myRecord.Id
        def uom = myRecord.UOM
        def unitPrice = myRecord.UnitPrice
        def currency = myRecord.Currency
    
        if (!''.equals(items)) {
            items += ',\r\n'
        }        
        items += '{ "id": '+JsonOutput.toJson(description)+', "price": '+JsonOutput.toJson(unitPrice)+', "unit": '+JsonOutput.toJson(currency)+', "currency": '+JsonOutput.toJson(currency)+', "uom": '+JsonOutput.toJson(uom)+' }'
    }                 
            
    log.debug 'ITEMS => '+items  


    def oneLine = """ {
             "id": ${JsonOutput.toJson(rfqLine.ID.text())},
             "note": ${JsonOutput.toJson(rfqLine.LineItem.Item.Name.text())},
             "quantity": ${JsonOutput.toJson(rfqLine.LineItem.Quantity.text())},
             "items": [
                 ${items}
            ]
        }"""
        

    if (!"".equals(rfqLines)) {
        rfqLines += ",\r\n"
    }
     rfqLines += oneLine
}

log.debug 'RFQ LINES => '+rfqLines

def model = """{
    "rfq": { 
        "id": ${JsonOutput.toJson(rfq.ID.text())},
        "issueDate": ${JsonOutput.toJson(rfq.IssueDate.text())},
        "issueTime": ${JsonOutput.toJson(rfq.IssueTime.text())},
        "note": ${JsonOutput.toJson(rfq.Note.text())}
    },
    "lines": [
        ${rfqLines}
    ]
}"""

log.debug 'MODEL => '+model

pipeline.put 'model', model
pipeline.put 'form', resource.getCompiled( 'WF_RFQ_Form' )

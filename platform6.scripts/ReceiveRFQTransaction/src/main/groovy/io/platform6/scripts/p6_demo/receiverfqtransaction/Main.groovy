import org.apache.commons.io.FileUtils
import java.text.SimpleDateFormat

// This script reads the incoming RFQ, parses its content and writes the transaction in P6 database using its internal TransactionInfo model (check the Data Models menu for more info)
def rfqContent = p6.pipeline.get 'ubl'

XmlSlurper slurper = new XmlSlurper(false, true)
def rfq = slurper.parseText(rfqContent)

def instanceId = p6.configuration.get("instance.id")

def currentDate = new Date()
SimpleDateFormat transactionSDF = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss")
def formattedDate = transactionSDF.format(currentDate)

def transactionId = rfq.ID.text()
def comments = rfq.Note.text()
def issueDate = rfq.IssueDate.text()

int nbLines = rfq.RequestForQuotationLine.size()

def sellerName = rfq.SellerSupplierParty.Party.PartyName.Name.text()
def buyerName = rfq.OriginatorCustomerParty.Party.PartyName.Name.text()

def transactionType = 'RequestForQuotation'
def status = 'Received'
def statusMessage = 'Received by Demo App'

// Write the transaction on the file system
def sourceURI = "file://$P6_DATA/resources/documents/RequestForQuotation/in/" + transactionId + ".xml"
def sourceFile = p6.uri.fileFromUrl(sourceURI)
FileUtils.writeStringToFile(sourceFile, rfqContent,'UTF-8')

def targetURI = "file://$P6_DATA/resources/documents/RequestForQuotation/" + transactionId + ".xml"
def targetFile = p6.uri.fileFromUrl(targetURI)
FileUtils.writeStringToFile(targetFile, rfqContent,'UTF-8')

def transactionInfo = """<TransactionInfo>
    <Id>${transactionId}</Id>
    <CreationDate>${formattedDate}</CreationDate>
    <TransactionType>Request For Quotation</TransactionType>
    <TransactionNumber>${transactionId}</TransactionNumber>
    <SourceSystem>Demo App</SourceSystem>
    <TargetSystem/>
    <Sender/>
    <FinalRecipient>Supplier</FinalRecipient>
    <TransferProtocol>Blockchain</TransferProtocol>
    <DeliveredVia>Ethereum Blockchain</DeliveredVia>
    <SourceDocumentFormat>UBL</SourceDocumentFormat>
    <SourceDocumentURI>${sourceURI}</SourceDocumentURI>
    <SourceDocumentContentType>text/xml; charset=utf-8</SourceDocumentContentType>
    <PivotDocumentFormat>UBL</PivotDocumentFormat>
    <PivotDocumentURI>${targetURI}</PivotDocumentURI>
    <PivotDocumentContentType>text/xml; charset=utf-8</PivotDocumentContentType>
    <TargetDocumentFormat>UBL</TargetDocumentFormat>
    <TargetDocumentURI>${targetURI}</TargetDocumentURI>
    <TargetDocumentContentType>text/xml; charset=utf-8</TargetDocumentContentType>
    <TechnicalStatusCode>${status}</TechnicalStatusCode>
    <TechnicalStatusMessage>${statusMessage}</TechnicalStatusMessage>
    <TechnicalStatusDate>${formattedDate}</TechnicalStatusDate>
    <FunctionalStatusCode/>
    <FunctionalStatusMessage/>
    <FunctionalStatusDate/>
    <Flags>{"aavisible": true}</Flags>
    <OrgPath>/${instanceId}/</OrgPath>
    <KeyValue>
      <Key>Seller Name</Key>
      <Value>${p6.utils.escapeXml(sellerName)}</Value>
   </KeyValue>
   <KeyValue>
      <Key>Buyer Name</Key>
      <Value>${p6.utils.escapeXml(buyerName)}</Value>
   </KeyValue>
    <KeyValue>
        <Key>Comment</Key>
        <Value>${p6.utils.escapeXml(comments)}</Value>
    </KeyValue>
    <KeyValue>
        <Key>Issue Date</Key>
        <Value>${p6.utils.escapeXml(issueDate)}</Value>
    </KeyValue>
    <KeyValue>
        <Key>Line items</Key>
        <Value>${nbLines}</Value>
    </KeyValue>
       <KeyValue>
      <Key>Total Amount</Key>
      <Value></Value>
   </KeyValue>
    <KeyValue>
      <Key>Currency</Key>
      <Value></Value>
   </KeyValue>
</TransactionInfo>"""

def ipk = p6.transaction.buildPK('p6_demo.TransactionInfo', transactionId)
p6.transaction.saveAndRoute(transactionInfo, 'p6_demo.TransactionInfo', ipk, 'direct:p6router.p6_demo_Dispatcher')

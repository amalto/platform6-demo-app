/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import groovy.json.*
import org.apache.commons.io.FileUtils
import java.time.format.DateTimeFormatter
import java.math.BigDecimal
import java.text.DecimalFormat


def helper = new RFQSmartContractHelper(this)

def xml = pipeline.getXml 'xml'
def concept = pipeline.get 'platform6.request.concept'
def rfqId = pipeline.get 'platform6.request.ids'

// Fetch RFQ from database
def rfqPk = transaction.buildPK(concept, rfqId)
def transactionInfoContent = transaction.get(rfqPk)

XmlSlurper slurper = new XmlSlurper()
def transactionInfo = slurper.parseText(transactionInfoContent)

// Calculate issue date and time
def now = helper.now()
def formattedDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss").format(now)
def issueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now)
def issueTime = DateTimeFormatter.ofPattern("HH:mm:ss").format(now)

// Update the RFQ
transactionInfo.FunctionalStatusCode = 'Quote provided'
transactionInfo.FunctionalStatusMessage = ''
transactionInfo.FunctionalStatusDate = formattedDate

transactionInfo.TechnicalStatusCode = 'Handled'
transactionInfo.TechnicalStatusMessage = ''
transactionInfo.TechnicalStatusDate = formattedDate

def srcFile = fileFromUri( transactionInfo.SourceDocumentURI.text())

String rfqContent = FileUtils.readFileToString(srcFile)
def rfq = slurper.parseText(rfqContent)

def sellerName = rfq.SellerSupplierParty.Party.PartyName.Name.text()
def buyerName = rfq.OriginatorCustomerParty.Party.PartyName.Name.text()
def rfqIssueDate = rfq.IssueDate.text()

transaction.project(groovy.xml.XmlUtil.serialize(transactionInfo), 'p6_demo.TransactionInfo', rfqPk)

def linesContent = pipeline.get 'lines'

// Create Quote document

JsonSlurper jSlurper = new JsonSlurper()
def myLines = jSlurper.parseText(linesContent)

DecimalFormat ubl_df = new DecimalFormat("#0.00")

def quotationLines = ''

def totalAmount = new BigDecimal(0)
def totalCurrency = ''

def quoteId = UUID.randomUUID().toString()

rfq.RequestForQuotationLine.each { myLine -> 

    def inputLine = myLines.find { it.id == myLine.ID.text() }
    def itemDetail = inputLine.items.find { it.id == inputLine.item }
    
    BigDecimal unitPrice = new BigDecimal(itemDetail.price)
    
    if (inputLine.discount!=null && !''.equals(inputLine.discount) && !'0'.equals(inputLine.discount)) {
        BigDecimal discount =  new BigDecimal(100).	subtract(new BigDecimal(inputLine.discount))
        unitPrice = unitPrice.multiply(discount).divide(new BigDecimal(100))
    }
    
    def lineQty = new BigDecimal(myLine.LineItem.Quantity.text())
    
    def lineAmmount = lineQty.multiply(unitPrice)
    
    totalAmount = totalAmount.add(lineAmmount)
    totalCurrency = itemDetail.currency
    
    quotationLines +=  """<cac:QuotationLine>
      <cbc:ID>${escapeXml(myLine.ID.text())}</cbc:ID>
      <cbc:Note>${escapeXml(myLine.Note.text())}</cbc:Note>
      <cac:LineItem>
        <cbc:ID>${escapeXml(myLine.LineItem.ID.text())}</cbc:ID>
        <cbc:Quantity unitCode="${escapeXml(myLine.LineItem.Quantity.'@unitCode'.text())}">${escapeXml(myLine.LineItem.Quantity.text())}</cbc:Quantity>
        <cbc:LineExtensionAmount currencyID="${escapeXml(itemDetail.currency)}">${escapeXml(ubl_df.format(lineAmmount))}</cbc:LineExtensionAmount>
        <cac:Price>
          <cbc:PriceAmount currencyID="${escapeXml(itemDetail.currency)}">${escapeXml(ubl_df.format(unitPrice))}</cbc:PriceAmount>
          <cbc:BaseQuantity unitCode="${escapeXml(itemDetail.uom)}">1</cbc:BaseQuantity>
        </cac:Price>
        <cac:Item>
          <cbc:Description>${escapeXml(inputLine.item)}</cbc:Description>
          <cbc:Name>${escapeXml(myLine.LineItem.Item.Name.text())}</cbc:Name>
        </cac:Item>
      </cac:LineItem>
    </cac:QuotationLine>
    """
}

def quoteUBL = """<?xml version="1.0" encoding="UTF-8"?>
<Quotation xmlns="urn:oasis:names:specification:ubl:schema:xsd:Quotation-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
  <cbc:UBLVersionID>2.0</cbc:UBLVersionID>
  <cbc:CustomizationID>OIOUBL-2.1</cbc:CustomizationID>
  <cbc:ProfileID schemeAgencyID="320" schemeID="urn:oioubl:id:profileid-1.2">Procurement-QuoSim-1.0</cbc:ProfileID>
  <cbc:ID>${escapeXml(quoteId)}</cbc:ID>
  <cbc:CopyIndicator>false</cbc:CopyIndicator>
  <cbc:UUID>${escapeXml(quoteId)}</cbc:UUID>
  <cbc:IssueDate>${escapeXml(issueDate)}</cbc:IssueDate>
  <cbc:IssueTime>${escapeXml(issueTime)}</cbc:IssueTime>
  <cbc:Note languageID="da-dk">Bestilling af computere</cbc:Note>
  <cac:RequestForQuotationDocumentReference>
    <cbc:ID>G867B</cbc:ID>
	<cbc:UUID>${escapeXml(rfqId)}</cbc:UUID>
	<cbc:IssueDate>${escapeXml(rfqIssueDate)}</cbc:IssueDate>
  </cac:RequestForQuotationDocumentReference>
  <cac:SellerSupplierParty>
    <cbc:CustomerAssignedAccountID>LEV00123</cbc:CustomerAssignedAccountID>
    <cac:Party>
      <cbc:EndpointID schemeID="DK:CVR">DK18296799</cbc:EndpointID>
      <cac:PartyIdentification>
        <cbc:ID schemeID="DK:CVR">DK18296799</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name>${escapeXml(sellerName)}</cbc:Name>
      </cac:PartyName>
      <cac:PostalAddress>
        <cbc:AddressFormatCode listAgencyID="320" listID="urn:oioubl:codelist:addressformatcode-1.1">StructuredDK</cbc:AddressFormatCode>
        <cbc:StreetName>Arne Jacobsens Allé</cbc:StreetName>
        <cbc:BuildingNumber>15</cbc:BuildingNumber>
        <cbc:CityName>København S</cbc:CityName>
        <cbc:PostalZone>2300</cbc:PostalZone>
        <cac:Country>
          <cbc:IdentificationCode>DK</cbc:IdentificationCode>
        </cac:Country>
      </cac:PostalAddress>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName>Delcomputer A/S</cbc:RegistrationName>
        <cbc:CompanyID schemeID="DK:CVR">18296799</cbc:CompanyID>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:SellerSupplierParty>
  <cac:OriginatorCustomerParty>
    <cac:Party>
      <cbc:EndpointID schemeAgencyID="9" schemeID="GLN">5798000416604</cbc:EndpointID>
      <cac:PartyIdentification>
        <cbc:ID schemeAgencyID="9" schemeID="GLN">5798000416604</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name>${escapeXml(buyerName)}</cbc:Name>
      </cac:PartyName>
      <cac:PostalAddress>
        <cbc:AddressFormatCode listAgencyID="320" listID="urn:oioubl:codelist:addressformatcode-1.1">StructuredDK</cbc:AddressFormatCode>
        <cbc:StreetName>Bernstorffsvej</cbc:StreetName>
        <cbc:BuildingNumber>161</cbc:BuildingNumber>
        <cbc:CityName>Charlottenlund</cbc:CityName>
        <cbc:PostalZone>2920</cbc:PostalZone>
        <cac:Country>
          <cbc:IdentificationCode>DK</cbc:IdentificationCode>
        </cac:Country>
      </cac:PostalAddress>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName>Gentofte Kommune</cbc:RegistrationName>
        <cbc:CompanyID schemeID="DK:CVR">DK12345678</cbc:CompanyID>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:OriginatorCustomerParty>
	<cac:QuotedMonetaryTotal>
    <cbc:PayableAmount currencyID ="${escapeXml(totalCurrency)}">${escapeXml(ubl_df.format(totalAmount))}</cbc:PayableAmount>
  </cac:QuotedMonetaryTotal>
  ${quotationLines}
</Quotation>
"""

def transactionType = 'Quote'
def comments = ''
int nbLines = rfq.RequestForQuotationLine.size()

def status = 'Created'
def statusMessage = 'Created by Demo App'

def sourceURI = "file://$B2BOX_DATA/resources/documents/" + transactionType + "/" + quoteId + ".xml"

def  sourceFile = fileFromUri(sourceURI)
FileUtils.writeStringToFile(sourceFile, quoteUBL,'UTF-8')

def applicationId = 'DemoApp_Dev'

def newTransactionInfo = """<TransactionInfo>
    <Id>${escapeXml(quoteId)}</Id>
    <CreationDate>${escapeXml(formattedDate)}</CreationDate>
    <TransactionType>${escapeXml(transactionType)}</TransactionType>
    <TransactionNumber>${escapeXml(quoteId)}</TransactionNumber>
    <SourceSystem>Demo App</SourceSystem>
    <TargetSystem/>
    <Sender/>
    <FinalRecipient>Supplier</FinalRecipient>
    <TransferProtocol>Blockchain</TransferProtocol>
    <DeliveredVia>Amalto</DeliveredVia>
    <SourceDocumentFormat>UBL</SourceDocumentFormat>
    <SourceDocumentURI>${escapeXml(sourceURI)}</SourceDocumentURI>
    <SourceDocumentContentType>text/xml; charset=utf-8</SourceDocumentContentType>
    <PivotDocumentFormat>UBL</PivotDocumentFormat>
    <PivotDocumentURI>${escapeXml(sourceURI)}</PivotDocumentURI>
    <PivotDocumentContentType>text/xml; charset=utf-8</PivotDocumentContentType>
    <TargetDocumentFormat>UBL</TargetDocumentFormat>
    <TargetDocumentURI>${escapeXml(sourceURI)}</TargetDocumentURI>
    <TargetDocumentContentType>text/xml; charset=utf-8</TargetDocumentContentType>
    <TechnicalStatusCode>${escapeXml(status)}</TechnicalStatusCode>
    <TechnicalStatusMessage>${escapeXml(statusMessage)}</TechnicalStatusMessage>
    <TechnicalStatusDate>${escapeXml(formattedDate)}</TechnicalStatusDate>
    <FunctionalStatusCode/>
    <FunctionalStatusMessage/>
    <FunctionalStatusDate/>
    <Flags>{"aavisible": true}</Flags>
    <OrgPath>/${escapeXml(applicationId)}/</OrgPath>
    <KeyValue>
      <Key>Seller Name</Key>
      <Value>${escapeXml(sellerName)}</Value>
    </KeyValue>
    <KeyValue>
      <Key>Buyer Name</Key>
      <Value>${escapeXml(buyerName)}</Value>
    </KeyValue>
    <KeyValue>
        <Key>Comment</Key>
        <Value>${escapeXml(comments)}</Value>
    </KeyValue>
    <KeyValue>
        <Key>Issue Date</Key>
        <Value>${escapeXml(issueDate)}</Value>
    </KeyValue>
    <KeyValue>
        <Key>Line items</Key>
        <Value>${nbLines}</Value>
    </KeyValue>
       <KeyValue>
      <Key>Total Amount</Key>
      <Value>${escapeXml(ubl_df.format(totalAmount))}</Value>
   </KeyValue>
      <KeyValue>
      <Key>Currency</Key>
      <Value>${escapeXml(totalCurrency)}</Value>
   </KeyValue>
    <KeyValue>
      <Key>Application</Key>
      <Value>p6_demo</Value>
   </KeyValue>
</TransactionInfo>"""

def quotePk = transaction.buildPK('TransactionInfo', quoteId)
transaction.p6projectAndRoute(newTransactionInfo, 'p6_demo.TransactionInfo', quotePk, 'direct:p6router.p6_demo_Dispatcher')

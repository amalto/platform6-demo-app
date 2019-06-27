/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import java.util.UUID
import org.apache.commons.io.FileUtils
import java.time.format.DateTimeFormatter

def helper = new RFQSmartContractHelper(this)

def quoteContent = p6.pipeline.get 'ubl'

XmlSlurper slurper = new XmlSlurper(false, true)
def quote = slurper.parseText(quoteContent)

def applicationId = 'DemoAp_Dev'

// Calculate issue date and time
def now = helper.now()
def formattedDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss").format(now)
def issueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now)
def issueTime = DateTimeFormatter.ofPattern("HH:mm:ss").format(now)

def sellerName = quote.SellerSupplierParty.Party.PartyName.Name.text()
def buyerName = quote.OriginatorCustomerParty.Party.PartyName.Name.text()
def totalAmount = quote.QuotedMonetaryTotal.PayableAmount.text()
def totalCurrency = quote.QuotedMonetaryTotal.PayableAmount.'@currencyID'.text()

def quoteId = quote.ID.text()
def comments = quote.Note.text()

int nbLines = quote.QuotationLine.size()

def transactionType = 'PurchaseOrder'
def purchaseOrderId = UUID.randomUUID().toString()
def status = 'Received'
def statusMessage = 'Received by Demo App'

def sourceURI = "file://$P6_DATA/resources/documents/Quote/" + quoteId + ".xml"
def targetURI = "file://$P6_DATA/resources/documents/PurchaseOrder/" + purchaseOrderId + ".xml"

def orderLines = ''

quote.QuotationLine.each { myLine ->

    orderLines += """<cac:OrderLine>
    <cbc:Note>${escapeXml(myLine.Note.text())}</cbc:Note>
    <cac:LineItem>
      <cbc:ID>${escapeXml(myLine.ID.text())}</cbc:ID>
      <cbc:Quantity unitCode="${escapeXml(myLine.LineItem.Quantity.'@unitCode'.text())}">${escapeXml(myLine.LineItem.Quantity.text())}</cbc:Quantity>
      <cbc:LineExtensionAmount currencyID="${escapeXml(myLine.LineItem.LineExtensionAmount.'@currencyID'.text())}">${escapeXml(myLine.LineItem.LineExtensionAmount.text())}</cbc:LineExtensionAmount>
      <cac:Price>
        <cbc:PriceAmount currencyID="${escapeXml(myLine.LineItem.Price.PriceAmount.'@currencyID'.text())}">${escapeXml(myLine.LineItem.Price.PriceAmount.text())}</cbc:PriceAmount>
        <cbc:BaseQuantity unitCode="${escapeXml(myLine.LineItem.Price.BaseQuantity.'@unitCode'.text())}">${escapeXml(myLine.LineItem.Price.BaseQuantity.text())}</cbc:BaseQuantity>
      </cac:Price>
      <cac:Item>
        <cbc:Description>${escapeXml(myLine.LineItem.Item.Description.text())}</cbc:Description>
        <cbc:Name>${escapeXml(myLine.LineItem.Item.Name.text())}</cbc:Name>
      </cac:Item>
    </cac:LineItem>
  </cac:OrderLine>
    """
}


def orderContent = """<?xml version="1.0" encoding="UTF-8"?>
<Order xmlns="urn:oasis:names:specification:ubl:schema:xsd:Order-2"
  xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
  xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
  <cbc:CustomizationID>urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0</cbc:CustomizationID>
  <cbc:ProfileID schemeAgencyID="BII" schemeID="Profile"
    >urn:www.cenbii.eu:profile:BII01:ver1.0</cbc:ProfileID>
  <cbc:ID>${escapeXml(purchaseOrderId)}</cbc:ID>
  <cbc:IssueDate>${escapeXml(issueDate)}</cbc:IssueDate>
   <cbc:IssueTime>${escapeXml(issueTime)}</cbc:IssueTime>
  <cbc:Note>Information text for the whole order</cbc:Note>
  <cbc:DocumentCurrencyCode>SEK</cbc:DocumentCurrencyCode>
  <cbc:AccountingCostCode>Project123</cbc:AccountingCostCode>
  <cac:QuotationDocumentReference>
    <cbc:ID>QuoteID123</cbc:ID>
  </cac:QuotationDocumentReference>
  <cac:OrderDocumentReference>
    <cbc:ID>RjectedOrderID123</cbc:ID>
  </cac:OrderDocumentReference>
  <cac:OriginatorDocumentReference>
    <cbc:ID>MAFO</cbc:ID>
  </cac:OriginatorDocumentReference>
  <cac:Contract>
    <cbc:ID>34322</cbc:ID>
    <cbc:ContractType>FrameworkAgreementID123</cbc:ContractType>
  </cac:Contract>
  <cac:BuyerCustomerParty>
    <cac:Party>
      <cbc:EndpointID schemeAgencyID="9" schemeID="GLN">7300072311115</cbc:EndpointID>
      <cac:PartyIdentification>
        <cbc:ID schemeAgencyID="9" schemeID="GLN">7300070011115</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyIdentification>
        <cbc:ID>PartyID123</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name>${escapeXml(buyerName)}</cbc:Name>
      </cac:PartyName>
      <cac:PostalAddress>
        <cbc:ID schemeAgencyID="9" schemeID="GLN">1234567890123</cbc:ID>
        <cbc:Postbox>PoBox123</cbc:Postbox>
        <cbc:StreetName>Rådhusgatan</cbc:StreetName>
        <cbc:AdditionalStreetName>2nd floor</cbc:AdditionalStreetName>
        <cbc:BuildingNumber>5</cbc:BuildingNumber>
        <cbc:Department>Purchasing department</cbc:Department>
        <cbc:CityName>Stockholm</cbc:CityName>
        <cbc:PostalZone>11000</cbc:PostalZone>
        <cbc:CountrySubentity>RegionX</cbc:CountrySubentity>
        <cac:Country>
          <cbc:IdentificationCode>SE</cbc:IdentificationCode>
        </cac:Country>
      </cac:PostalAddress>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName>Johnssons Byggvaror AB</cbc:RegistrationName>
        <cbc:CompanyID schemeID="SE:ORGNR">5532331183</cbc:CompanyID>
        <cac:RegistrationAddress>
          <cbc:CityName>Stockholm</cbc:CityName>
          <cbc:CountrySubentity>RegionX</cbc:CountrySubentity>
          <cac:Country>
            <cbc:IdentificationCode>SE</cbc:IdentificationCode>
          </cac:Country>
        </cac:RegistrationAddress>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:BuyerCustomerParty>
  <cac:SellerSupplierParty>
    <cac:Party>
      <cbc:EndpointID schemeAgencyID="9" schemeID="GLN">7302347231111</cbc:EndpointID>
      <cac:PartyIdentification>
        <cbc:ID>SellerPartyID123</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name>${escapeXml(sellerName)}</cbc:Name>
      </cac:PartyName>
      <cac:PostalAddress>
        <cbc:ID schemeAgencyID="9" schemeID="GLN">0987654321123</cbc:ID>
        <cbc:Postbox>321</cbc:Postbox>
        <cbc:StreetName>Kungsgatan</cbc:StreetName>
        <cbc:AdditionalStreetName>suite12</cbc:AdditionalStreetName>
        <cbc:BuildingNumber>22</cbc:BuildingNumber>
        <cbc:Department>Sales department</cbc:Department>
        <cbc:CityName>Stockholm</cbc:CityName>
        <cbc:PostalZone>11000</cbc:PostalZone>
        <cbc:CountrySubentity>RegionX</cbc:CountrySubentity>
        <cac:Country>
          <cbc:IdentificationCode>SE</cbc:IdentificationCode>
        </cac:Country>
      </cac:PostalAddress>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName>Moderna Produkter AB</cbc:RegistrationName>
        <cbc:CompanyID schemeID="SE:ORGNR">5532332283</cbc:CompanyID>
        <cac:RegistrationAddress>
          <cbc:CityName>Stockholm</cbc:CityName>
          <cbc:CountrySubentity>RegionX</cbc:CountrySubentity>
          <cac:Country>
            <cbc:IdentificationCode>SE</cbc:IdentificationCode>
          </cac:Country>
        </cac:RegistrationAddress>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:SellerSupplierParty>
  <cac:OriginatorCustomerParty>
    <cac:Party>
      <cac:PartyIdentification>
        <cbc:ID schemeAgencyID="9" schemeID="GLN">0987678321123</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name>Moderna Produkter AB</cbc:Name>
      </cac:PartyName>
    </cac:Party>
  </cac:OriginatorCustomerParty>
  <cac:AnticipatedMonetaryTotal>
    <cbc:PayableAmount currencyID ="${escapeXml(totalCurrency)}">${escapeXml(totalAmount)}</cbc:PayableAmount>
  </cac:AnticipatedMonetaryTotal>
  ${orderLines}
</Order>

"""


def targetFile = p6.uri.fileFromUrl(targetURI)

FileUtils.writeStringToFile(targetFile, orderContent,'UTF-8')

def transactionInfo = """<TransactionInfo>
    <Id>${purchaseOrderId}</Id>
    <CreationDate>${formattedDate}</CreationDate>
    <TransactionType>Purchase Order</TransactionType>
    <TransactionNumber>${purchaseOrderId}</TransactionNumber>
    <SourceSystem>Demo App</SourceSystem>
    <TargetSystem/>
    <Sender/>
    <FinalRecipient>Supplier</FinalRecipient>
    <TransferProtocol>Blockchain</TransferProtocol>
    <DeliveredVia>Amalto</DeliveredVia>
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
    <OrgPath>/${applicationId}/</OrgPath>
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
      <Value>${escapeXml(totalAmount)}</Value>
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

def ipk = p6.transaction.buildPK('TransactionInfo', purchaseOrderId)
p6.transaction.saveAndRoute(transactionInfo, 'p6_demo.TransactionInfo', ipk, 'direct:p6router.p6_demo_Dispatcher')

/**
 * You can call another script by using 'Include' tags
 * 
 * @include RFQSmartContractHelper
 */

import groovy.json.JsonSlurper

import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.Random

import org.apache.commons.io.FileUtils


// It is assumed that there is only one buyer in the system to keep the Demo App simple
def buyerId = '1'
def helper = new RFQSmartContractHelper(this)

// Calculate issue date and time for generated RFQ transaction
def now = helper.now()
def issueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now)
def issueTime = DateTimeFormatter.ofPattern("HH:mm:ss").format(now)

Random rand = new Random()

// Retrieve the list of categories from Items table
def itemRecords = table.lookup('p6_demo.Items', [:]) 

Set<String> categories = new HashSet<String>()
Map<String,String> cat_to_uom = new HashMap<String,String>()

for (myRecord in itemRecords) {
    def category = myRecord.Category
    def uom = myRecord.UOM

    if (!categories.contains(category)) {
        categories.add(category)
        cat_to_uom.put(category, uom)
    }
}

// Generate random RFQ lines from available items
int nbLineMax = categories.size()
int quantityMax = 10
int nbLines = rand.nextInt(nbLineMax) + 1

def rfqLines= ''
for (int i =0; i< nbLines; i++) {
    
    int categoryIndex = rand.nextInt(categories.size())
    def categoryArray = categories.toArray()
    
    def category = categoryArray[categoryIndex]
    categories.remove(category)

    def quantity = rand.nextInt(quantityMax + 1) + 1
    def uom = cat_to_uom.get(category)
        
    rfqLines += """<cac:RequestForQuotationLine>
      <cbc:ID>${i+1}</cbc:ID>
      <cbc:Note>sample</cbc:Note>
      <cac:LineItem>
         <cbc:ID>${i+1}</cbc:ID>
         <cbc:Quantity unitCode="${uom}">${quantity}</cbc:Quantity>
         <cac:Item>
            <cbc:Description></cbc:Description>
            <cbc:Name>${escapeXml(category)}</cbc:Name>
            <cac:BuyersItemIdentification>
               <cbc:ID>${buyerId}</cbc:ID>
            </cac:BuyersItemIdentification>
         </cac:Item>
      </cac:LineItem>
   </cac:RequestForQuotationLine>
    """
}

// Generate the transaction ID and the corresponding UBL
def transactionId = UUID.randomUUID()

def requestForQuotationUBL = """<RequestForQuotation xmlns="urn:oasis:names:specification:ubl:schema:xsd:RequestForQuotation-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
   <cbc:UBLVersionID>2.0</cbc:UBLVersionID>
   <cbc:ID>${transactionId}</cbc:ID>
   <cbc:IssueDate>${escapeXml(issueDate)}</cbc:IssueDate>
   <cbc:IssueTime>${escapeXml(issueTime)}</cbc:IssueTime>
   <cbc:Note>Automatically generated RFQ</cbc:Note>
   <cac:OriginatorCustomerParty>
      <cac:Party>
         <cac:PartyName>
            <cbc:Name>Buyer Corp.</cbc:Name>
         </cac:PartyName>
         <cac:PostalAddress>
            <cbc:StreetName>Avon Way</cbc:StreetName>
            <cbc:BuildingName>Thereabouts</cbc:BuildingName>
            <cbc:BuildingNumber>56A</cbc:BuildingNumber>
            <cbc:CityName>Bridgtow</cbc:CityName>
            <cbc:PostalZone>ZZ99 1ZZ</cbc:PostalZone>
            <cbc:CountrySubentity>Avon</cbc:CountrySubentity>
            <cac:AddressLine>
               <cbc:Line>3rd Floor, Room 5</cbc:Line>
            </cac:AddressLine>
            <cac:Country>
               <cbc:IdentificationCode>GB</cbc:IdentificationCode>
            </cac:Country>
         </cac:PostalAddress>
      </cac:Party>
   </cac:OriginatorCustomerParty>
   <cac:SellerSupplierParty>
      <cbc:CustomerAssignedAccountID>CO001</cbc:CustomerAssignedAccountID>
      <cac:Party>
         <cac:PartyName>
            <cbc:Name>Seller Inc.</cbc:Name>
         </cac:PartyName>
         <cac:PostalAddress>
            <cbc:StreetName>Busy Street</cbc:StreetName>
            <cbc:BuildingName>Thereabouts</cbc:BuildingName>
            <cbc:BuildingNumber>56A</cbc:BuildingNumber>
            <cbc:CityName>Farthing</cbc:CityName>
            <cbc:PostalZone>AA99 1BB</cbc:PostalZone>
            <cbc:CountrySubentity>Heremouthshire</cbc:CountrySubentity>
            <cac:AddressLine>
               <cbc:Line>The Roundabout</cbc:Line>
            </cac:AddressLine>
            <cac:Country>
               <cbc:IdentificationCode>GB</cbc:IdentificationCode>
            </cac:Country>
         </cac:PostalAddress>
      </cac:Party>
   </cac:SellerSupplierParty>
   ${rfqLines}
</RequestForQuotation>
"""

// Write the transaction in the blockchain
helper.submitRFQ(transactionId, now, requestForQuotationUBL)

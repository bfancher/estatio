package org.estatio.app.services.order;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;

import org.incode.module.country.dom.impl.Country;
import org.incode.module.country.dom.impl.CountryRepository;

import org.estatio.capex.dom.charge.IncomingCharge;
import org.estatio.capex.dom.charge.IncomingChargeRepository;
import org.estatio.capex.dom.order.Order;
import org.estatio.capex.dom.order.OrderRepository;
import org.estatio.capex.dom.time.TimeInterval;
import org.estatio.capex.dom.time.TimeIntervalRepository;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.party.OrganisationRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.dom.project.Project;
import org.estatio.dom.project.ProjectRepository;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.tax.TaxRepository;

import lombok.AllArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "orderInvoiceLine")
@XmlType(
        propOrder = {
                "status",
                "supplierName",
                "charge",
                "entryDate",
                "orderDate",
                "seller",
                "orderDescription",
                "netAmount",
                "vatAmount",
                "grossAmount",
                "orderApprovedBy",
                "orderApprovedOn",
                "projectReference",
                "period",
                "tax",
                "invoiceNumber",
                "invoiceDescription",
                "invoiceNetAmount",
                "invoiceVatAmount",
                "invoiceGrossAmount",
                "invoiceTax"
        }
)
@DomainObject(
        objectType = "org.estatio.app.services.order.OrderInvoiceLine"
)
@AllArgsConstructor
public class OrderInvoiceLine {

    public String title() {
        return "Order - Invoice Import Line";
    }

    public OrderInvoiceLine() {}

    @Setter
    @MemberOrder(sequence = "1")
    private String status;

    @XmlElement(required = false)
    public String getStatus() {
        return status;
    }

    @Setter
    private String supplierName;

    @XmlElement(required = false)
    public String getSupplierName() {
        return supplierName;
    }

    @Setter
    @MemberOrder(sequence = "3")
    private String charge;

    @XmlElement(required = false)
    public String getCharge() {
        return charge;
    }


    @Setter
    private LocalDate entryDate;

    @XmlElement(required = false)
    public LocalDate getEntryDate() {
        return entryDate;
    }

    @Setter
    private LocalDate orderDate;

    @XmlElement(required = false)
    public LocalDate getOrderDate() {
        return orderDate;
    }

    @Setter
    private String seller;

    @XmlElement(required = false)
    public String getSeller() {
        return seller;
    }

    @Setter
    private String orderDescription;

    @XmlElement(required = false)
    public String getOrderDescription() {
        return orderDescription;
    }


    @Setter
    @Column(scale = 2)
    private BigDecimal netAmount;

    @XmlElement(required = false)
    public BigDecimal getNetAmount() {
        return netAmount;
    }


    @Setter
    @Column(scale = 2)
    private BigDecimal vatAmount;

    @XmlElement(required = false)
    public BigDecimal getVatAmount() {
        return vatAmount;
    }


    @Setter
    @Column(scale = 2)
    private BigDecimal grossAmount;

    @XmlElement(required = false)
    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    @Setter
    private String orderApprovedBy;

    @XmlElement(required = false)
    public String getOrderApprovedBy() {
        return orderApprovedBy;
    }

    @Setter
    private LocalDate orderApprovedOn;

    @XmlElement(required = false)
    public LocalDate getOrderApprovedOn() {
        return orderApprovedOn;
    }

    @Setter
    private String projectReference;

    @XmlElement(required = false)
    public String getProjectReference() {
        return projectReference;
    }

    @Setter
    private String period;

    @XmlElement(required = false)
    public String getPeriod() {
        return period;
    }

    @Setter
    private String tax;

    @XmlElement(required = false)
    public String getTax() {
        return tax;
    }

    @Setter
    private String invoiceNumber;

    @XmlElement(required = false)
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    @Setter
    private String invoiceDescription;

    @XmlElement(required = false)
    public String getInvoiceDescription() {
        return invoiceDescription;
    }

    @Setter
    @Column(scale = 2)
    private BigDecimal invoiceNetAmount;

    @XmlElement(required = false)
    public BigDecimal getInvoiceNetAmount() {
        return invoiceNetAmount;
    }

    @Setter
    @Column(scale = 2)
    private BigDecimal invoiceVatAmount;

    @XmlElement(required = false)
    public BigDecimal getInvoiceVatAmount() {
        return invoiceVatAmount;
    }

    @Setter
    @MemberOrder(sequence = "20")
    @Column(scale = 2)
    private BigDecimal invoiceGrossAmount;

    @XmlElement(required = false)
    public BigDecimal getInvoiceGrossAmount() {
        return invoiceGrossAmount;
    }

    @Setter
    @MemberOrder(sequence = "21")
    private String invoiceTax;

    @XmlElement(required = false)
    public String getInvoiceTax() {
        return invoiceTax;
    }

    /**
     * Using a mixin so can continue to use lombok's @AllArgsConstructor
     * (else the additional injected services required confuse things)
     */
    @Mixin(method="act")
    public static class _apply {
        private final OrderInvoiceLine line;
        public _apply(final OrderInvoiceLine line) {
            this.line = line;
        }
        @Action()
        @ActionLayout(contributed= Contributed.AS_ACTION)
        public OrderInvoiceLine act() {
            final String orderNumber = determineOrderNumber();
            final Party supplier = determineSupplier(line.supplierName);
            final Project project = lookupProject(line.projectReference);
            final Property property = inferPropertyFrom(line.projectReference);
            final Party buyer =  partyRepository.findPartyByReference(property.getExternalReference());
            final String atPath = "/FRA";
            final TimeInterval timeInterval = timeIntervalRepository.findByName(line.period);

            if(property == null) {
                return line;
            }

            Order order = orderRepository.findOrCreate(
                    line.getSupplierName(),
                    determineOrderNumber(),
                    line.entryDate,
                    line.orderDate,
                    supplier,
                    buyer,
                    atPath,
                    line.orderApprovedBy, line.orderApprovedOn);

            final IncomingCharge chargeObj = incomingChargeRepository.findByName(line.charge);
            final Tax taxObj = taxRepository.findByReference(line.tax);

            order.addItem(
                    chargeObj, line.orderDescription,
                    line.netAmount, line.vatAmount, line.grossAmount,
                    taxObj, timeInterval, property, project);

            return line;
        }

        private Party determineSupplier(final String supplierName) {
            Party party = partyRepository.matchPartyByReferenceOrName(supplierName);
            Country france = countryRepository.findCountry("FRA");
            if (party==null){
                RandomCodeGenerator10Chars generator = new RandomCodeGenerator10Chars();
                party = organisationRepository.newOrganisation(generator.generateRandomCode().toUpperCase(), false, supplierName, france);
            }
            return party;
        }

        private final Pattern projectReferencePattern = Pattern.compile("^([^-])+[-].*$");
        private Property inferPropertyFrom(final String projectReference) {
            final Matcher matcher = projectReferencePattern.matcher(projectReference);
            if(!matcher.matches()) {
                return null;
            }
            final String propertyReference = matcher.group(1);
            return propertyRepository.findPropertyByReference(propertyReference);
        }

        private Project lookupProject(final String projectReference) {
            return projectRepository.findByReference(projectReference);
        }

        private String determineOrderNumber() {
            return "TODO";
        }

        @Inject
        OrderRepository orderRepository;
        @Inject
        PropertyRepository propertyRepository;
        @Inject
        ProjectRepository projectRepository;
        @Inject
        IncomingChargeRepository incomingChargeRepository;
        @Inject
        TaxRepository taxRepository;
        @Inject
        TimeIntervalRepository timeIntervalRepository;
        @Inject
        PartyRepository partyRepository;
        @Inject
        OrganisationRepository organisationRepository;
        @Inject
        CountryRepository countryRepository;


        public static class RandomCodeGenerator10Chars {

            private static final int NUMBER_CHARACTERS = 10;
            private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

            public String generateRandomCode() {
                final StringBuilder buf = new StringBuilder(NUMBER_CHARACTERS);
                for (int i = 0; i < NUMBER_CHARACTERS; i++) {
                    final int pos = (int) ((Math.random() * CHARACTERS.length()));
                    buf.append(CHARACTERS.charAt(pos));
                }
                return buf.toString();
            }

        }


    }
    
    
}

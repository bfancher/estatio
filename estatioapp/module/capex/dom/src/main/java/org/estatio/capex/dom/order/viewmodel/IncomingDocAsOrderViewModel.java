package org.estatio.capex.dom.order.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateStringAdapter;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.capex.dom.documents.BuyerFinder;
import org.estatio.capex.dom.documents.viewmodel.IncomingDocViewModel;
import org.estatio.capex.dom.order.Order;
import org.estatio.capex.dom.order.OrderItem;
import org.estatio.capex.dom.order.OrderRepository;
import org.estatio.dom.party.Party;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@DomainObject(
        // WORKAROUND: using fqcn as objectType because Isis' invalidation of cache in prototyping mode causing NPEs in some situations
        objectType = "org.estatio.capex.dom.documents.categorisation.order.IncomingDocAsOrderViewModel",
        editing = Editing.ENABLED
)
@XmlRootElement(name = "incomingOrderViewModel")
@XmlType(
        propOrder = {
                "document",
                "orderNumber",
                "buyer",
                "seller",
                "sellerOrderReference",
                "orderDate",
                "description",
                "charge",
                "property",
                "project",
                "period",
                "budgetItem",
                "netAmount",
                "vatAmount",
                "tax",
                "grossAmount",
                "domainObject",
                "originatingTask"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
@Getter @Setter
public class IncomingDocAsOrderViewModel extends IncomingDocViewModel<Order> {


    /**
     * for unit testing
     */
    IncomingDocAsOrderViewModel() {}

    public IncomingDocAsOrderViewModel(final Order order, final Document document) {
        super(document);
        this.domainObject = order;
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(named = "Order")
    Order domainObject;

    @Property(editing = Editing.ENABLED)
    private String orderNumber;

    @Property(editing = Editing.ENABLED)
    private String sellerOrderReference;

    @XmlJavaTypeAdapter(JodaLocalDateStringAdapter.ForJaxb.class)
    @Property(editing = Editing.ENABLED)
    private LocalDate orderDate;

    /**
     * TODO: inline this mixin
     */
    @Mixin(method="act")
    public static class changeOrderDetails {
        private final IncomingDocAsOrderViewModel viewModel;
        public changeOrderDetails(final IncomingDocAsOrderViewModel viewModel) {
            this.viewModel = viewModel;
        }
        @Action(semantics = SemanticsOf.IDEMPOTENT)
        public IncomingDocAsOrderViewModel act(
                final String orderNumber,
                final Party buyer,
                final Party seller,
                @Nullable
                final String sellerOrderReference,
                @Nullable
                final LocalDate orderDate
        ){
            viewModel.setOrderNumber(orderNumber);
            viewModel.setBuyer(buyer);
            viewModel.setSeller(seller);
            viewModel.setSellerOrderReference(sellerOrderReference);
            viewModel.setOrderDate(orderDate);
            return viewModel;
        }

        public String default0Act(){
            return viewModel.getOrderNumber();
        }

        public Party default1Act(){
            return viewModel.getBuyer();
        }

        public Party default2Act(){
            return viewModel.getSeller();
        }

        public String default3Act(){
            return viewModel.getSellerOrderReference();
        }

        public LocalDate default4Act(){
            return viewModel.getOrderDate();
        }

        public String disableAct() {
            return viewModel.reasonNotEditableIfAny();
        }

    }

    @Programmatic
    public void init() {

        final Order order = getDomainObject();
        setOrderNumber(order.getOrderNumber());
        setSellerOrderReference(order.getSellerOrderReference());
        setOrderDate(order.getOrderDate());
        setSeller(order.getSeller());
        setBuyer(order.getBuyer());
        setProperty(order.getProperty());

        final Optional<OrderItem> firstItemIfAny = getFirstItemIfAny();
        if(firstItemIfAny.isPresent()) {
            OrderItem orderItem = firstItemIfAny.get();

            setCharge(orderItem.getCharge());
            setDescription(orderItem.getDescription());
            setNetAmount(orderItem.getNetAmount());
            setVatAmount(orderItem.getVatAmount());
            setGrossAmount(orderItem.getGrossAmount());
            setTax(orderItem.getTax());
            setPeriod(periodFrom(orderItem.getStartDate(), orderItem.getEndDate()));
            setProperty(orderItem.getProperty());
            setProject(orderItem.getProject());
            setBudgetItem(orderItem.getBudgetItem());
        }
    }

    @Action(semantics = SemanticsOf.SAFE)
    @MemberOrder(sequence = "2")
    public Order cancel() {
        return getDomainObject();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public Order save() {

        Order order = getDomainObject();
        order.setOrderNumber(getOrderNumber());
        order.setSellerOrderReference(getSellerOrderReference());
        order.setEntryDate(clockService.now());
        order.setOrderDate(getOrderDate());
        order.setSeller(getSeller());
        order.setBuyer(getBuyer());

        Optional<OrderItem> firstItemIfAny = getFirstItemIfAny();
        if(firstItemIfAny.isPresent()) {

            OrderItem orderItem = firstItemIfAny.get();
            orderItem.setCharge(getCharge());
            orderItem.setDescription(getDescription());
            orderItem.setNetAmount(getNetAmount());
            orderItem.setVatAmount(getVatAmount());
            orderItem.setGrossAmount(getGrossAmount());
            orderItem.setTax(getTax());
            orderItem.setStartDate(getStartDateFromPeriod());
            orderItem.setEndDate(getEndDateFromPeriod());
            orderItem.setProperty(getProperty());
            orderItem.setProject(getProject());
            orderItem.setBudgetItem(getBudgetItem());
        } else {
            order.addItem(
                    getCharge(),
                    getDescription(),
                    getNetAmount(),
                    getVatAmount(),
                    getGrossAmount(),
                    getTax(),
                    getPeriod(),
                    getProperty(),
                    getProject(),
                    getBudgetItem()
            );
        }

        return order;
    }

    public String disableSave() {
        return reasonNotEditableIfAny();
    }

    @Override
    protected String reasonNotEditableIfAny() {
        Order order = getDomainObject();

        String reasonDisabledDueToState = order.reasonDisabledDueToState();
        if(reasonDisabledDueToState != null) {
            return reasonDisabledDueToState;
        }

        return null;
    }

    private Optional<OrderItem> getFirstItemIfAny() {
        return getDomainObject().getItems().stream().findFirst();
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(multiLine = 5)
    public String getNotification(){
        final StringBuilder result = new StringBuilder();

        final String noBuyerBarcodeMatch = buyerBarcodeMatchValidation();
        if (noBuyerBarcodeMatch!=null){
            result.append(noBuyerBarcodeMatch);
        }

        final String sameOrderNumberCheck = doubleOrderCheck();
        if (sameOrderNumberCheck !=null){
            result.append(sameOrderNumberCheck);
        }

        return result.length()>0 ? result.toString() : null;

    }

    public boolean hideNotification(){
        return getNotification() == null;
    }

    private String doubleOrderCheck(){
        final String doubleOrderCheck = possibleDoubleOrder();
        if (doubleOrderCheck !=null){
            return doubleOrderCheck;
        }
        final String sameNumberCheck = sameOrderNumber();
        if (sameNumberCheck !=null){
            return sameNumberCheck;
        }
        return null;
    }

    private String possibleDoubleOrder(){
        if (getOrderNumber()==null || getSeller()==null || getOrderDate()==null){
            return null;
        }
        if (getDomainObject() == null) {
            return null;
        }
        Order possibleDouble = orderRepository.findByOrderNumberAndSellerAndOrderDate(getOrderNumber(), getSeller(), getOrderDate());
        if (possibleDouble == null || possibleDouble.equals(domainObject)) {
            return null;
        }

        return "WARNING: There is already an order with the same number and order date for this seller. Please check.";
    }

    private String sameOrderNumber(){
        if (getOrderNumber()==null || getSeller()==null){
            return null;
        }
        if (getDomainObject()!=null){
            List<Order> similarNumberedOrders = new ArrayList<>();
            for (Order order : orderRepository.findByOrderNumberAndSeller(getOrderNumber(), getSeller())) {
                if (!order.equals(getDomainObject())) {
                    similarNumberedOrders.add(order);
                }
            }
            if (similarNumberedOrders.size()>0){
                String message = "WARNING: Orders with the same number of this seller are found ";
                for (Order order : similarNumberedOrders){
                    if (order.getOrderDate()!=null) {
                        message = message.concat("on date ").concat(order.getOrderDate().toString()).concat("; ");
                    }
                }
                return message;
            }
        }
        return null;
    }

    private String buyerBarcodeMatchValidation(){
        if (getBuyer()!=null && getDomainObject()!=null){
            if (buyerFinder.buyerDerivedFromDocumentName(getDomainObject())==null){
                return null; // covers all cases where no buyer could be derived from document name
            }
            if (!getBuyer().equals(buyerFinder.buyerDerivedFromDocumentName(getDomainObject()))){
                return "Buyer does not match barcode (document name); ";
            }
        }
        return null;
    }

    @Inject
    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    ClockService clockService;

    @Inject
    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    FactoryService factoryService;

    @Inject
    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    OrderRepository orderRepository;

    @Inject
    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    BuyerFinder buyerFinder;

}

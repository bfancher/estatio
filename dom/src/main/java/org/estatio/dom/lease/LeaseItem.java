package org.estatio.dom.lease;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.common.collect.Ordering;

import org.estatio.dom.index.Index;
import org.estatio.dom.index.Indices;
import org.estatio.dom.invoice.Charge;
import org.estatio.dom.invoice.Charges;
import org.estatio.dom.utils.CalenderUtils;
import org.estatio.dom.utils.Orderings;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Resolve;
import org.apache.isis.applib.annotation.Resolve.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

@PersistenceCapable
public class LeaseItem extends AbstractDomainObject implements Comparable<LeaseItem> {

    @Hidden
    void dummyAction1(LeaseTermForIndexableRent x) {
    }

    @Hidden
    void dummyAction2(LeaseTermForTurnoverRent x) {
    }

    // {{ Lease (property)
    private Lease lease;

    @Hidden(where = Where.PARENTED_TABLES)
    @MemberOrder(sequence = "1")
    public Lease getLease() {
        return lease;
    }

    public void setLease(final Lease lease) {
        this.lease = lease;
    }

    // }}

    // {{ Sequence (property)
    private BigInteger sequence;

    @MemberOrder(sequence = "1")
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    // }}

    // {{ LeaseItemType (property)
    private LeaseItemType type;

    @Title
    @MemberOrder(sequence = "2")
    public LeaseItemType getType() {
        return type;
    }

    public void setType(final LeaseItemType type) {
        this.type = type;
    }

    // }}

    // {{ StartDate (property)
    private LocalDate startDate;

    @Persistent
    @MemberOrder(sequence = "3")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    // }}

    // {{ EndDate (property)
    private LocalDate endDate;

    @Persistent
    @MemberOrder(sequence = "4")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // }}

    // {{ TenancyStartDate (property)
    private LocalDate tenancyStartDate;

    @Persistent
    @Optional
    @MemberOrder(sequence = "5")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getTenancyStartDate() {
        return tenancyStartDate;
    }

    public void setTenancyStartDate(final LocalDate tenancyStartDate) {
        this.tenancyStartDate = tenancyStartDate;
    }

    // }}

    // {{ TenancyEndDate (property)
    private LocalDate tenancyEndDate;

    @Persistent
    @Optional
    @MemberOrder(sequence = "6")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getTenancyEndDate() {
        return tenancyEndDate;
    }

    public void setTenancyEndDate(final LocalDate tenancyEndDate) {
        this.tenancyEndDate = tenancyEndDate;
    }

    // }}

    // {{ NextDueDate (property)
    private LocalDate nextDueDate;

    @MemberOrder(sequence = "7")
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(final LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    // }}

    // {{ InvoicingFrequency (property)
    private InvoicingFrequency invoicingFrequency;

    @MemberOrder(sequence = "12")
    public InvoicingFrequency getInvoicingFrequency() {
        return invoicingFrequency;
    }

    public void setInvoicingFrequency(final InvoicingFrequency invoicingFrequency) {
        this.invoicingFrequency = invoicingFrequency;
    }

    // }}

    // {{ PayymentMethod (property)
    private PaymentMethodType paymentMethod;

    @MemberOrder(sequence = "13")
    public PaymentMethodType getPayymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethodType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // }}

    // {{ Charge (property)
    private Charge charge;

    @MemberOrder(sequence = "14")
    public Charge getCharge() {
        return charge;
    }

    public void setCharge(final Charge charge) {
        this.charge = charge;
    }

    public List<Charge> choicesCharge() {
        return chargeService.allCharges();
    }

    // }}

    // {{ CurrentValue
    public BigDecimal getCurrentValue() {
        for (LeaseTerm term : getTerms()) {
            if (CalenderUtils.isBetween(LocalDate.now(), term.getStartDate(), term.getEndDate())) {
                return term.getValue();
            }
        }
        return null;
    }

    @Hidden
    public BigDecimal getValueForDate(LocalDate date) {
        for (LeaseTerm term : getTerms()) {
            if (CalenderUtils.isBetween(date, term.getStartDate(), term.getEndDate())) {
                return term.getValue();
            }
        }
        return null;
    }

    // }}
    
    // {{ Terms (Collection)
    private SortedSet<LeaseTerm> terms = new TreeSet<LeaseTerm>();

    @Resolve(Type.EAGERLY)
    @Persistent(mappedBy = "leaseItem")
    @MemberOrder(name = "Terms", sequence = "15")
    public SortedSet<LeaseTerm> getTerms() {
        return terms;
    }

    public void setTerms(final SortedSet<LeaseTerm> terms) {
        this.terms = terms;
    }

    public void addToTerms(final LeaseTerm leaseTerm) {
        // check for no-op
        if (leaseTerm == null || getTerms().contains(leaseTerm)) {
            return;
        }
        // associate new
        getTerms().add(leaseTerm);
        // additional business logic
        onAddToTerms(leaseTerm);
    }

    public void removeFromTerms(final LeaseTerm terms) {
        // check for no-op
        if (terms == null || !getTerms().contains(terms)) {
            return;
        }
        // dissociate existing
        getTerms().remove(terms);
        // additional business logic
        onRemoveFromTerms(terms);
    }

    @Hidden
    public LeaseTerm findTerm(LocalDate startDate) {
        for (LeaseTerm term : getTerms()) {
            if (startDate.equals(term.getStartDate())) {
                return term;
            }
        }
        return null;
    }

    @Hidden
    public LeaseTerm findTermForSequence(BigInteger sequence) {
        for (LeaseTerm term : getTerms()) {
            if (sequence.equals(term.getSequence())) {
                return term;
            }
        }
        return null;
    }

    protected void onAddToTerms(final LeaseTerm terms) {
    }

    protected void onRemoveFromTerms(final LeaseTerm terms) {
    }

    @Hidden
    @MemberOrder(name = "Terms", sequence = "11")
    public LeaseTerm addTerm() {
        LeaseTerm term = getType().createLeaseTerm(getContainer());
        terms.add(term);
        return term;
    }

    // }}

    // {{ Injected Services


    private Charges chargeService;

    public void setChargeService(Charges charges) {
        this.chargeService = charges;
    }

    // }}
    
    // {{ Comparable

    @Override
    public int compareTo(LeaseItem o) {
        return ORDERING_BY_TYPE.compound(ORDERING_BY_START_DATE).compare(this, o);
    }

    public static Ordering<LeaseItem> ORDERING_BY_TYPE = new Ordering<LeaseItem>() {
        public int compare(LeaseItem p, LeaseItem q) {
            return LeaseItemType.ORDERING_NATURAL.compare(p.getType(), q.getType());
        }
    };

    public final static Ordering<LeaseItem> ORDERING_BY_START_DATE = new Ordering<LeaseItem>() {
        public int compare(LeaseItem p, LeaseItem q) {
            return Orderings.lOCAL_DATE_NATURAL_NULLS_FIRST.compare(p.getStartDate(), q.getStartDate());
        }
    };

    // }}

}

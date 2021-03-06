package org.estatio.dom.budgetassignment.calculationresult.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.budgetassignment.BudgetAssignmentService;
import org.estatio.dom.budgeting.budget.Budget;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationService;
import org.estatio.dom.budgeting.budgetcalculation.BudgetCalculationType;

/**
 * This cannot be inlined (needs to be a mixin) because Budget doesn't know about BudgetAssignmentService.
 */
@Mixin
public class Budget_Calculate {

    private final Budget budget;
    public Budget_Calculate(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public Budget calculate(
            @ParameterLayout(describedAs = "Final calculation will make the calculations permanent and impact the leases")
            final boolean finalCalculation
    ) {
        budgetCalculationService.calculatePersistedCalculations(budget);
        budgetAssignmentService.calculateResultsForLeases(budget, BudgetCalculationType.BUDGETED);
        if (finalCalculation){
            budgetAssignmentService.assign(budget);
        }
        return budget;
    }

    @Inject
    private BudgetCalculationService budgetCalculationService;

    @Inject
    private BudgetAssignmentService budgetAssignmentService;

}

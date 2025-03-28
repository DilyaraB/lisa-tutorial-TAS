package it.unive.lisa.tutorial;

import it.unive.lisa.analysis.combination.ValueCartesianProduct;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.symbolic.value.Identifier;

import java.util.HashSet;
import java.util.Set;

public class ExtendedSignsTVPIProductDomain extends ValueCartesianProduct<
        ValueEnvironment<ExtendedSigns>,
        TwoVarsLinearInequality> {

    public ExtendedSignsTVPIProductDomain(ValueEnvironment<ExtendedSigns> left, TwoVarsLinearInequality right) {
        super(left, right);
        reduce();
    }

    @Override
    public ExtendedSignsTVPIProductDomain mk(ValueEnvironment<ExtendedSigns> left, TwoVarsLinearInequality right) {
        return new ExtendedSignsTVPIProductDomain(left, right).reduce();
    }

    private ExtendedSignsTVPIProductDomain reduce() {
        ValueEnvironment<ExtendedSigns> newLeft = this.left;
        TwoVarsLinearInequality newRight = this.right;

        if (newRight.isBottom() || newLeft.isBottom()) {
            return new ExtendedSignsTVPIProductDomain(newLeft.bottom(), newRight.bottom());
        }

        // Refine TwoVarsLinearInequality based on ExtendedSigns
        Set<TwoVarsLinearInequality.TwoVarsInequality> newConstraints = new HashSet<>(newRight.getConstraints());
        for (Identifier id : newLeft.getKeys()) {
            ExtendedSigns sign = newLeft.getState(id);
            if (sign == ExtendedSigns.getZero()) {
                // Add constraint: id == 0 (i.e., 1*id <= 0 and -1*id <= 0)
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(1, id, 0, null, 0));
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(-1, id, 0, null, 0));
            } else if (sign == ExtendedSigns.getPositive()) {
                // Add constraint: id > 0 (approximated as id >= 1)
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(-1, id, 0, null, -1));
            } else if (sign == ExtendedSigns.getNegative()) {
                // Add constraint: id < 0 (approximated as id <= -1)
                newConstraints.add(new TwoVarsLinearInequality.TwoVarsInequality(1, id, 0, null, -1));
            }
        }

        newRight = new TwoVarsLinearInequality(newConstraints);
        return new ExtendedSignsTVPIProductDomain(newLeft, newRight);
    }
}
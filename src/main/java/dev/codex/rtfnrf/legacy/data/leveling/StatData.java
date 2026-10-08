package dev.codex.rtfnrf.legacy.data.leveling;

import dev.codex.rtfnrf.legacy.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import java.util.function.Function;

public final class StatData {
    private final String id;
    private final double initialMin;
    private final double initialMax;
    private final double thresholdMin;
    private final double thresholdMax;
    private final boolean hasThreshold;
    private final UpgradeOperation operation;
    private final double upgradeModifier;
    private final Function<Double, ? extends Number> formatter;

    private StatData(
        String id, double initialMin, double initialMax,
        double thresholdMin, double thresholdMax, boolean hasThreshold,
        UpgradeOperation operation, double upgradeModifier,
        Function<Double, ? extends Number> formatter
    ) {
        this.id = id;
        this.initialMin = initialMin;
        this.initialMax = initialMax;
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
        this.hasThreshold = hasThreshold;
        this.operation = operation;
        this.upgradeModifier = upgradeModifier;
        this.formatter = formatter;
    }

    public static StatDataBuilder builder(String id) {
        return new StatDataBuilder(id);
    }

    public String getId() {
        return id;
    }

    public AbilityStatTemplate toTemplate(int maxLevel) {
        var builder = AbilityStatTemplate.builder(id).initialValue(initialMin, initialMax);
        if (hasThreshold) {
            builder.thresholdValue(thresholdMin, thresholdMax);
        }
        if (upgradeModifier != 0.0D && maxLevel > 0) {
            double target;
            switch (operation) {
                case MULTIPLY_BASE -> {
                    target = initialMax + initialMax * upgradeModifier * maxLevel;
                    builder.targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), target);
                }
                case MULTIPLY_TOTAL -> {
                    target = initialMax * Math.pow(upgradeModifier + 1.0D, maxLevel);
                    builder.targetValue(RelicsScalingModels.EXPONENTIAL.get(), target);
                }
                default -> {
                    target = initialMax + upgradeModifier * maxLevel;
                    builder.targetValue(RelicsScalingModels.ADDITIVE.get(), target);
                }
            }
        }
        if (formatter != null) {
            builder.formatValue(formatter);
        }
        return builder.build();
    }

    public static final class StatDataBuilder {
        private final String id;
        private double initialMin;
        private double initialMax;
        private double thresholdMin;
        private double thresholdMax;
        private boolean hasThreshold;
        private UpgradeOperation operation = UpgradeOperation.ADD;
        private double upgradeModifier;
        private Function<Double, ? extends Number> formatter = value -> value;

        private StatDataBuilder(String id) {
            this.id = id;
        }

        public StatDataBuilder initialValue(double min, double max) {
            this.initialMin = min;
            this.initialMax = max;
            return this;
        }

        public StatDataBuilder thresholdValue(double min, double max) {
            this.thresholdMin = min;
            this.thresholdMax = max;
            this.hasThreshold = true;
            return this;
        }

        public StatDataBuilder upgradeModifier(UpgradeOperation operation, double modifier) {
            this.operation = operation == null ? UpgradeOperation.ADD : operation;
            this.upgradeModifier = modifier;
            return this;
        }

        public StatDataBuilder formatValue(Function<Double, ? extends Number> formatter) {
            this.formatter = formatter;
            return this;
        }

        public StatData build() {
            return new StatData(
                id, initialMin, initialMax, thresholdMin, thresholdMax,
                hasThreshold, operation, upgradeModifier, formatter
            );
        }
    }
}


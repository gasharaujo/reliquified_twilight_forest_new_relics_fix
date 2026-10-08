package dev.codex.rtfnrf.legacy.data.leveling;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilitiesData {
    private final Map<String, AbilityData> abilities;

    private AbilitiesData(Map<String, AbilityData> abilities) {
        this.abilities = Map.copyOf(abilities);
    }

    public static AbilitiesDataBuilder builder() {
        return new AbilitiesDataBuilder();
    }

    public Map<String, AbilityData> getAbilities() {
        return abilities;
    }

    public AbilitiesTemplate toTemplate() {
        var builder = AbilitiesTemplate.builder();
        for (AbilityData ability : abilities.values()) builder.ability(ability.toTemplate());
        return builder.build();
    }

    public static final class AbilitiesDataBuilder {
        private final Map<String, AbilityData> abilities = new LinkedHashMap<>();

        public AbilitiesDataBuilder ability(AbilityData ability) {
            if (ability != null) abilities.put(ability.getId(), ability);
            return this;
        }

        public AbilitiesData build() {
            return new AbilitiesData(abilities);
        }
    }
}


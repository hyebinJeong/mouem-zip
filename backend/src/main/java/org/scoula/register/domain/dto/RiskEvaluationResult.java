package org.scoula.register.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.register.domain.RegistryRating;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationResult {
    private RegistryRating rating;
    private long totalPriorAmount;
}

package org.scoula.specialcontractmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialcontractmanagerDTO {
    private int specialClauseId;
    private String category;
    private String importance;
    private String importanceColor;
    private String description;
}

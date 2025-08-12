package org.scoula.specialcontractmanager.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialcontractmanagerVO {
    private int specialClauseId;
    private String category;
    private String importance;
    private String importanceColor;
    private String description;
}

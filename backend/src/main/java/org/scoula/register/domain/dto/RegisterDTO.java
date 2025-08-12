package org.scoula.register.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.register.domain.RegistryRating;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private Integer registryId;
    private Integer userId;
    private String address;
    private String risks;
    private String registryName;
    private RegistryRating registryRating;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisDate;
    private boolean status;
    private String fileName;
    private long totalPriorAmount;
}

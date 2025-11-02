package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSuggestionDTO {

    private String placeId;
    private String description;
    private String mainText;
    private String secondaryText;
}

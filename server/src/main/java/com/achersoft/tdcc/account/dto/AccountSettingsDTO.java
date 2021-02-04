package com.achersoft.tdcc.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSettingsDTO {
    private String id;
    private boolean interactive;
    private String settings;
}

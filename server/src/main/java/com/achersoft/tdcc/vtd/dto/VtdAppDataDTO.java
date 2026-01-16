package com.achersoft.tdcc.vtd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdAppDataDTO {

    public @Builder.Default String name = "TD Character Creator";
    public @Builder.Default String version = "01.2026";

}

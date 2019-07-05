package com.achersoft.security.dao;

import com.achersoft.jackson.SecondDateDeserializer;
import com.achersoft.jackson.SecondDateSerializer;
import com.achersoft.security.type.Privilege;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal{
    @JsonDeserialize(using = SecondDateDeserializer.class)
    @JsonSerialize(using = SecondDateSerializer.class)
    private Date iat;
    @JsonDeserialize(using = SecondDateDeserializer.class)
    @JsonSerialize(using = SecondDateSerializer.class)
    private Date exp;
    private String sub;
    private String userName;
    private String name;
    private String sessionId;
    private List<Privilege> privileges;
}

package com.company.Day4.Practice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
public class Order {

    private Long id;

    private  Long ProductId;

    private Long quantity;

    private  Status status;

    private String Idempotency_Key;


}

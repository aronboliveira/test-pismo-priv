package com.pismochallenge.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operation_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationType {

    @Id
    @Column(name = "operation_type_id")
    private Integer operationTypeId;

    @Column(name = "description", nullable = false, length = 30)
    private String description;
}

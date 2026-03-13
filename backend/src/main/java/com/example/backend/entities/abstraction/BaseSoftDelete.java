package com.example.backend.entities.abstraction;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@SoftDelete(columnName = "deleted", strategy = SoftDeleteType.ACTIVE)
public abstract class BaseSoftDelete extends BaseEntity{

    @Column(name = "deleted_at")
    private Date deletedAt;
}

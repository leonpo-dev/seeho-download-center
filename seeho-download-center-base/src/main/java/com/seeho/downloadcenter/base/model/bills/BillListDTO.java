package com.seeho.downloadcenter.base.model.bills;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Leonpo
 * @since 2025-12-15
 */
@Data
public class BillListDTO {

    private Long id;

    private String billCode;
    /**
     * (yyyy-mm-dd)
     */
    private LocalDate billDate;


    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

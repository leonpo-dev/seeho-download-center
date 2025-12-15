package com.seeho.downloadcenter.base.model.bills;

import com.seeho.downloadcenter.base.common.PageRequest;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Leonpo
 * @since 2025-12-15
 */
@Data
public class QueryBillsDTO extends PageRequest {
    /**
     * (yyyy-mm-dd)
     */
    //not null
    private LocalDate billDateStart;
    //not null
    private LocalDate billDateEnd;
}

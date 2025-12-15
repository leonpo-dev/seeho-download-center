package com.seeho.downloadcenter.domain.dotask.process.impl;

import com.seeho.downloadcenter.base.model.bills.BillListDTO;
import com.seeho.downloadcenter.base.model.bills.QueryBillsDTO;
import com.seeho.downloadcenter.domain.dotask.process.QueryExportDataService;
import com.seeho.downloadcenter.domain.utils.JsonUtil;
import com.seeho.downloadcenter.domain.utils.ParamSplitUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * simple example
 *
 * @author Leonpo
 * @since 2025-12-15
 */
@Service
public class ZTOBillsQueryImpl implements QueryExportDataService<QueryBillsDTO, BillListDTO> {
    @Override
    public QueryBillsDTO convertedDownloadCondition(String downloadCondition) {
        return JsonUtil.fromJson(downloadCondition, QueryBillsDTO.class);
    }

    @Override
    public Long queryTotalCount(QueryBillsDTO condition) {
        //todo
        return 0L;
    }

    @Override
    public List<BillListDTO> queryExportData(QueryBillsDTO condition) {
        //todo
        return null;
    }

    @Override
    public ParamSplitUtils<QueryBillsDTO> initSplitUtils(QueryBillsDTO condition) {
        Function<QueryBillsDTO, Long> counter = this::queryTotalCount;
        if (Objects.nonNull(condition.getBillDateStart()) && Objects.nonNull(condition.getBillDateEnd())) {
            ParamSplitUtils<QueryBillsDTO> splitUtils = ParamSplitUtils
                    .forLocalDate(QueryBillsDTO::getBillDateStart,
                            QueryBillsDTO::getBillDateEnd,
                            QueryBillsDTO::setBillDateStart,
                            QueryBillsDTO::setBillDateEnd,
                            counter);

            return splitUtils;
        }
        return null;
    }

    @Override
    public Class<BillListDTO> getExportDataClass() {
        return BillListDTO.class;
    }

    @Override
    public Map<String, Function<BillListDTO, Object>> getFieldMapper() {
        Map<String, Function<BillListDTO, Object>> mapper = new HashMap<>();
        mapper.put("billDate", BillListDTO::getBillDate);
        // TODO:
        return mapper;
    }
}

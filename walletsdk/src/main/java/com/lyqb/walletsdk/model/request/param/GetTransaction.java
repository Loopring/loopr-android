package com.lyqb.walletsdk.model.request.param;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GetTransaction {
    private String owner;
    private String txHash;
    private String symbol;
    private String status;
    private String txType;

    private int pageIndex;
    private int pageSize;

}
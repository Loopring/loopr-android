/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-26 下午6:00
 * Cooperation: loopring.org 路印协议基金会
 */
package com.lyqb.walletsdk.model.request.param;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class GetTokenParam {

    @NonNull
    String owner;
}

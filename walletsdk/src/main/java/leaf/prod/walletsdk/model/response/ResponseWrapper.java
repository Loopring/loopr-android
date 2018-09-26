package leaf.prod.walletsdk.model.response;

import lombok.Data;

@Data
public class ResponseWrapper<T> {

    private String id;

    private String jsonrpc;

    private T result;

    private String error;
}

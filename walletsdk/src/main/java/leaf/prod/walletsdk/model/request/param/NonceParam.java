package leaf.prod.walletsdk.model.request.param;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class NonceParam {

    @NonNull
    private String owner;
}

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class CoreConvertService<D, S extends Enum> {
    private final Xs2aObjectMapper xs2aObjectMapper;

    public byte[] data(D data, S targetStatus) {
        Map<S, Function<D, Object>> statusTransformer = statusTransformer();
        for (S status : statusTransformer.keySet()) {
            if (status == targetStatus) {
                Function<D, Object> transformingFunction = statusTransformer.get(status);
                return writeValueAsBytes(transformingFunction.apply(data));
            }
        }
        return new byte[0];
    }

    private byte[] writeValueAsBytes(Object object) {
        if (object == null) {
            return new byte[0];
        }

        try {
            return xs2aObjectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.warn("Can't convert object to byte[] : {}", e.getMessage());
            return new byte[0];
        }
    }

    public abstract Map<S, Function<D, Object>> statusTransformer();
}

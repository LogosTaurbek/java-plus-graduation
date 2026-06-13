package ru.practicum.explorewithme.service.compilation.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.service.exception.NotFoundException;

@Configuration("compilationFeignErrorDecoder")
public class FeignErrorDecoder {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new NotFoundException("Ресурс не найден (статус 404 от " + methodKey + ")");
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}

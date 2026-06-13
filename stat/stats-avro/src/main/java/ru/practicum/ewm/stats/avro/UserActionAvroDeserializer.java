package ru.practicum.ewm.stats.avro;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class UserActionAvroDeserializer implements Deserializer<UserActionAvro> {

    private final SpecificDatumReader<UserActionAvro> reader = new SpecificDatumReader<>(UserActionAvro.class);

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации UserActionAvro", e);
        }
    }
}

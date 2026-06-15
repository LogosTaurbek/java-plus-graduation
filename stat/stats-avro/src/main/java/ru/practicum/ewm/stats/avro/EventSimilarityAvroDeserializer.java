package ru.practicum.ewm.stats.avro;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {

    private final SpecificDatumReader<EventSimilarityAvro> reader = new SpecificDatumReader<>(EventSimilarityAvro.class);

    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации EventSimilarityAvro", e);
        }
    }
}

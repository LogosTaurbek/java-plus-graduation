package ru.practicum.ewm.stats.avro;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EventSimilarityAvroSerializer implements Serializer<EventSimilarityAvro> {

    private final SpecificDatumWriter<EventSimilarityAvro> writer = new SpecificDatumWriter<>(EventSimilarityAvro.class);

    @Override
    public byte[] serialize(String topic, EventSimilarityAvro data) {
        if (data == null) return null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации EventSimilarityAvro", e);
        }
    }
}

package org.mongodb.morphia.mapping.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeData;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
class MorphiaMapPropertyCodecProvider implements PropertyCodecProvider {

    @Override
    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            Class<?> keyType = type.getTypeParameters().get(0).getType();

            try {
                return new MapCodec(type.getType(), keyType, registry.get(type.getTypeParameters().get(1)));
            } catch (CodecConfigurationException e) {
                if (type.getTypeParameters().get(1).getType() == Object.class) {
                    try {
                        return (Codec<T>) registry.get(TypeData.builder(Map.class).build());
                    } catch (CodecConfigurationException e1) {
                        // Ignore and return original exception
                    }
                }
                throw e;
            }
        }
        return null;
    }

    private static class MapCodec<K, V> implements Codec<Map<K, V>> {
        private final Class<Map<K, V>> encoderClass;
        private Class<K> keyType;
        private final Codec<V> codec;

        MapCodec(final Class<Map<K, V>> encoderClass, final Class<K> keyType, final Codec<V> codec) {
            this.encoderClass = encoderClass;
            this.keyType = keyType;
            this.codec = codec;
        }

        @Override
        public void encode(final BsonWriter writer, final Map<K, V> map, final EncoderContext encoderContext) {
            writer.writeStartDocument();
            for (final Entry<K, V> entry : map.entrySet()) {
                final K key = entry.getKey();
                writer.writeName((String) Conversions.convert(key.getClass(), String.class, key));
                if (entry.getValue() == null) {
                    writer.writeNull();
                } else {
                    codec.encode(writer, entry.getValue(), encoderContext);
                }
            }
            writer.writeEndDocument();
        }

        @Override
        public Map<K, V> decode(final BsonReader reader, final DecoderContext context) {
            reader.readStartDocument();
            Map<K, V> map = getInstance();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                final K key = (K) Conversions.convert(String.class, keyType, reader.readName());
                if (reader.getCurrentBsonType() == BsonType.NULL) {
                    map.put(key, null);
                    reader.readNull();
                } else {
                    map.put(key, codec.decode(reader, context));
                }
            }
            reader.readEndDocument();
            return map;
        }

        @Override
        public Class<Map<K, V>> getEncoderClass() {
            return encoderClass;
        }

        private Map<K, V> getInstance() {
            if (encoderClass.isInterface()) {
                return new HashMap<>();
            }
            try {
                return encoderClass.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                throw new CodecConfigurationException(e.getMessage(), e);
            }
        }
    }

}

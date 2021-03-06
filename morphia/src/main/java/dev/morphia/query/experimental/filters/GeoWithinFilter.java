package dev.morphia.query.experimental.filters;

import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.MultiPolygon;
import com.mongodb.client.model.geojson.Polygon;
import dev.morphia.mapping.Mapper;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;

/**
 * Defines a $geoWithin filter.
 *
 * @morphia.internal
 * @since 2.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class GeoWithinFilter extends Filter {
    private CoordinateReferenceSystem crs;

    GeoWithinFilter(final String field, final Polygon value) {
        super("$geoWithin", field, value);
    }

    GeoWithinFilter(final String field, final MultiPolygon value) {
        super("$geoWithin", field, value);
    }

    /**
     * @param crs the CoordinateReferenceSystem to use
     * @return this
     */
    public GeoWithinFilter crs(final CoordinateReferenceSystem crs) {
        this.crs = crs;
        return this;
    }

    @Override
    public final void encode(final Mapper mapper, final BsonWriter writer, final EncoderContext context) {
        writer.writeStartDocument(field(mapper));
        writer.writeStartDocument(getFilterName());
        writer.writeName("$geometry");

        Object shape = getValue();
        Codec codec = mapper.getCodecRegistry().get(shape.getClass());
        codec.encode(writer, shape, context);

        writer.writeEndDocument();
        writer.writeEndDocument();
    }
}

package dev.morphia.query;

import dev.morphia.Datastore;
import org.bson.Document;

/**
 * A implementation of {@link QueryFactory} to create {@link LegacyQuery} instances.
 */
public class LegacyQueryFactory extends AbstractQueryFactory {
    @Override
    public <T> Query<T> createQuery(final Datastore datastore, final String collection, final Class<T> type) {
        return new LegacyQuery<>(datastore, collection, type);
    }

    @Override
    public <T> Query<T> createQuery(final Datastore datastore, final Class<T> type, final Document seed) {

        final LegacyQuery<T> query = new LegacyQuery<>(datastore, null, type);

        if (seed != null) {
            query.setQueryObject(seed);
        }

        return query;
    }

    @Override
    public <T> Query<T> createQuery(final Datastore datastore) {
        return new LegacyQuery<>(datastore);
    }
}

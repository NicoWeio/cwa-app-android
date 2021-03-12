package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.events.toTraceLocations
import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.eventregistration.storage.entity.toTraceLocationEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTraceLocationRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope
) : TraceLocationRepository {

    private val traceLocationDatabase: TraceLocationDatabase by lazy {
        traceLocationDatabaseFactory.create()
    }

    private val traceLocationDao: TraceLocationDao by lazy {
        traceLocationDatabase.traceLocationDao()
    }

    override val allTraceLocations: Flow<List<TraceLocation>>
        get() = traceLocationDao.allEntries().map { it.toTraceLocations() }

    override fun addTraceLocation(event: TraceLocation) {
        appScope.launch {
            Timber.d("Add hosted event: $event")
            val eventEntity = event.toTraceLocationEntity()
            traceLocationDao.insert(eventEntity)
        }
    }

    override fun deleteTraceLocation(event: TraceLocation) {
        appScope.launch {
            Timber.d("Delete hosted event: $event")
            val eventEntity = event.toTraceLocationEntity()
            traceLocationDao.delete(eventEntity)
        }
    }

    override fun deleteAllTraceLocations() {
        appScope.launch {
            Timber.d("Delete all hosted events.")
            traceLocationDao.deleteAll()
        }
    }
}

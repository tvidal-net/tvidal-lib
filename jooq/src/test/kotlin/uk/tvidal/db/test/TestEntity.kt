package uk.tvidal.db.test

import uk.tvidal.model.Entity
import uk.tvidal.model.MutableEntity
import java.time.Instant
import java.time.LocalDate

data class TestEntity(
    var name: String,
    var type: TestType,
    var date: LocalDate = LocalDate.now(),
    override var id: Int = 0,
    override var created: Instant? = null,
    override var modified: Instant? = null
) : Entity<Int>, MutableEntity
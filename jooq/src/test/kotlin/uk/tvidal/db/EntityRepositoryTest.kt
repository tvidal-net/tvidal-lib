package uk.tvidal.db

import org.junit.jupiter.api.Test
import uk.tvidal.db.test.TestEntity
import uk.tvidal.db.test.TestRepository
import uk.tvidal.db.test.TestType.FIRST
import uk.tvidal.db.test.TestType.SECOND
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class EntityRepositoryTest {

    private val test = TestRepository()

    @Test
    internal fun `basic crud operations`() {
        val new = TestEntity("test", FIRST, id = 0)
        assertNull(new.created)
        assertNull(new.modified)

        test.insert(new)
        assertNotNull(new.created)
        assertNull(new.modified)

        val id = new.id
        assertNotEquals(0, id)

        val inserted = test[id]
        assertNotNull(inserted)
        assertEquals(new, inserted)

        inserted.run {
            name = "updated"
            type = SECOND
        }
        test.update(inserted)
        assertNotNull(inserted.modified)

        val updated = test[id]
        assertNotNull(updated)
        assertEquals(inserted, updated)

        test.delete(updated)
        assertNull(test[id])
    }

    @Test
    internal fun `batch insert and list`() {
        val size = 20
        val new = (0 until size).map {
            TestEntity("Test $it", FIRST)
        }
        test.insert(new)

        val inserted = test.list()
        assertEquals(new, inserted)
    }
}
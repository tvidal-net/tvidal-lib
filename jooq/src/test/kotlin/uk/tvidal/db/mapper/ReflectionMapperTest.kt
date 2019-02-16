package uk.tvidal.db.mapper

import io.mockk.every
import io.mockk.mockk
import org.jooq.Record
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.fail

@Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST", "unused")
internal class ReflectionMapperTest {

    val record = mockk<Record>().also {
        every { it["test_name"] } returns "ok"
        every { it["test_id"] } returns 1
    }

    @Test
    fun `check for public constructor`() {
        assertThrows<IllegalArgumentException> {
            ReflectionMapper(NoPublicConstructor::class)
        }
    }

    @Test
    fun `use the largest constructor`() {
        ReflectionMapper(LargestConstructor::class)
    }

    @Test
    fun `reads simple values from the database`() {
        val mapper = ReflectionMapper(TestEntity::class)

        val entity = mapper.fromRecord(record)
        val expected = TestEntity("ok").apply { testId = 1 }
        assertEquals(expected, entity)
        assertEquals(expected.testId, entity.testId)
    }

    @Test
    fun `resolves properties using the provided resolver`() {
        every { record["test_name"] } returns "fail"
        val resolver = mockk<PropertyResolver<String>>().also {
            every { it.fromDatabase("fail") } returns "pass"
        }
        val resolverMap = mapOf(
            String::class.java to resolver
        ) as Map<Class<*>, PropertyResolver<*>>
        val mapper = ReflectionMapper(TestEntity::class, resolverMap)
        val entity = mapper.fromRecord(record)
        val expected = TestEntity("pass")
        assertEquals(expected, entity)
    }

    class NoPublicConstructor private constructor()

    class LargestConstructor(test: String, id: Number) {
        constructor(test: String) : this(test, 0) {
            fail()
        }
    }

    data class TestEntity(val testName: String) {
        var testId: Number = 0
    }
}
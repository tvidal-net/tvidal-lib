package uk.tvidal.db.test

import org.jooq.UniqueKey
import org.jooq.impl.DSL.name
import org.jooq.impl.EnumConverter
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.SQLDataType.BIGINT
import org.jooq.impl.SQLDataType.INTEGER
import org.jooq.impl.SQLDataType.TIMESTAMP
import org.jooq.impl.SQLDataType.VARCHAR
import org.jooq.impl.SequenceImpl
import org.jooq.impl.TableImpl
import org.jooq.impl.UpdatableRecordImpl
import uk.tvidal.db.converter.InstantConverter
import uk.tvidal.db.converter.LocalDateConverter
import uk.tvidal.db.test.TestTable.TestRecord

object TestTable : TableImpl<TestRecord>(name("test_table")) {

    val ID = createField("id", INTEGER.nullable(false).identity(true))!!
    val CREATED = createField("created", TIMESTAMP.nullable(false), "", InstantConverter())!!
    val MODIFIED = createField("modified", TIMESTAMP, "", InstantConverter())!!
    val NAME = createField("name", VARCHAR.nullable(false))!!
    val TYPE = createField("type", VARCHAR, "", EnumConverter(String::class.java, TestType::class.java))!!
    val DATE = createField("date", SQLDataType.DATE.nullable(false), "", LocalDateConverter())!!

    val TEST_SEQUENCE = SequenceImpl("test_sequence", schema, BIGINT)

    private val internalPrimaryKey = Internal.createUniqueKey(this, ID)

    override fun getPrimaryKey(): UniqueKey<TestRecord> = internalPrimaryKey

    override fun getRecordType(): Class<out TestRecord> = TestRecord::class.java

    class TestRecord : UpdatableRecordImpl<TestRecord>(TestTable)
}
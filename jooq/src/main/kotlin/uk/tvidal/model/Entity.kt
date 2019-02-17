package uk.tvidal.model

import java.time.Instant

interface Entity<ID> {
    var id: ID
    var created: Instant?
}
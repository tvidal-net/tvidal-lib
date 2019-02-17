package uk.tvidal.model

import java.time.Instant

interface MutableEntity {
    var modified: Instant?
}
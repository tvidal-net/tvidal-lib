package uk.tvidal.model

interface TreeEntity<ID, T : TreeEntity<ID, T>> : Entity<ID> {
    var parent: ID?
    val children: MutableList<T>
}
package tedzs

/**
  * A trait encapsulating the different edit operations - inserting, deleting, or changing a node.
  */
sealed trait Edit[A]

final case class Insert[A](node: Node[A]) extends Edit[A] {
  override  def toString = s"Insert(${node.label})"
}

final case class Delete[A](node: Node[A]) extends Edit[A] {
  override def toString: String = s"Delete(${node.label}"
}

final case class Change[A](from: Node[A], to: Node[A]) extends Edit[A] {
  override def toString: String = s"Change(${from.label}, ${to.label})"
}
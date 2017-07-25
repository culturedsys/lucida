/**
  * A Node, that is, a label and an ordered sequence (possible empty) of child nodes.
  */
case class Node[A](label: A, children: Seq[Node[A]]) {
  /**
    * Return the nodes in the tree rooted at this node, in tree postorder
    */
  def postOrder: Vector[Node[A]] = {
    def helper(acc: Vector[Node[A]], stack: Seq[Node[A]]): Vector[Node[A]] = stack match {
      case Seq() => acc
      case head +: rest =>
        helper(head +: acc, head.children.reverse ++ rest)
    }

    helper(Vector(), Seq(this))
  }

  /**
    * Return, in postorder, a list of paths from each node to the root.
    */
  def postOrderPaths: Vector[Seq[Node[A]]] = {
    def helper(acc: Vector[Seq[Node[A]]],
               stack: Seq[Vector[Node[A]]]): Vector[Seq[Node[A]]] =
      stack match {
        case Seq() => acc
        case path +: rest =>
          val newAcc = path +: acc
          val newStack = path.head.children.reverse.map(_ +: path) ++ rest
          helper(newAcc, newStack)
      }
    helper(Vector(), Seq(Vector(this)))
  }

  /**
    * Return an array, in postorder, of the postorder index of the left-most leaf descendant of
    * each node in this tree.  That is, `leftMostDescendants(i)` gives the postorder index of the
    * left-most leaf descendant of the node with postorder index `i`.
    */
  def leftMostDescendants: Vector[Int] = Node.leftMostDescendants(postOrderPaths)

  /**
    * Return the postorder indexes of the keyroots of the tree, in increasing order. Keyroots are
    * those nodes where there is not node with a later postorder index which has the same
    * left-most descendant; this is equivalent to the nodes which have left siblings.
    */
  def keyroots: Seq[Int] = Node.keyroots(leftMostDescendants)
}

object Node {
  def leftMostDescendants[A](paths: Vector[Seq[Node[A]]]): Vector[Int] = {
    def helper(acc: Vector[Int], leafForNode: Map[Node[A], Int],
               paths: Vector[(Seq[Node[A]], Int)]): Vector[Int] =
      paths match {
        case Vector() => acc
        case (path, index) +: rest =>
          val (leaf, leafMap) =
            if (path.head.children.isEmpty) {
              (index, path.tail.map(_ -> index).toMap ++ leafForNode)
            } else {
              (leafForNode(path.head), leafForNode)
            }
          helper(acc :+ leaf, leafMap, rest)
      }
    helper(Vector(), Map(), paths.zipWithIndex)
  }

  def keyroots(lmds: Seq[Int]): Seq[Int] = {
    val roots = lmds.zipWithIndex.foldLeft(Map[Int, Int]()) { (roots, lmdAndIndex) =>
      roots + lmdAndIndex
    }
    roots.values.toSeq.sorted
  }
}

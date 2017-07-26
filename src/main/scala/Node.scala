/**
  * A Node, that is, a label and an ordered sequence (possible empty) of child nodes.
  */
case class Node[+A](label: A, children: Seq[Node[A]]) {
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

  /**
    * Calculate the edit distance between this node and another node.
    */
  def distance[B >: A](other: Node[B])
              (implicit costs: Costs[B]): Int = Node.distance(this, other)
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

  /**
    * Return the tree edit distance between the two trees `tree1` and `tree2`. `costs` is an
    * object giving the cost of inserting, deleting, and changing nodes; an implicit
    * implementation (depending on the type of the node label) will be used if available.
    */
  def distance[A](tree1: Node[A], tree2: Node[A])
              (implicit costs: Costs[A]): Int = {
    val treeDists = treeDistanceMatrix(tree1, tree2)(costs)
    treeDists.last.last
  }

  /**
    * Calculate the matrix of tree edit distances between subtrees of `tree1` and `tree2`. This is
    * a two-dimensional array where the array indexes are the post-order positions of root nodes
    * in each tree, e.g. `treeDistanceMatrix(t1, t2)(n)(m)` gives the edit distance between the
    * tree rooted at node `n` in `t1` and node `m` in `t1`.
    *
    * Implements the algorithm of Zhang and Shasha (1989).
    */
  def treeDistanceMatrix[A](tree1: Node[A], tree2: Node[A])(implicit costs: Costs[A]):
      Array[Array[Int]] = {
    val paths1 = tree1.postOrderPaths
    val paths2 = tree2.postOrderPaths

    val nodes1 = paths1.map(_.head)
    val nodes2 = paths2.map(_.head)

    val lmds1 = leftMostDescendants(paths1)
    val lmds2 = leftMostDescendants(paths2)

    val keyroots1 = keyroots(lmds1)
    val keyroots2 = keyroots(lmds2)

    val treeDists = Array.ofDim[Int](nodes1.length, nodes2.length)

    for (i <- keyroots1)
      for (j <- keyroots2)
        calculateTreeDist(i, j)

    // Calculate the tree edit distances for the trees starting at node `i` in `tree1` and node
    // `j` in `tree2`. The results are stored in the `treeDist` array.
    def calculateTreeDist(i: Int, j: Int): Unit = {
      val left1 = lmds1(i)
      val left2 = lmds2(j)
      val forestDists = ForestDistanceMatrix(left1 to i, left2 to j)

      forestDists(left1 - 1, left2 - 1) = 0

      for (i1 <- left1 to i)
        forestDists(i1, left2 - 1) = forestDists(i1 - 1, left2 -1) + costs.delete(nodes1(i1))
      for (j1 <- left2 to j)
        forestDists(left1 - 1, j1) =  forestDists(left1 - 1, j1 - 1) + costs.insert(nodes2(j1))

      for (i1 <- left1 to i)
        for (j1 <- left2 to j) {
          val deleteCost = forestDists(i1 - 1, j1) + costs.delete(nodes1(i1))
          val insertCost = forestDists(i1, j1 - 1) + costs.insert(nodes2(j1))

          if (lmds1(i1) == lmds1(i) && lmds2(j1) == lmds2(j)) {
            val changeCost = forestDists(i1 - 1, j1 - 1) + costs.change(nodes1(i1), nodes2(j1))
            val minCost = math.min(deleteCost, math.min(insertCost, changeCost))
            forestDists(i1, j1) = minCost
            treeDists(i1)(j1) = minCost
          } else {
            val changeCost = forestDists(lmds1(i1) - 1, lmds2(j1) - 1) + treeDists(i1)(j1)
            val minCost = math.min(deleteCost, math.min(insertCost, changeCost))
            forestDists(i1, j1) = minCost
          }
        }
    }

    treeDists
  }
}

/**
  * A trait encapsulating a set of insert, delete, and change costs for `Node`s of type `A`
  */
trait Costs[-A] {
  def insert(node: Node[A]): Int

  def delete(node: Node[A]): Int

  def change(from: Node[A], to: Node[A]): Int
}

object Costs {
  implicit object IntCosts extends Costs[Int] {
    def insert(node: Node[Int]): Int = node.label

    def delete(node: Node[Int]): Int = node.label

    def change(from: Node[Int], to: Node[Int]): Int = math.abs(from.label - to.label)
  }

  object TrivialCosts extends Costs[Any] {
    def insert(node: Node[Any]): Int = 1
    def delete(node: Node[Any]): Int = 1
    def change(from: Node[Any], to: Node[Any]): Int = if (from.label == to.label) 0 else 1
  }
}

/**
  * A trait encapsulating the different edit operations - inserting, deleting, or changing a node.
  */
sealed trait Edit[A]
final case class Insert[A](node: Node[A]) extends Edit[A]
final case class Delete[A](node: Node[A]) extends Edit[A]
final case class Change[A](from: Node[A], to: Node[A]) extends Edit[A]
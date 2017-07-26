/*
 * Copyright 2017 Tim Fisken
 *
 * This file is part of ted-zs-scala.
 *
 * ted-zs-scala is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ted-zs-scala is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with ted-zs-scala.  If
 * not, see <http://www.gnu.org/licenses/>
 */

package tedzs

/**
  * A node, that is, a label and an ordered sequence (possibly empty) of child nodes.
  */
trait Node[+A] {

  def label: A
  def children: Seq[Node[A]]

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
    * those nodes where there is no node with a later postorder index which has the same
    * left-most descendant; this is equivalent to the nodes which have left siblings.
    */
  def keyroots: Seq[Int] = Node.keyroots(leftMostDescendants)

  /**
    * Calculate the edit distance between this node and another node.
    */
  def distance[B >: A](other: Node[B])
              (implicit costs: Costs[B]): Int = Node.distance(this, other)

  /**
    * Calculate the mapping from this node to another node.
    */
  def mapping[B >: A](other: Node[B])(implicit costs: Costs[B]): Seq[Edit[B]] =
    Node.mapping(this, other)
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
    * Convert a tree into a sequence of nodes in postorder, and a sequence of corresponding
    * left-most descendents.
    */
  def preProcess[A](tree: Node[A]):
      (Seq[Node[A]], Seq[Int]) = {
    val paths = tree.postOrderPaths
    val nodes = paths.map(_.head)
    val lmds = leftMostDescendants(paths)

    (nodes, lmds)
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

    val (nodes1, lmds1) = preProcess(tree1)
    val (nodes2, lmds2) = preProcess(tree2)

    treeDistanceMatrix(nodes1, lmds1, nodes2, lmds2)
  }

  def treeDistanceMatrix[A](nodes1: Seq[Node[A]], lmds1: Seq[Int],
                            nodes2: Seq[Node[A]], lmds2: Seq[Int])
                           (implicit costs: Costs[A]): Array[Array[Int]] = {
    val keyroots1 = keyroots(lmds1)
    val keyroots2 = keyroots(lmds2)

    val treeDists = Array.ofDim[Int](nodes1.length, nodes2.length)

    for {
      i <- keyroots1
      j <- keyroots2
    } forestDistanceMatrix(i, j, nodes1, lmds1, nodes2, lmds2, treeDists)

    treeDists
  }

  /**
    * Calculate the edit distances between the forests in two subtrees trees, stretching from the
    * left-most descendant `i` to `i`, in one tree, and the left-most descendant of `j` to `j` in
    * the other tree.
    *
    * @param nodes1 all nodes in the first tree, in post order.
    * @param lmds1 the left-most descendant corresponding to each node in the `nodes1` sequence.
    * @param nodes2 all nodes in the second tree, in post order.
    * @param lmds2 the left-most descendant corresponding to each node in the `nodes2` sequence.
    */
  def forestDistanceMatrix[A](i: Int, j: Int, nodes1: Seq[Node[A]], lmds1: Seq[Int],
                         nodes2: Seq[Node[A]], lmds2: Seq[Int],
                         treeDists: Array[Array[Int]])
                        (implicit costs: Costs[A]): ForestDistanceMatrix = {
    val left1 = lmds1(i)
    val left2 = lmds2(j)
    val forestDists = ForestDistanceMatrix(left1 to i, left2 to j)

    forestDists(left1 - 1, left2 - 1) = 0

    for (i1 <- left1 to i)
      forestDists(i1, left2 - 1) = forestDists(i1 - 1, left2 -1) + costs.delete(nodes1(i1))
    for (j1 <- left2 to j)
      forestDists(left1 - 1, j1) =  forestDists(left1 - 1, j1 - 1) + costs.insert(nodes2(j1))

    for {
      i1 <- left1 to i
      j1 <- left2 to j
    } {
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

    forestDists
  }

  /**
    * Calculate a mapping between `tree1` and `tree2`, i.e., a sequence of edit operations that
    * transforms `tree1` into `tree2`
    */
  def mapping[A](tree1: Node[A], tree2: Node[A])(implicit costs: Costs[A]): Seq[Edit[A]] = {
    val (nodes1, lmds1) = preProcess(tree1)
    val (nodes2, lmds2) = preProcess(tree2)

    mapping(nodes1, lmds1, nodes2, lmds2)(costs)
  }

  def mapping[A](nodes1: Seq[Node[A]], lmds1: Seq[Int], nodes2: Seq[Node[A]], lmds2: Seq[Int])
                (implicit costs: Costs[A]): Seq[Edit[A]] = {
    val treeDists = treeDistanceMatrix(nodes1, lmds1, nodes2, lmds2)

    var edits = Seq[Edit[A]]()
    var toCompare= Seq((nodes1.length - 1, nodes2.length - 1))

    while (toCompare.nonEmpty) {
      val (i, j) = toCompare.head
      toCompare = toCompare.tail

      val forestDists = forestDistanceMatrix(i, j, nodes1, lmds1, nodes2, lmds2, treeDists)

      var row = i
      var col = j

      // The row and column in the forestDists matrix representing the empty forest
      val emptyRow = forestDists.rowBase - 1
      val emptyCol = forestDists.colBase - 1

      while ( row > emptyRow || col > emptyCol ) {
        if (row > emptyRow &&
            forestDists(row - 1, col) + costs.delete(nodes1(row)) == forestDists(row, col)) {
          edits = Delete(nodes1(row)) +: edits
          row -= 1
        } else if (col > emptyCol &&
            forestDists(row, col - 1) + costs.insert(nodes2(col)) == forestDists(row, col)) {
          edits = Insert(nodes2(col)) +: edits
          col -= 1
        } else if (lmds1(row) == lmds1(i) && lmds2(col) == lmds2(j)) {
          val n1 = nodes1(row)
          val n2 = nodes2(col)
          if (n1.label != n2.label)
            edits = Change(nodes1(row), nodes2(col)) +: edits
          row -= 1
          col -= 1
        } else {
          toCompare = (row, col) +: toCompare
          row = lmds1(row) - 1
          col = lmds2(col) - 1
        }
      }
    }
    edits
  }
}



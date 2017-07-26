# Zhang Shasha Tree Edit Distance

A Scala implementation of Zhang & Shasha (1989)'s tree edit distance algorithm. 

Tree Edit Distance is the number of edit operations (insert a node, delete a node, or change the 
label on a node) required to change one tree to another. Zhang and Shasha put forward an 
algorithm for calculating the minimum edit distance which is reasonable efficient and reasonably 
straightforward. For two trees, one with n nodes, of depth d, and with l leaves, and another with m nodes, of depth e 
and with k leaves, Zhang and Shasha's algorithm finds the distance in 
O(n * m * min(d, l) * min(e, k)) time, and O(n * m) space. While working out the edit distance, 
the algorithm also works out what edit operation are used, and this list of edit operations 
(called a *mapping*) can also be retrieved using this library.

Zhang and Shasha's algorithm is defined on ordered trees, where a tree is made up of nodes, each 
of which has a label and an ordered list of children (this list may be empty, in which case the 
node is a leaf). This is represented in this library with a trait, parameterized on the type of 
the label:

```scala
trait Node[A] {
  def label: A
  def children: Seq[Node[A]]
}
```

The primary functions are defined on `Node`'s companion object: `Node.distance(tree1, tree2)` 
returns the minimum edit distance between `tree1` and `tree2`, while `Node.mapping(tree1, tree2)`
returns a list of editing operations that will transform `tree1` into `tree2`. 

In calculating the minimum edit distance, the algorithm needs to know the cost of each operation.
This is supplied as an implicit parameter to the `distance` and `mapping` functions, an 
implementation of the `Costs` trait (again parameterized by label type):
 
```scala
trait Costs[A] {
  def insert(node: Node[A]): Int
  def delete(node: Node[A]): Int
  def change(from: Node[A], to: Node[A]): Int
}
```

The library currently provides an implicit implementation of `Costs` for `Int` labels, where the 
cost of inserting and deleting a node is the value of its label, and the cost for changing a 
label the difference between the two labels. The library also provides an implementation which 
works for any type, setting the cost of all operations as 1 (except for changes where the two 
nodes have the same label, in which case the cost is 0). This implementation, `TrivialCosts`, can
be passed explicitly to the `distance` and `mapping` methods.
 
## References

Zhang and Shasha's original description of their algorithm is in: K. Zhang and D. Shasha, ‘[Simple
Fast Algorithms for the Editing Distance between Trees and Related Problems](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.460.5601)’, *SIAM Journal on 
Computing*, vol. 18, no. 6, p. 18, Dec. 1989.  


I also want to acknowledge Tim Henderson and Steve Johnson's [Python implementation of 
Zhang-Shasha](https://github.com/timtadh/zhang-shasha), which I found particularly helpful in 
figuring out how to efficiently calculate the left-most descendant node of each node, and Mateusz
Pawlik and Nikolaus Augsten's [APTED](https://github.com/DatabaseGroup/apted), a more complicated
(and more efficient) tree edit distance algorithm, which I consulted to figure out how to derive 
the list of edit operations from the calculations involved in working out the edit distance.

package tedzs

/**
  * The simplest node, which just contains a label and a list of children
  */
case class SimpleNode[A](label: A, children: Seq[SimpleNode[A]]) extends Node[A]

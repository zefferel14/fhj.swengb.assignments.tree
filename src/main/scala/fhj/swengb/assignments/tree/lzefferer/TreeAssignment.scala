package fhj.swengb.assignments.tree.lzefferer

import javafx.scene.paint.Color


import scala.math.BigDecimal.RoundingMode
import scala.util.Random

object Graph {

  val colorMap =
    Map[Int, Color](
      0 -> Color.ROSYBROWN,
      1 -> Color.BROWN,
      2 -> Color.SADDLEBROWN,
      3 -> Color.INDIANRED,
      4 -> Color.DARKGREEN,
      5 -> Color.GREEN,
      6 -> Color.YELLOWGREEN,
      7 -> Color.GREENYELLOW,
      8 -> Color.YELLOW
    )

  /**
   * creates a random tree
   *
   * @param pt
   * @return
   */
  def randomTree(pt: Pt2D): Tree[L2D] =
    mkGraph(pt, Random.nextInt(360), Random.nextDouble() * 150, Random.nextInt(7))


  /**
   * Given a Tree of L2D's and a function which can convert any L2D to a Line,
   * you have to traverse the tree (visit all nodes) and create a sequence
   * of Line's. The ordering of the lines is not important.
   *
   * @param tree  a tree which contains L2D instances
   * @param convert a converter function
   * @return
   */
  def traverse[A, B](tree: Tree[A])(convert: A => B): Seq[B] = tree match {
    case Node(value) => Seq(convert(value))
    case Branch(left,right) => traverse(left)(convert) ++ traverse(right)(convert)
  }

  /**
   * Creates a tree graph.
   *
   * @param start the startpoint (root) of the tree
   * @param initialAngle initial angle of the tree
   * @param length the initial length of the tree
   * @param treeDepth the depth of the tree
   * @param factor the factor which the length is decreasing for every iteration
   * @param angle the angle between a branch and the root
   * @param colorMap color map, by default it is the colormap given in the companion object Graph
   *
   * @return a Tree[L2D] which can be traversed by other algorithms
   */
  def mkGraph(start: Pt2D,
              initialAngle: AngleInDegrees,
              length: Double,
              treeDepth: Int,
              factor: Double = 0.75,
              angle: Double = 20.0,
              colorMap: Map[Int, Color] = Graph.colorMap): Tree[L2D] = {
    assert(treeDepth <= colorMap.size, s"Treedepth higher than color mappings - bailing out ...")

    def constructGraph(start: L2D, acc: Int): Tree[L2D] = acc match {
      case nothign if treeDepth == 0    => Node(start)
      case node    if treeDepth == acc  => Branch(Node(start), Branch(Node(start.left(factor, angle, colorMap(acc - 1))), Node(start.right(factor, angle, colorMap(acc - 1)))))
      case _                            => Branch(Node(start), Branch(constructGraph(start.left(factor, angle, colorMap(acc - 1)), acc + 1), constructGraph(start.right(factor, angle, colorMap(acc - 1)), acc + 1)))
    }

    constructGraph(L2D(start,initialAngle,length,colorMap(0)),1)

  }
}

object MathUtil {

  /**
   * rounds the given value to 3 decimal places.
   *
   * @param value  a double value
   * @return
   *
   * %.2f".format(x).toDouble
   */
  def round(value: Double): Double = {
    val roundedValue = BigDecimal(value).setScale(3, BigDecimal.RoundingMode.HALF_UP).toDouble
    return roundedValue
  }
  /**
   * turns an angle given in degrees to a value in radiants.
   *
   * @param angle
   * @return
   */
  def toRadiants(angle: AngleInDegrees): AngleInRadiants = angle.toRadians

}
object L2D {

  import MathUtil._

  /**
   * Given a startpoint, an angle and a length the endpoint of the line
   * is calculated and finally a L2D class is returned.
   *
   * @param start the startpoint
   * @param angle the angle
   * @param length the length of the line
   * @param color the color
   * @return
   */
  def apply(start: Pt2D, angle: AngleInDegrees, length: Double, color: Color): L2D = {
    val a = round(start.x + Math.cos(toRadiants(angle))*length)
    val b = round(start.y + Math.sin(toRadiants(angle))*length)
    val end = Pt2D(a,b)
    return L2D(start:Pt2D,end,color)
  }


}

case class L2D(start: Pt2D, end: Pt2D, color: Color) {

  lazy val xDist = end.x - start.x
  lazy val yDist = end.y - start.y

  lazy val angle = {
    assert(!((xDist == 0) && (yDist == 0)))
    (xDist, yDist) match {
      case (x, 0) if x > 0 => 0
      case (0, y) if y > 0 => 90
      case (0, y) if y < 0 => 270
      case (x, 0) if x < 0 => 180
      case (x, y) if x < 0 && y < 0 => Math.atan(y / x) * 180 / Math.PI + 180
      case (x, y) if x < 0 && y > 0 => Math.atan(y / x) * 180 / Math.PI + 180
      case (x, y) if x > 0 && y < 0 => Math.atan(y / x) * 180 / Math.PI + 360
      case (x, y) => Math.atan(y / x) * 180 / Math.PI
    }
  }

  lazy val length: Double = {
    Math.sqrt(xDist * xDist + yDist * yDist)
  }

  def left(factor: Double, deltaAngle: AngleInDegrees, c: Color): L2D = {
    L2D(end, angle - deltaAngle, length * factor, c)
  }

  def right(factor: Double, deltaAngle: AngleInDegrees, c: Color): L2D = {
    L2D(end, angle + deltaAngle, length * factor, c)
  }


}

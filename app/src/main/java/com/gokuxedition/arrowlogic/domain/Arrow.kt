package com.gokuxedition.arrowlogic.domain

data class Cell(val x: Int, val y: Int)

/**
 * Arrow head is the cell nearest the escape edge. Occupied cells extend backwards
 * from the head opposite the travel direction, matching the canonical puzzle contract.
 */
data class Arrow(
    val id: String,
    val head: Cell,
    val direction: Direction,
    val length: Int
) {
    init {
        require(id.isNotBlank()) { "Arrow id must not be blank" }
        require(length > 0) { "Arrow length must be positive" }
    }

    /** Occupied cells ordered from head backwards toward the tail. */
    fun occupiedCells(): List<Cell> =
        (0 until length).map { offset ->
            Cell(head.x - direction.dx * offset, head.y - direction.dy * offset)
        }
}

data class Board(
    val width: Int,
    val height: Int,
    val arrows: List<Arrow>
) {
    init {
        require(width > 0 && height > 0) { "Board dimensions must be positive" }
        require(arrows.map { it.id }.distinct().size == arrows.size) { "Arrow ids must be unique" }
    }
}

package com.gokuxedition.arrowlogic.domain

data class Cell(val x: Int, val y: Int)

data class Arrow(
    val id: String,
    val tail: Cell,
    val direction: Direction,
    val length: Int
) {
    init {
        require(id.isNotBlank()) { "Arrow id must not be blank" }
        require(length > 0) { "Arrow length must be positive" }
    }

    /** Occupied cells ordered from tail to head. */
    fun occupiedCells(): List<Cell> =
        (0 until length).map { offset ->
            Cell(tail.x + direction.dx * offset, tail.y + direction.dy * offset)
        }

    fun head(): Cell = Cell(
        tail.x + direction.dx * (length - 1),
        tail.y + direction.dy * (length - 1)
    )
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

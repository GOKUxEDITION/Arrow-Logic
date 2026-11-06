package com.gokuxedition.arrowlogic.domain

object EscapeRules {
    fun validateBoard(board: Board): List<String> {
        val errors = mutableListOf<String>()
        val occupied = mutableMapOf<Cell, String>()
        board.arrows.forEach { arrow ->
            arrow.occupiedCells().forEach { cell ->
                if (cell.x !in 0 until board.width || cell.y !in 0 until board.height) {
                    errors += "Arrow ${arrow.id} has out-of-bounds cell ${cell.x},${cell.y}"
                }
                val previous = occupied.put(cell, arrow.id)
                if (previous != null && previous != arrow.id) {
                    errors += "Arrows $previous and ${arrow.id} overlap at ${cell.x},${cell.y}"
                }
            }
        }
        return errors.distinct()
    }

    fun isClear(board: Board, arrowId: String): Boolean {
        val arrow = board.arrows.firstOrNull { it.id == arrowId } ?: return false
        val occupiedByOthers = board.arrows
            .asSequence()
            .filterNot { it.id == arrowId }
            .flatMap { it.occupiedCells().asSequence() }
            .toHashSet()

        val head = arrow.head()
        var x = head.x + arrow.direction.dx
        var y = head.y + arrow.direction.dy
        while (x in 0 until board.width && y in 0 until board.height) {
            if (Cell(x, y) in occupiedByOthers) return false
            x += arrow.direction.dx
            y += arrow.direction.dy
        }
        return true
    }

    fun legalArrowIds(board: Board): List<String> =
        board.arrows.filter { isClear(board, it.id) }.map { it.id }
}

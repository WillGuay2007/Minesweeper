package minesweeper

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.view.children
import ca.william.minesweeper.R
import ca.william.minesweeper.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cell(
    var exposed: Boolean = false,
    var containsMine : Boolean = false,
    var hasFlag : Boolean = false
) : Parcelable

@Parcelize
data class Model(val grid: Array<Cell>) : Parcelable


class MainActivity : Activity() {

    companion object {
        const val TAG = "MainActivity"
        const val NB_COLUMNS = 10
        const val NB_ROWS = 10
        const val GRID_SIZE = NB_COLUMNS * NB_ROWS
    }

    var numberOfMines : Int = 6
    var gameEnded = false
    var hasLost = false
    var hasWon = false

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var model = Model(Array(NB_COLUMNS * NB_ROWS) { Cell() })

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeGame()

        binding.grid.children.forEachIndexed { index:Int, button: View ->

            button.setOnClickListener {

                onButtonClicked(index)
            }

            button.setOnLongClickListener {

                if (!gameEnded) {
                    model.grid[index].hasFlag = !model.grid[index].hasFlag //Un toggle
                    refresh()
                }
                true // prevents regular click
            }
        }

        binding.NewGame.setOnClickListener {
            resetGame()
        }

    }

    fun onButtonClicked(index:Int) {

        if (gameEnded) {
            return
        }

        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].exposed)
            return

        if (model.grid[index].hasFlag) {
            return
        }

        if (model.grid[index].containsMine) {
            hasLost = true
            gameEnded = true
            refresh()
            return
        }

        model.grid[index].exposed = true




        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].exposed }
        var howManyAdjacentMines = getNeighbors(index).count { model.grid[it].containsMine }

        if (howManyAdjacentMines == 0) {
            getNeighbors(index).forEach { onButtonClicked(it) }
        }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors, howManyAdjacentMines = $howManyAdjacentMines")

        checkIfWon()

        refresh()
    }

    private fun getNeighbors(index: Int): List<Int> {

        val (x, y) = index.toCoords()
        return listOf(
            Pair(x - 1, y - 1),
            Pair(x, y - 1),
            Pair(x + 1, y - 1),
            Pair(x - 1, y),
            Pair(x + 1, y),
            Pair(x - 1, y + 1),
            Pair(x, y + 1),
            Pair(x + 1, y + 1)
        ).mapNotNull { it.toIndex() }
    }


    fun refresh() {


        //Mes notes: Le bouton = UI ---- La cell = la DataClass
        binding.grid.children.forEachIndexed{ index, button ->

            var cell = model.grid[index]

            var adjacentMines = getNeighbors(index).count {model.grid[it].containsMine}
            button.setBackgroundResource(
                if (hasLost && cell.containsMine) R.drawable.mine
                else if (hasLost && cell.hasFlag && !cell.containsMine) R.drawable.wrong_mine
                else if (cell.exposed)
                    when (adjacentMines) {
                        1 -> R.drawable.one_mine
                        2 -> R.drawable.two_mine
                        3 -> R.drawable.three_mine
                        4 -> R.drawable.four_mine
                        5 -> R.drawable.five_mine
                        6 -> R.drawable.six_mine
                        7 -> R.drawable.seven_mine
                        8 -> R.drawable.eight_mine
                        else -> R.drawable.btn_down
                    }
                else if (cell.hasFlag) R.drawable.flag
                else R.drawable.btn_up
            )
        }
        val remainingMines = "Mines restantes: ${numberOfMines - model.grid.count { it.hasFlag }}"
        binding.MinesInfo.text =
            if (gameEnded && hasLost) "Vous avez perdu.."
            else if (gameEnded && hasWon) "Vous avez gagné!"
            else remainingMines
    }

    private fun resetGame() {
        model.grid.forEach {
            it.containsMine = false
            it.exposed = false
            it.hasFlag = false
            gameEnded = false
            hasLost = false
            hasWon = false
        }
        initializeGame()
        refresh()
    }

    private fun checkIfWon() {
        var exposedMines = model.grid.count { it.exposed }
        if (exposedMines == GRID_SIZE - numberOfMines) {
            gameEnded = true
            hasWon = true
        }
    }

    private fun initializeGame() {
        //Donner des mines aux tuiles
        if (numberOfMines > GRID_SIZE ) numberOfMines = GRID_SIZE //Pour pas que le while crash
        var initializedMines = 0
        while (initializedMines < numberOfMines) {
            var randomCell : Cell = model.grid.random()
            if (randomCell.containsMine) continue
            randomCell.containsMine = true
            initializedMines += 1
        }
    }

    private fun Int.toCoords() = Pair(this % NB_COLUMNS, this / NB_COLUMNS)

    private fun Pair<Int, Int>.toIndex() =
        if (this.first < 0 || this.first >= NB_COLUMNS ||
            this.second < 0 || this.second >= NB_ROWS)
            null
        else
            this.second * NB_COLUMNS + this.first


}




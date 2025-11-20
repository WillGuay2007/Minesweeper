package ca.bart.guifra.minesweeper

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import ca.bart.guifra.minesweeper.MainActivity.Companion.NUMBER_OF_MINES
import ca.bart.guifra.minesweeper.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cell(
    var exposed: Boolean = false,
    var containsMine : Boolean = false
) : Parcelable

@Parcelize
data class Model(val grid: Array<Cell>) : Parcelable


class MainActivity : Activity() {

    companion object {

        const val NUMBER_OF_MINES : Int = 5
        const val TAG = "MainActivity"
        const val NB_COLUMNS = 5
        const val NB_ROWS = 5
        const val GRID_SIZE = NB_COLUMNS * NB_ROWS
    }

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var model = Model(Array(NB_COLUMNS * NB_ROWS) { Cell() })

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Donner des mines aux tuiles
        var initializedMines = 0
        while (initializedMines < NUMBER_OF_MINES) {
            var randomCell : Cell = model.grid.random()
            if (randomCell.containsMine) continue
            randomCell.containsMine = true
            initializedMines += 1

        }

        binding.grid.children.forEachIndexed { index:Int, button: View ->

            button.setOnClickListener {

                onButtonClicked(index)
            }

            button.setOnLongClickListener {


                true // prevents regular click
            }
        }



    }

    fun onButtonClicked(index:Int) {

        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].exposed)
            return

        if (model.grid[index].containsMine) {
            return
        }

        model.grid[index].exposed = true




        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].exposed }
        var howManyAdjacentMines = getNeighbors(index).count { model.grid[it].containsMine }

        if (howManyAdjacentMines == 0) {
            getNeighbors(index).forEach { onButtonClicked(it) }
        }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors, howManyAdjacentMines = $howManyAdjacentMines")


        //getNeighbors(index).forEach { onButtonClicked(it) }



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
        (binding.grid.children zip model.grid.asSequence()).forEach { (button, cell) ->

            var adjacentMines = getNeighbors(model.grid.indexOf(cell)).count {model.grid[it].containsMine}
            button.setBackgroundResource(
                if (cell.exposed)
                    if (adjacentMines > 0) {
                        if (adjacentMines == 1) R.drawable.one_mine else R.drawable.btn_down
                    } else {
                        R.drawable.btn_down
                    }
                else if (cell.containsMine)
                    R.drawable.mine
                else R.drawable.btn_up
            )
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




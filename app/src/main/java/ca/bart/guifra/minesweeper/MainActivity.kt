package ca.bart.guifra.minesweeper

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.view.children
import ca.bart.guifra.minesweeper.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cell(var exposed: Boolean = false) : Parcelable

@Parcelize
data class Model(val grid: Array<Cell>) : Parcelable

class MainActivity : Activity() {

    companion object {

        const val TAG = "MainActivity"

        const val NB_COLUMNS = 3
        const val NB_ROWS = 3
    }

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var model = Model(Array(NB_COLUMNS * NB_ROWS) { Cell() })

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

        model.grid[index].exposed = true




        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].exposed }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors")


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

        (binding.grid.children zip model.grid.asSequence()).forEach { (button, cell) ->

            button.setBackgroundResource(
                if (cell.exposed)
                    R.drawable.btn_down
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




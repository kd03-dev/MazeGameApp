package com.example.mazegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Switch
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class GameView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var cells = arrayOf<Array<Cell>>()
    private lateinit var playerCell : Cell; private lateinit var exitCell : Cell
    private var cellSize : Float = 0.0F; private var horMargin : Float = 0.0F; private var verMargin : Float = 0.0F
    private enum class Directions {
        UP, DOWN, RIGHT, LEFT
    }
    private var wallPaint : Paint = Paint()
    private var playerPaint: Paint = Paint(); private var exitPaint : Paint = Paint()
    private var random : Random = Random()

    private companion object {
        var COLS : Int = 7; var ROWS : Int = 10
        var wallThickness : Float = 4.0F
    }

    init {
        wallPaint.color = Color.BLUE
        wallPaint.strokeWidth = wallThickness

        playerPaint.color = Color.GREEN
        exitPaint.color = Color.RED
        createMaze()
    }

    private fun createMaze() {

        cells = Array(COLS) { col ->
            Array(ROWS) { row ->
                Cell(col, row)
            }
        }


        var stackCell : Stack<Cell> = Stack<Cell>()
        var currentPos : Cell? ; var nextPos : Cell?

        playerCell = cells[0][0]
        exitCell = cells[COLS-1][ROWS-1]

        currentPos = cells[0][0]
        currentPos.visited = true
        stackCell.push(currentPos)

        while (!stackCell.empty()) {
            nextPos = currentPos?.let { neighbourCell(it) }
            if(nextPos!=null) {
                currentPos?.let { removeWall(it, nextPos) }
                stackCell.push(currentPos)
                currentPos = nextPos
                currentPos.visited = true
            }
            else {
                currentPos = stackCell.pop()
            }
        }
    }

    private fun neighbourCell(currentPos: Cell): Cell? {

        var neighbours = ArrayList<Cell>()

        //left neighbour
        if(currentPos.col>0 && !cells[currentPos.col-1][currentPos.row].visited) {
            neighbours.add(cells[currentPos.col-1][currentPos.row])
        }
        //right neighbour
        if(currentPos.col+1<COLS && !cells[currentPos.col+1][currentPos.row].visited) {
            neighbours.add(cells[currentPos.col+1][currentPos.row])
        }
        //top neighbour
        if(currentPos.row>0 && !cells[currentPos.col][currentPos.row-1].visited) {
            neighbours.add(cells[currentPos.col][currentPos.row-1])
        }
        //bottom neighbour
        if(currentPos.row+1<ROWS && !cells[currentPos.col][currentPos.row+1].visited) {
            neighbours.add(cells[currentPos.col][currentPos.row + 1])
        }

        if(neighbours.size>0) {
            var randomIndex : Int = random.nextInt(neighbours.size)
            return neighbours[randomIndex]
        }
        return null
    }

    private fun removeWall(currentPos: Cell, nextPos: Cell) {

        //check if next cell is above of the current cell
        if(currentPos.col==nextPos.col && currentPos.row==nextPos.row+1) {
            currentPos.topWall=false
            nextPos.bottomWall=false
        }
        //check if next cell is bottom of the current cell
        if(currentPos.col==nextPos.col && currentPos.row==nextPos.row-1) {
            currentPos.bottomWall=false
            nextPos.topWall=false
        }
        //check if next cell is right of the current cell
        if(currentPos.row==nextPos.row && currentPos.col==nextPos.col-1) {
            currentPos.rightWall=false
            nextPos.leftWall=false
        }
        //check if next cell is left of the current cell
        if(currentPos.row==nextPos.row && currentPos.col==nextPos.col+1) {
            currentPos.leftWall=false
            nextPos.rightWall=false
        }
    }



    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(Color.YELLOW)

        var w: Int = width
        var h: Int = height

        Log.i("WH", "$w,$h")

        cellSize = if (w/h <= COLS/ROWS) {
            (w/(COLS+1)).toFloat()
        } else {
            (h/(ROWS+1)).toFloat()
        }

        Log.i("CellSize",cellSize.toString())

        horMargin = (w-(COLS*cellSize))/2
        verMargin = (h-(ROWS*cellSize))/2

        Log.i("horMaegin",horMargin.toString())
        Log.i("verMargin",verMargin.toString())

        canvas?.translate(horMargin, verMargin)

        for(i in 0 until COLS) {
            for(j in 0 until ROWS) {

                if(cells[i][j].topWall) {
                    canvas?.drawLine(
                        i*cellSize, j*cellSize,
                        (i+1)*cellSize, j*cellSize,
                        wallPaint
                    )
                }
                if(cells[i][j].leftWall) {
                    canvas?.drawLine(
                        i*cellSize, j*cellSize,
                        i*cellSize, (j+1)*cellSize,
                        wallPaint
                    )
                }
                if(cells[i][j].bottomWall) {
                    canvas?.drawLine(
                        i*cellSize, (j+1)*cellSize,
                        (i+1)*cellSize, (j+1)*cellSize,
                        wallPaint
                    )
                }
                if(cells[i][j].rightWall) {
                    canvas?.drawLine(
                        (i+1)*cellSize, j*cellSize,
                        (i+1)*cellSize, (j+1)*cellSize,
                        wallPaint
                    )
                }
            }
        }

        var marginfor4Side : Float = cellSize/10
        canvas?.drawRect(
            (playerCell.col*cellSize)+marginfor4Side, (playerCell.row*cellSize)+marginfor4Side,
            ((playerCell.col+1)*cellSize)-marginfor4Side, ((playerCell.row+1)*cellSize)-marginfor4Side,
            playerPaint
        )

        canvas?.drawRect(
            (exitCell.col*cellSize)+marginfor4Side, (exitCell.row*cellSize)+marginfor4Side,
            ((exitCell.col+1)*cellSize)-marginfor4Side, ((exitCell.row+1)*cellSize)-marginfor4Side,
            exitPaint
        )

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if(event?.action == MotionEvent.ACTION_MOVE) {
            var xCo : Float = event.x
            var yCo : Float = event.y

            Log.i("xCo",xCo.toString())
            Log.i("yCo",yCo.toString())

            var playerCenterX : Float = horMargin+(playerCell.col+0.5F)*cellSize
            var playerCenterY: Float = verMargin+(playerCell.row+0.5F)*cellSize

            Log.i("playerCenterX",playerCenterX.toString())
            Log.i("playerCenterY",playerCenterY.toString())

            var dx : Float = xCo-playerCenterX
            var dy : Float = yCo-playerCenterY

            Log.i("dx",dx.toString())
            Log.i("dy",dy.toString())

            var absDx : Float = abs(dx)
            var absDy : Float = abs(dy)

            Log.i("absDx",absDx.toString())
            Log.i("absDy",absDy.toString())

            if(absDx>cellSize || absDy>cellSize) {
                if(absDx>absDy) {
                    //move in x-direction
                    if(dx>0) {
                       //move in right-direction
                        movePlayer(Directions.RIGHT)
                    }
                    else {
                        //move in left-direction
                        movePlayer(Directions.LEFT)
                    }
                }
                else {
                    //move in y-direction
                    if(dy>0) {
                        //move in down-direction
                        movePlayer(Directions.DOWN)
                    }
                    else {
                        //move in up-direction
                        movePlayer(Directions.UP)
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun movePlayer (directions: Directions) {
        when(directions) {
            Directions.UP -> {
                if(!playerCell.topWall) {
                    playerCell = cells[playerCell.col][playerCell.row-1]
                }
            }
            Directions.DOWN -> {
                if(!playerCell.bottomWall) {
                    playerCell = cells[playerCell.col][playerCell.row+1]
                }
            }
            Directions.RIGHT -> {
                if(!playerCell.rightWall) {
                    playerCell = cells[playerCell.col+1][playerCell.row]
                }
            }
            else -> {
                if(!playerCell.leftWall) {
                    playerCell = cells[playerCell.col-1][playerCell.row]
                }
            }
        }
        checkExit()
        invalidate()
    }

    private fun checkExit() {
        if(playerCell == exitCell) {
            Toast.makeText(context, "Congratulations, Be ready for new one!", Toast.LENGTH_SHORT).show()
            createMaze()
        }
    }

    class Cell(var col: Int, var row: Int) {

        var topWall = true; var rightWall = true; var bottomWall = true; var leftWall = true
        var visited = false

    }
}
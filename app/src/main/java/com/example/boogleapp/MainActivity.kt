package com.example.boogleapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.lang.Math.max
import java.lang.Math.min

class MainActivity : AppCompatActivity() {

    lateinit var gridView: GridView;
    lateinit var listView: ListView;
    var diceSet = arrayOf(
        "AAEIOT",
        "ABILRT",
        "ABJMOQ",
        "ACDEMP",
        "ACELRS",
        "ADENVZ",
        "AFIORX",
        "AIMORS",
        "DENOST",
        "EEFHIS",
        "EGINTV",
        "EGLNUY",
        "EHINRS",
        "EILRUW",
        "EKNOTU",
        "ELPSTU"
    )

    private var letterGridValues = arrayOf(
        "A", "B", "C", "D",
        "A", "B", "C", "D",
        "A", "B", "C", "D",
        "A", "B", "C", "D",
    )

    private var wordsHashMap:HashMap<String,Int> = HashMap<String,Int>()
    private var prefixHashMap:HashMap<String,Int> = HashMap<String,Int>()

    private val NROWS = 4;
    private val NCOLS = 4;
    private val SMALLEST_WORD_SIZE = 3;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gridView = findViewById(R.id.letterGrid);
        listView = findViewById(R.id.listWords);
        generate_grid();
        //val file_name = "OUT_WORDS.txt"
        //val bufferReader = application.assets.open(file_name).bufferedReader()
        //val data = bufferReader.use {
        //    it.readText()
        //}
        //Log.d("salut", data);

        Log.d("salut", "ReadWordsBegin")

        val file_name_WORDS = "OUT_WORDS.txt"
        val bufferReader_WORDS = application.assets.open(file_name_WORDS).bufferedReader()
        val data_WORDS = bufferReader_WORDS.useLines {
            lines -> lines.forEach { wordsHashMap.put(it, 1) }
        }
        //lineList.forEach { Log.d("salut",">  " + it) }
        Log.d("salut", "ReadWordsDone")

        Log.d("salut", "ReadPrefixBegin")

        val file_name_PREFIX = "OUT_PREFIX.txt"
        val bufferReader_PREFIX = application.assets.open(file_name_PREFIX).bufferedReader()
        val data_PREFIX = bufferReader_PREFIX.useLines {
                lines -> lines.forEach { prefixHashMap.put(it, 1) }
        }
        //lineList.forEach { Log.d("salut",">  " + it) }
        Log.d("salut", "ReadPrefixDone")

        Log.d("salut", wordsHashMap["habile"].toString())
        Log.d("salut", wordsHashMap["dkshqgui"].toString())
    }

    fun generate_grid(){
        Log.d("salut", diceSet.joinToString())
        diceSet.shuffle()
        Log.d("salut", diceSet.joinToString())
        for (i in letterGridValues.indices){
            var curDice = diceSet.get(i)
            val rnd_ind = (0..5).random()
            letterGridValues[i] = curDice[rnd_ind].toString()
        }
        var arrAdapt = ArrayAdapter(this, R.layout.mytextview , letterGridValues )

        gridView.setAdapter(arrAdapt);

    }

    fun onClickGenerate(view: View){
        generate_grid()
    }

    fun onClickSolve(view: View){
        solve_grid()
    }

    fun extending(prefix: String, path: Array<Pair<Int, Int>>): Array<String>{
        var solutions = arrayOf<String>()
        if (prefix.length >= SMALLEST_WORD_SIZE && wordsHashMap.get(prefix) != null){
            solutions = solutions + arrayOf(prefix.uppercase())
        }
        val curpair = path.last()
        Log.d("salut", "CUR_POS:"+curpair.toString())
        for (pair in neighbors(curpair)){
            if (!path.contains(pair)){
                val prefix1 = prefix + letterGridValues.get(pair.first+pair.second*NROWS).lowercase()
                Log.d("salut", "TEST_PREFIX:"+prefix1)
                if (prefixHashMap.get(prefix1) != null){
                    solutions = solutions + extending(prefix1, path + arrayOf(pair))
                }
            }
        }
        return solutions
    }

    fun neighbors(node:Pair<Int, Int>):Array<Pair<Int, Int>>{
        var out = arrayOf<Pair<Int, Int>>()
        val x = node.first
        val y = node.second
        for (i in max(0,x-1) until min(x+2, NCOLS)){
            for (j in max(0,y-1) until min(y+2, NROWS)){
                out=out+Pair(i, j)
            }
        }
        out.forEach { Log.d("salut", "NEIGHBORS_FOUND:"+it.toString()) }

        return out
    }

    fun solve_grid(){
        var wordsFound = arrayOf<String>()

        for (i in 0 until NROWS){
            for (j in 0 until NCOLS){
                val startPos = Pair(i,j)
                val letter = letterGridValues.get(i+j*NROWS).lowercase()
                Log.d("salut", "STARTING_LETTER:"+letter)
                val cur_solutions = extending(letter, arrayOf(startPos))
                Log.d("salut", "END_LETTER_SOLUTIONS:"+cur_solutions.size.toString())
                wordsFound = wordsFound + cur_solutions;
            }

        }
        wordsFound.sort()
        wordsFound.sortWith(compareByDescending { it.length })
        val final = wordsFound.distinct()


        var arrAdapt = ArrayAdapter(this, android.R.layout.simple_list_item_1 , final )

        listView.setAdapter(arrAdapt);

    }
}
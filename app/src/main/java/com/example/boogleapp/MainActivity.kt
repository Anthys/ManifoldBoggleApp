package com.example.boogleapp

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType.TYPE_CLASS_NUMBER
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.w3c.dom.Text
import java.io.File
import java.lang.Math.max
import java.lang.Math.min
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    lateinit var gridView: GridView;
    lateinit var listView: ListView;
    lateinit var toolbar: Toolbar;
    lateinit var timerText: TextView;
    lateinit var timer: CountDownTimer;
    private var timerRunning = false;

    val diceSet = arrayOf(
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

    val diceSet5 = arrayOf(
        "MDNSNH",
        "GFSTEY",
        "LMTRXS",
        "TTRSCH",
        "BMLNDL",
        "TMRDBT",
        "EIUEAO",
        "RLXSSB",
        "NAATEQ",
        "TCJFSH",
        "IEEAOA",
        "NDHSNM",
        "IAAIEO",
        "OEUEIA",
        "LCPRJS",
        "DSTLSM",
        "NKLPFN",
        "DWRNLP",
        "RZNNTQ",
        "RGLRVF",
        "RVCGRT",
        "IIOEAE",
        "EUIAEO",
        "UIAEOA",
        "NSEVAE"
    )

    private var letterGridValues = arrayOf(
        "A", "B", "C", "D",
        "A", "B", "C", "D",
        "A", "B", "C", "D",
        "A", "B", "C", "D",
    )

    private var testGrid = arrayOf(
        "N", "L", "Y", "A",
        "F", "O", "U", "H",
        "I", "O", "N", "D",
        "A", "Q", "V", "T"
    )

    private val sizesHashMap:HashMap<Int, Float> = HashMap<Int, Float>()

    private var wordsHashMap:HashMap<String,Int> = HashMap<String,Int>()
    private var prefixHashMap:HashMap<String,Int> = HashMap<String,Int>()
    private var wordsFound = arrayOf<String>()

    private var NROWS = 4;
    private var NCOLS = 4;
    private var SMALLEST_WORD_SIZE = 3;
    private var RETURN_LETTERS = false;
    private var GO_THROUGH_EDGES = false;
    private var TIMER_SECONDS = 3*60.toLong();

    inner class MyAdapter(private val context: Context, private val arrayList: Array<String>) : BaseAdapter() {
        private lateinit var letter: TextView
        override fun getCount(): Int {
            return arrayList.size
        }
        override fun getItem(position: Int): Any {
            return position
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            convertView = LayoutInflater.from(context).inflate(R.layout.mytextview, parent, false)
            letter = convertView.findViewById(R.id.letter)
            letter.text = arrayList[position]

            var delta_index = 0;
            var orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                delta_index = 100;
            }

            var size = sizesHashMap[NROWS+delta_index]
            if (size is Float) {
                letter.setTextSize(size)
            }else{
                letter.setTextSize((70-(NROWS-3)*12).toFloat())
            }
            return convertView
        }
    }

    override fun onSaveInstanceState(outState: Bundle){
        outState.putInt("boardSize", NROWS);
        outState.putInt("smallestWordSize", SMALLEST_WORD_SIZE);
        outState.putBoolean("returnLetters", RETURN_LETTERS);
        outState.putBoolean("goThroughEdges", GO_THROUGH_EDGES);
        outState.putLong("timeSeconds", TIMER_SECONDS);
        outState.putStringArray("wordsFound", wordsFound);
        outState.putStringArray("gridLetters", letterGridValues);
        super.onSaveInstanceState(outState);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupAll()

        if(savedInstanceState != null){
            Log.d("salut", "RELOAD")
            var a = savedInstanceState.getStringArray("gridLetters")
            if (a != null) {
                letterGridValues = a
            };
            var b = savedInstanceState.getStringArray("wordsFound")
            if (b != null) {
                wordsFound = b
            };
            NROWS = savedInstanceState.getInt("boardSize");

            SMALLEST_WORD_SIZE = savedInstanceState.getInt("smallestWordSize");
            RETURN_LETTERS= savedInstanceState.getBoolean("returnLetters");
            GO_THROUGH_EDGES= savedInstanceState.getBoolean("goThroughEdges");
            TIMER_SECONDS = savedInstanceState.getLong("timeSeconds");

            show_grid()
            show_solutions()
        }else{
            firstLaunch()
        }

    }

    fun setupAll(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView = findViewById(R.id.letterGrid);
        listView = findViewById(R.id.listWords);
        timerText = findViewById(R.id.timerText)

        init_timer()

        sizesHashMap[3] = 70.toFloat()
        sizesHashMap[4] = 60.toFloat()
        sizesHashMap[5] = 54.toFloat()
        sizesHashMap[6] = 45.toFloat()
        sizesHashMap[7] = 41.toFloat()

        sizesHashMap[100+3] = 54.toFloat()
        sizesHashMap[100+4] = 50.toFloat()
        sizesHashMap[100+5] = 40.toFloat()
        sizesHashMap[100+6] = 33.toFloat()
        sizesHashMap[100+7] = 28.toFloat()


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

    fun firstLaunch(){
        reset_grid()
        //val file_name = "OUT_WORDS.txt"
        //val bufferReader = application.assets.open(file_name).bufferedReader()
        //val data = bufferReader.use {
        //    it.readText()
        //}
        //Log.d("salut", data);

    }

    fun reset_grid(){
        generate_grid()
        show_grid()
        clear_list_view()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menubar,menu)
        return true
    }

    override fun onOptionsItemSelected (item: MenuItem): Boolean{
        val id = item.itemId

        if (id == R.id.ReturnLetters){
            RETURN_LETTERS = !RETURN_LETTERS;
            Log.d("salut", RETURN_LETTERS.toString())
            item.setChecked(RETURN_LETTERS)
            return true
        }
        if (id == R.id.GoThroughEdges){
            GO_THROUGH_EDGES = !GO_THROUGH_EDGES;
            Log.d("salut", GO_THROUGH_EDGES.toString())
            item.setChecked(GO_THROUGH_EDGES)
            return true
        }
        if (id==R.id.action_settings){
            letterGridValues = testGrid.clone()
            var arrAdapt = ArrayAdapter(this, R.layout.mytextview , letterGridValues )

            gridView.setAdapter(arrAdapt);

            clear_list_view();
        }
        if (id==R.id.setTime){
            onClickChangeTime();
        }
        if (id==R.id.setBoardSize){
            onClickSetBoardSize();
        }
        return false

    }

    fun initialize_timer_text(){
        var txt = "%d:%02d"
        txt = String.format(txt,
            TimeUnit.SECONDS.toMinutes(TIMER_SECONDS),
            TimeUnit.SECONDS.toSeconds(TIMER_SECONDS) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(TIMER_SECONDS))
        );
        timerText.setText(txt)
    }

    fun generate_grid(){
        Log.d("salut", diceSet.joinToString())
        diceSet.shuffle()
        Log.d("salut", diceSet.joinToString())
        letterGridValues = Array(NROWS*NROWS){"0"}
        if (NROWS == 4){
            for (i in letterGridValues.indices){
                var curDice = diceSet.get(i)
                val rnd_ind = (0..5).random()
                letterGridValues[i] = curDice[rnd_ind].toString()
            }
        }else if (NROWS==5){
            diceSet5.shuffle()
            for (i in letterGridValues.indices){
                var curDice = diceSet5.get(i)
                val rnd_ind = (0..5).random()
                letterGridValues[i] = curDice[rnd_ind].toString()
            }
        }else{
            for (i in 0 until NROWS*NROWS){
                val rnd_dice = diceSet.get((0..diceSet.size-1).random())
                val rnd_ind = (0..5).random()
                letterGridValues[i] = rnd_dice[rnd_ind].toString()
            }
        }

    }

    fun show_grid(){
        gridView.numColumns = NROWS;
        var arrAdapt = MyAdapter(this, letterGridValues)
        gridView.setAdapter(arrAdapt);
    }

    fun onClickGenerate(view: View){
        reset_grid()
    }

    fun onClickSolve(view: View){
        solve_grid()
    }
    fun onClickTimer(view: View){
        if (timerRunning) {
            timer.cancel()
            initialize_timer_text()
            timerRunning = false
        }else{
            timerRunning = true
            timer.start()
        }
    }

    fun init_timer(){
        timer = object: CountDownTimer((TIMER_SECONDS-1)*1000, 250) {
            override fun onTick(millisUntilFinished: Long) {

                var curmils = millisUntilFinished + 1000;

                var txt = "%d:%02d"
                txt = String.format(txt,
                    TimeUnit.MILLISECONDS.toMinutes(curmils),
                    TimeUnit.MILLISECONDS.toSeconds(curmils) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(curmils))
                );
                timerText.setText(txt)
            }

            override fun onFinish() {
                timerText.setText("STOP")
            }
        }
        initialize_timer_text()
    }

    fun onClickChangeTime(){
        val builder = AlertDialog.Builder(this)
        var input=EditText(this);
        input.inputType = TYPE_CLASS_NUMBER;
        builder.setTitle("Set time in seconds")
        builder.setView(input)
        //builder.setMessage("We have a message")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener(
            function = fun (var1: DialogInterface, var2: Int) {
                TIMER_SECONDS = input.getText().toString().toLong()
                init_timer()
            }
            )
        )

        //builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        //    Toast.makeText(applicationContext,
        //        android.R.string.yes, Toast.LENGTH_SHORT).show()
        //}
        builder.show()
    };

    fun onClickSetBoardSize(){
        val builder = AlertDialog.Builder(this)
        var input=EditText(this);
        input.inputType = TYPE_CLASS_NUMBER;
        builder.setTitle("Set board size (NROWS)")
        builder.setView(input)
        //builder.setMessage("We have a message")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener(
            function = fun (var1: DialogInterface, var2: Int) {
                NROWS = input.getText().toString().toInt()
                NCOLS = NROWS;
                reset_grid()
            }
        )
        )

        //builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        //    Toast.makeText(applicationContext,
        //        android.R.string.yes, Toast.LENGTH_SHORT).show()
        //}
        builder.show()

    }

    fun extending(prefix: String, path: Array<Pair<Int, Int>>): Array<String>{
        var solutions = arrayOf<String>()
        if (prefix.length >= SMALLEST_WORD_SIZE && wordsHashMap.get(prefix) != null){
            solutions = solutions + arrayOf(prefix.uppercase())
        }
        val curpair = path.last()
        //Log.d("salut", "CUR_POS:"+curpair.toString())
        for (pair in neighbors(curpair)){
            if ((path.last() != pair) && (RETURN_LETTERS || !path.contains(pair))){
                val prefix1 = prefix + letterGridValues.get(pair.first+pair.second*NROWS).lowercase()
                //Log.d("salut", "TEST_PREFIX:"+prefix1)
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
        if (!GO_THROUGH_EDGES) {
            for (i in max(0, x - 1) until min(x + 2, NCOLS)) {
                for (j in max(0, y - 1) until min(y + 2, NROWS)) {
                    out = out + Pair(i, j)
                }
            }
        }else{
            for (i in x - 1 until x + 2) {
                for (j in y - 1 until y + 2) {
                    out = out + Pair(i.mod(NCOLS), j.mod(NROWS))
                }
            }
        }
        //out.forEach { Log.d("salut", "NEIGHBORS_FOUND:"+it.toString()) }

        return out
    }

    fun solve_grid(){
        wordsFound = arrayOf<String>();
        Log.d("salut", letterGridValues.toString())

        for (i in 0 until NROWS){
            for (j in 0 until NCOLS){
                val startPos = Pair(i,j)
                val letter = letterGridValues.get(i+j*NROWS).lowercase()
                //Log.d("salut", "STARTING_LETTER:"+letter)
                val cur_solutions = extending(letter, arrayOf(startPos))
                //Log.d("salut", "END_LETTER_SOLUTIONS:"+cur_solutions.size.toString())
                wordsFound = wordsFound + cur_solutions;
            }

        }
        wordsFound.sort()
        wordsFound.sortWith(compareByDescending { it.length })

        show_solutions()

    }

    fun show_solutions(){
        val final = wordsFound.distinct()


        var arrAdapt = ArrayAdapter(this, android.R.layout.simple_list_item_1 , final )

        listView.setAdapter(arrAdapt);

    }

    fun clear_list_view(){
        var blank_list = arrayOf<String>()

        var arrAdapt = ArrayAdapter(this, android.R.layout.simple_list_item_1 , blank_list )

        listView.setAdapter(arrAdapt);
        wordsFound = arrayOf<String>()
    }
}
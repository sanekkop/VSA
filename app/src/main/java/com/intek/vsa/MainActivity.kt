package com.intek.vsa

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.http.Header
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    var barcode = ""
    private var widthDisplay = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == -1)
            || (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == -1)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET
                ), 0
            )
        }
        //widthDisplay = windowManager.defaultDisplay.width

        btn.setOnClickListener {
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            startActivityForResult(intent, 10)
        }
        btnRefresh!!.setOnClickListener {
            RefreshCondition()
        }
        btnRefresh.visibility = View.INVISIBLE

        barcode = savedInstanceState?.getString("BARCODE_KEY","").toString()
        if (barcode != "" && barcode != "null")
        {
            tvresult.text = barcode
            btnRefresh.visibility = View.VISIBLE
            RefreshStatus()
        }
    }

    fun RefreshStatus() {
        hadinfo.text = ""
        tkorr.text = ""
        tableLayout.removeAllViewsInLayout()
        val table = TableLayout(this)

        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        val rowDayLabels = TableRow(this)
        rowDayLabels.setGravity(Gravity.CENTER_HORIZONTAL)
        rowDayLabels.setGravity(Gravity.CENTER_VERTICAL)

        var sectorLabel = TextView(this)
        sectorLabel.text = "Сектор"
        sectorLabel.textSize = 16F
        //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //sectorLabel.width = 1
        sectorLabel.typeface = Typeface.DEFAULT_BOLD

        var pocessLabel = TextView(this)
        pocessLabel.text = "Задание"
        pocessLabel.textSize = 16F
        //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //sectorLabel.width = 1
        pocessLabel.typeface = Typeface.DEFAULT_BOLD

        var employerLabel = TextView(this)
        employerLabel.text = "Сотрудник"
        employerLabel.textSize = 16F
        //employerLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.45).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //employerLabel.width = 9
        employerLabel.typeface = Typeface.DEFAULT_BOLD

        val date1Label = TextView(this)
        date1Label.text = "Дата"
        date1Label.textSize = 16F
        //date1Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //date1Label.width = 5
        date1Label.typeface = Typeface.DEFAULT_BOLD

        rowDayLabels.setBackgroundColor(Color.LTGRAY)
        rowDayLabels.addView(sectorLabel)
        rowDayLabels.addView(pocessLabel)
        rowDayLabels.addView(employerLabel)
        rowDayLabels.addView(date1Label)
        table.addView(rowDayLabels)

        //тут у нас по идее только один элемент дата должен быть
        val jsonArrayData: JSONArray?
        try {
            jsonArrayData = myArray!!.getJSONArray("data")
        } catch (e: Exception) {
            tvresult.text = "Ошибка! Возможно не верный штрих-код"
            return
        }
        if (jsonArrayData.length() == 0) {
            //пустой массив
            tvresult.text = "Ошибка! Возможно не верный штрих-код"
            return
        }

        val dateDisp = jsonArrayData.getJSONObject(0).getString("date5")
        val dateKompl = jsonArrayData.getJSONObject(0).getString("date6")
        if (dateDisp == "01.01.1753") {
            hadinfo.text = "НЕ ЗАРЕГИСТРИРОВАНА"
            hadinfo.setTextColor(Color.RED)
        } else if (dateKompl != "01.01.1753") {
            //уже скомплектована
            val timeKompl = jsonArrayData.getJSONObject(0).getString("time6").toInt()
            var timeKompl_string =
                String.format(
                    "%02d:%02d",
                    timeKompl / 3600,
                    timeKompl / 60 % 60
                )
            timeKompl_string = " (" + timeKompl_string + ")"
            hadinfo.text = ("ЗАКРЫТА " + dateKompl + timeKompl_string)
            hadinfo.setTextColor(Color.GREEN)
        } else {
            val timeDisp = jsonArrayData.getJSONObject(0).getString("time5").toInt()
            var tameDisp_string =
                String.format("%02d:%02d", timeDisp / 3600, timeDisp / 60 % 60)
            tameDisp_string = " (" + tameDisp_string + ")"
            hadinfo.text = ("ЗАРЕГИСТРИРОВАНА " + dateDisp + tameDisp_string)
            hadinfo.setTextColor(Color.BLUE)
        }
        var isKorr = false

        for (jsonIndex in 0..(jsonArrayData.length() - 1)) {

            val rowLabels = TableRow(this)
            rowLabels.setGravity(Gravity.CENTER)
            var textColorKorr = Color.BLACK
            if (jsonArrayData.getJSONObject(jsonIndex).getString("korr") == "1") {
                isKorr = true
                textColorKorr = Color.RED
            }
            //сектор нужен всегда
            sectorLabel = TextView(this)
            //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            sectorLabel.text = jsonArrayData.getJSONObject(jsonIndex).getString("sector")
            sectorLabel.typeface = Typeface.DEFAULT_BOLD
            sectorLabel.textSize = 16F
            sectorLabel.gravity = Gravity.CENTER
            sectorLabel.setTextColor(textColorKorr)
            //sectorLabel.width = 1
            rowLabels.addView(sectorLabel)

            //теперь смотрим что происходит с заявкой
            val rowPocess = TableRow(this)
            rowPocess.setGravity(Gravity.CENTER)
            pocessLabel = TextView(this)

            //сначала смотрим время помплектации
            val datekompl = jsonArrayData.getJSONObject(jsonIndex).getString("date3")
            val timekompl = jsonArrayData.getJSONObject(jsonIndex).getString("time3").toInt()
            val datespusk = jsonArrayData.getJSONObject(jsonIndex).getString("date4")
            val timespusk = jsonArrayData.getJSONObject(jsonIndex).getString("time4").toInt()
            val datenabOk = jsonArrayData.getJSONObject(jsonIndex).getString("date2")
            val datenabNach = jsonArrayData.getJSONObject(jsonIndex).getString("date1")
            employerLabel = TextView(this)
            var Employer = "Неопределен"


            var time_string: String
            if (datekompl != "01.01.1753") {
                //скомплектована
                //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                pocessLabel.text = "Скомпл-но"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer2").toString()
                time_string =
                    String.format(
                        "%02d:%02d",
                        timekompl / 3600,
                        timekompl / 60 % 60
                    )
                rowLabels.setBackgroundColor(Color.GREEN)
            } else if (timekompl != 0) {
                //в комплектации
                time_string =
                    String.format(
                        "%02d:%02d",
                        timekompl / 3600,
                        timekompl / 60 % 60
                    )
                pocessLabel.text = "Комплект."
                time_string = " (" + time_string + ")"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer2").toString()
                rowLabels.setBackgroundColor(Color.GREEN)
            } else if (datespusk != "01.01.1753") {
                pocessLabel.text = "Спущено"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer3").toString()
                time_string =
                    String.format(
                        "%02d:%02d",
                        timespusk / 3600,
                        timespusk / 60 % 60
                    )
                rowLabels.setBackgroundColor(Color.BLUE)
            } else if (timespusk != 0) {
                //в комплектации
                time_string =
                    String.format(
                        "%02d:%02d",
                        timespusk / 3600,
                        timespusk / 60 % 60
                    )
                pocessLabel.text = "Спускают"
                time_string = " (" + time_string + ")"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer3").toString()
                rowLabels.setBackgroundColor(Color.BLUE)

            } else if (datenabOk != "01.01.1753") {
                val timenabOk = jsonArrayData.getJSONObject(jsonIndex).getString("time2").toInt()
                time_string =
                    String.format(
                        "%02d:%02d",
                        timenabOk / 3600,
                        timenabOk / 60 % 60
                    )
                pocessLabel.text = "Набрали"
                time_string = " (" + time_string + ")"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer").toString()
                rowLabels.setBackgroundColor(Color.YELLOW)
            } else if (datenabNach != "01.01.1753") {
                val timenabNach = jsonArrayData.getJSONObject(jsonIndex).getString("time1").toInt()
                time_string =
                    String.format(
                        "%02d:%02d",
                        timenabNach / 3600,
                        timenabNach / 60 % 60
                    )
                pocessLabel.text = "В наборе"
                time_string = " (" + time_string + ")"
                Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer").toString()
                rowLabels.setBackgroundColor(Color.YELLOW)
            } else {
                pocessLabel.text = "Ожидает набор"
                time_string = "( : : )"
                rowLabels.setBackgroundColor(Color.MAGENTA)
                textColorKorr = Color.BLACK
            }

            pocessLabel.typeface = Typeface.DEFAULT_BOLD
            pocessLabel.textSize = 14F
            pocessLabel.setTextColor(textColorKorr)
            //sectorLabel.width = 1
            rowLabels.addView(pocessLabel)

            if (Employer != "Неопределен") {
                //распармим сотрудника до фио
                Employer = Employer.replace(" ", "\n")
                val lines: List<String> = Employer.lines()
                Employer = ""
                lines.forEach {
                    if (it.trim() != "") {
                        if (Employer.trim() == "") {
                            Employer = it + " "
                        } else {
                            Employer += it.substring(0, 1) + ". "
                        }

                    }
                }
            }
            employerLabel.text = Employer
            employerLabel.typeface = Typeface.DEFAULT_BOLD
            employerLabel.textSize = 14F
            employerLabel.setTextColor(textColorKorr)
            // employerLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.45).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            //employerLabel.width = 9
            rowLabels.addView(employerLabel)


            val dateLabel = TextView(this)
            dateLabel.textSize = 16F
            //date1Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            dateLabel.text = time_string
            dateLabel.typeface = Typeface.DEFAULT_BOLD
            dateLabel.setTextColor(textColorKorr)
            //date1Label.width = 5
            rowLabels.addView(dateLabel)
            table.addView(rowLabels)

        }

        if (isKorr) {
            tkorr.text = "ВНИМАНИЕ! Есть корректировки"
        }

        tableLayout.addView(table)
    }

    fun RefreshCondition() {
        //обновим табличку
        try {
            GetStatus(barcode)
        } catch (e: Exception) {
            //tvresult?.text = e.toString()
            tvresult.text = "Ошибка! Возможно не отсканировался штрих-код"

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 0) {
            barcode = scanres
            tvresult.text = barcode
            btnRefresh.visibility = View.VISIBLE
            RefreshCondition()
            //Timer("SettingUp", false).schedule(10000) {
           //     RefreshCondition()
           // }
        }
    }

    companion object {

        var scanres = ""
        var myArray: JSONObject? = null
    }

    fun GetStatus(param: String) {
        val idd: String = "99990" + param.substring(2, 4) + "00" + param.substring(4, 12)
        val url = "http://api.inteksar.ru/200617/view-order-status/get/"
        val asyncHttpClient = AsyncHttpClient()
        val requestParams = RequestParams()
        requestParams.put("order_id", idd)
        asyncHttpClient.post(url, requestParams, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                super.onSuccess(statusCode, headers, response)
                if (response != null) {
                    myArray = response
                    RefreshStatus()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                response: JSONObject?
            ) {
                super.onFailure(statusCode, headers, throwable, response)
                if (response != null) {
                    try {
                        myArray = response
                        RefreshStatus()
                    } catch (e: Exception) {
                        tvresult.text = "Ошибка связи!"
                    }

                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("BARCODE_KEY", barcode)
    }
}


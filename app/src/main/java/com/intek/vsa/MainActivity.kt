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

        tableLayout.removeAllViewsInLayout()
        val table = TableLayout(this)

        table.isStretchAllColumns = true
        table.isShrinkAllColumns = true

        val rowDayLabels = TableRow(this)
        rowDayLabels.setGravity(Gravity.CENTER_HORIZONTAL)
        rowDayLabels.setGravity(Gravity.CENTER_VERTICAL)

        val sectorLabel = TextView(this)
        sectorLabel.text = "Сектор"
        //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //sectorLabel.width = 1
        sectorLabel.typeface = Typeface.DEFAULT_BOLD

        val employerLabel = TextView(this)
        employerLabel.text = "Наборщик"
        //employerLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.45).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //employerLabel.width = 9
        employerLabel.typeface = Typeface.DEFAULT_BOLD

        val date1Label = TextView(this)
        date1Label.text = "Начало набора"
        //date1Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //date1Label.width = 5
        date1Label.typeface = Typeface.DEFAULT_BOLD

        val date2Label = TextView(this)
        date2Label.text = "Окончание набора"
        //date2Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        //date2Label.width = 5
        date2Label.typeface = Typeface.DEFAULT_BOLD
        rowDayLabels.setBackgroundColor(Color.LTGRAY)
        rowDayLabels.addView(sectorLabel)
        rowDayLabels.addView(employerLabel)
        rowDayLabels.addView(date1Label)
        rowDayLabels.addView(date2Label)
        table.addView(rowDayLabels)

        //тут у нас по идее только один элемент дата должен быть
        val jsonArrayData = myArray!!.getJSONArray("data")

        for (jsonIndex in 0..(jsonArrayData.length() - 1)) {
            val rowSection = TableRow(this)
            rowSection.setGravity(Gravity.CENTER)
            val sectorLabel = TextView(this)
            //sectorLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.05).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            sectorLabel.text = jsonArrayData.getJSONObject(jsonIndex).getString("sector")
            sectorLabel.typeface = Typeface.DEFAULT_BOLD
            //sectorLabel.width = 1
            rowSection.addView(sectorLabel)
            val employerLabel = TextView(this)
            //распармим сотрудника до фио
            var Employer = jsonArrayData.getJSONObject(jsonIndex).getString("employer").toString()
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
            employerLabel.text = Employer
            employerLabel.typeface = Typeface.DEFAULT_BOLD
           // employerLabel.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.45).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            //employerLabel.width = 9
            rowSection.addView(employerLabel)
            val date1Label = TextView(this)
            var date1 = jsonArrayData.getJSONObject(jsonIndex).getString("date1")
            var time1 = jsonArrayData.getJSONObject(jsonIndex).getString("time1").toInt()

            var tame1_string =
                String.format("%02d:%02d:%02d", time1 / 3600, time1 / 60 % 60, time1 % 60)

            tame1_string = " (" + tame1_string + ")"
            if (date1 == "01.01.1753") {
                date1 = "  .  .     (  :  :  )"
            } else {
                date1 += tame1_string
            }
            //date1Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            date1Label.text = date1
            date1Label.typeface = Typeface.DEFAULT_BOLD
            //date1Label.width = 5
            rowSection.addView(date1Label)
            var date2 = jsonArrayData.getJSONObject(jsonIndex).getString("date2")
            var time2 = jsonArrayData.getJSONObject(jsonIndex).getString("time2").toInt()
            var tame2_string =
                String.format("%02d:%02d:%02d", time2 / 3600, time2 / 60 % 60, time2 % 60)

            tame2_string = " (" + tame2_string + ")"
            if (date2 == "01.01.1753") {
                date2 = "  .  .     (  :  :  )"
            } else {
                date2 += tame2_string
            }
            val date2Label = TextView(this)
            //date2Label.layoutParams = LinearLayout.LayoutParams((widthDisplay*0.25).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            date2Label.text = date2
            date2Label.typeface = Typeface.DEFAULT_BOLD
            //date2Label.width = 5
            rowSection.addView(date2Label)
            table.addView(rowSection)

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
        var requestParams = RequestParams()
        requestParams.put("order_id", idd)
        asyncHttpClient.post(url, requestParams, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                super.onSuccess(statusCode, headers, response);
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
                super.onFailure(statusCode, headers, throwable, response);
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


package io.github.karino2.equationpad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import io.github.karino2.equationpad.mathview.MathView
import io.github.karino2.equationpad.mathview.Variable

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mathView = findViewById<MathView>(R.id.mathView)

        val et = findViewById<EditText>(R.id.editText)
        et.setOnKeyListener { v, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                mathView.selectedExpr?.let {
                    val newTerm = Variable(et.text.toString())
                    it.parent?.replace(it, newTerm)
                    et.setText("")
                    mathView.selectedExpr = newTerm
                }
            }

            true
        }
    }
}

package io.github.karino2.equationpad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import io.github.karino2.equationpad.mathview.MathView
import io.github.karino2.equationpad.mathview.Subscript
import io.github.karino2.equationpad.mathview.Variable

class MainActivity : AppCompatActivity() {

    val mathView by lazy {
        findViewById<MathView>(R.id.mathView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


        findViewById<Button>(R.id.buttonSubscript).setOnClickListener {
            mathView.selectedExpr?.let {
                val pare = it.parent!!
                val newTerm = Subscript(it, Variable("x"))
                pare.replace(it, newTerm)
                mathView.selectedExpr = newTerm.sub
            }
        }
    }
}

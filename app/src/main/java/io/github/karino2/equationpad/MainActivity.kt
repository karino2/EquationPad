package io.github.karino2.equationpad

import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.github.karino2.equationpad.mathview.*

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
                mathView.ifSelected { expr->
                    val newTerm = Variable(et.text.toString())
                    expr.parent?.replace(expr, newTerm)
                    et.setText("")
                    mathView.selectedExpr = newTerm
                }
                true
            } else {
                false
            }
        }


        findViewById<Button>(R.id.buttonSubscript).setOnClickListener {
            replaceWith {old->
                val newTerm = Subscript(old, Variable("n"))
                mathView.selectedExpr = newTerm.sub
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonSuperscript).setOnClickListener {
            replaceWith {old->
                val newTerm = Superscript(old, Variable("n"))
                mathView.selectedExpr = newTerm.sup
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonCopyLatex).setOnClickListener {
            copyToClipboard("\$\$${mathView.expr.toLatex()}\$\$")
        }
    }

    fun replaceWith(factory: (Expr)-> Expr) {
        mathView.ifSelected { selected ->
            val pare = selected.parent!!
            val newTerm = factory(selected)
            pare.replace(selected, newTerm)
        }
    }


    fun showMessage(msg : String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    private fun copyToClipboard(content: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("math", content)
        clipboard.primaryClip = clip
        showMessage("TeX copied to clipboard")
    }
}

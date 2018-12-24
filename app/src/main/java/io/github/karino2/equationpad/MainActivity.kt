package io.github.karino2.equationpad

import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.github.karino2.equationpad.mathview.*

class MainActivity : AppCompatActivity() {

    val mathView by lazy {
        findViewById<MathView>(R.id.mathView)
    }

    val editText by lazy {
        findViewById<EditText>(R.id.editText)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner = findViewById<Spinner>(R.id.spinnerEntity)
        val adapter = ArrayAdapter<Pair<String, String>>(this, android.R.layout.simple_list_item_1, listOf(Pair("none", "")) + Variable.entityMap.toList())

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = adapter.getItem(position)
                if(item.second != "") {
                    mathView.ifSelected {
                        replaceWithVar("\\${item.first}", it)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        editText.setOnKeyListener { v, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                mathView.ifSelected { expr->
                    val name = editText.text.toString()
                    editText.setText("")
                    replaceWithVar(name, expr)
                }
                true
            } else {
                false
            }
        }


        findViewById<Button>(R.id.buttonSubscript).setOnClickListener {
            replaceWith {old->
                val (newTerm, selectTarget) = addSubscript(old)
                mathView.selectedExpr = selectTarget
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonSuperscript).setOnClickListener {
            replaceWith {old->
                val (newTerm, selectTarget) = addSuperscript(old)
                mathView.selectedExpr = selectTarget
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonFunction).setOnClickListener {
            replaceWith {old->
                val newTerm = FuncExpr(old, Variable("x"))
                mathView.selectedExpr = newTerm.body
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonSum).setOnClickListener {
            replaceWith {old->
                val newTerm = SumExpr(old)
                mathView.selectedExpr = newTerm
                newTerm
            }
        }
        findViewById<Button>(R.id.buttonProd).setOnClickListener {
            replaceWith {old->
                val newTerm = ProdExpr(old)
                mathView.selectedExpr = newTerm
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonEqual).setOnClickListener {
            replaceWith {old->
                val newTerm = EqualExpr(old, Variable("x"))
                mathView.selectedExpr = newTerm.rightExpr
                newTerm
            }
        }

        findViewById<Button>(R.id.buttonVerticalBar).setOnClickListener {
            replaceWith {old->
                val newTerm = VerticalBarExpr(old, Variable("x"))
                mathView.selectedExpr = newTerm.rightExpr
                newTerm
            }
        }


        findViewById<Button>(R.id.buttonCopyLatex).setOnClickListener {
            copyToClipboard("\$\$${mathView.expr.toLatex()}\$\$")
        }

        findViewById<Button>(R.id.buttonSwitch).setOnClickListener {
            mathView.selectedExpr?.let {
                it.switch()
                mathView.requestLayout()
            }
        }

        findViewById<Button>(R.id.buttonProducts).setOnClickListener {
            mathView.selectedExpr?.let {
                when(it) {
                    is Products -> {
                        val newNode = Variable("x")
                        it.addChild(newNode)
                        mathView.selectedExpr = newNode
                    }
                    else -> {
                        replaceWith {oldExpr ->
                            val newTerm = Products(oldExpr, Variable("x"))
                            mathView.selectedExpr = newTerm.children[1]
                            newTerm
                        }
                    }
                }
                mathView.requestLayout()
            }
        }

        findViewById<Button>(R.id.buttonWiden).setOnClickListener {
            mathView.ifSelected { oldExpr->
                when(oldExpr) {
                    is Root -> {}
                    else -> mathView.selectedExpr = oldExpr.parent!!
                }
            }
        }

    }

    private fun addSuperscript(old: Expr): Pair<Expr, Expr> {
        return when(old) {
            is MathOpExpr-> {
                val selectTarget = Variable("n")
                old.superScript = selectTarget
                Pair(old, selectTarget)
            }
            else -> {
                val newTerm = Superscript(old, Variable("n"))
                val selectTarget = newTerm.sup
                Pair(newTerm, selectTarget)
            }
        }
    }

    private fun addSubscript(old: Expr): Pair<Expr, Expr> {
        return when(old) {
            is MathOpExpr -> {
                val target = Variable("n")
                old.subScript = target
                Pair(old, target)
            }
            else -> {
                val newTerm = Subscript(old, Variable("n"))
                Pair(newTerm, newTerm.sub)

            }
        }

    }

    private fun replaceWithVar(name: String, selected: Expr) {
        val newTerm = Variable(name)
        selected.parent?.replace(selected, newTerm)
        mathView.selectedExpr = newTerm
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

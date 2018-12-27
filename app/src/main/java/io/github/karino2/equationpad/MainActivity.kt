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

    fun generalFactoryHandler(rid: Int, factory: (old: Expr) -> Pair<Expr, Expr>) {
        findViewById<Button>(rid).setOnClickListener {
            replaceWith {old->
                val (newTerm, selectTarget) = factory(old)
                mathView.selectedExpr = selectTarget
                newTerm
            }
        }

    }

    fun <T : Expr> genericFactoryHandler(rid: Int, factory: (old:Expr) -> T, targetSelector: (T) -> Expr) {
        generalFactoryHandler(rid) { old ->
            val newTerm = factory(old)
            Pair(newTerm, targetSelector(newTerm))
        }
    }


    fun infixFactoryHandler(rid: Int, factory : (Expr, Expr)->InfixExpr) {
        genericFactoryHandler(rid, {factory(it, Variable("x"))}, {it.rightExpr})
    }

    inline fun <reified T:ExprGroup> replaceOrAddHandler(rid: Int, crossinline factory: (old:Expr, x:Expr)->T) {
        findViewById<Button>(rid).setOnClickListener {
            mathView.selectedExpr?.let {
                when(it) {
                    is T -> {
                        val newNode = Variable("x")
                        it.addChild(newNode)
                        mathView.selectedExpr = newNode
                    }
                    else -> {
                        replaceWith {oldExpr ->
                            val newTerm = factory(oldExpr, Variable("x"))
                            mathView.selectedExpr = newTerm.children[1]
                            newTerm
                        }
                    }
                }
                mathView.requestLayout()
            }
        }
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
                    if(!name.isNullOrEmpty())
                        replaceWithVar(name, expr)
                }
                true
            } else {
                false
            }
        }


        generalFactoryHandler(R.id.buttonSubscript, ::addSubscript)
        generalFactoryHandler(R.id.buttonSuperscript, ::addSuperscript)

        genericFactoryHandler(R.id.buttonFunction, {FuncExpr(it, Variable("x"))}, {it.body})

        fun replaceWholeHandler(rid: Int, factory : (Expr)->Expr) = genericFactoryHandler(rid, factory, {it})
        replaceWholeHandler(R.id.buttonSum, ::SumExpr)
        replaceWholeHandler(R.id.buttonProd, ::ProdExpr)

        infixFactoryHandler(R.id.buttonEqual, ::createEqualExpr)
        infixFactoryHandler(R.id.buttonVerticalBar, ::createVerticalBarExpr)
        infixFactoryHandler(R.id.buttonRArrow, ::createRArrowExpr)

        findViewById<Button>(R.id.buttonCopyLatex).setOnClickListener {
            copyToClipboard("\$\$${mathView.expr.toLatex()}\$\$")
        }

        findViewById<Button>(R.id.buttonSwitch).setOnClickListener {
            mathView.selectedExpr?.let {
                it.switch()
                mathView.requestLayout()
            }
        }

        replaceOrAddHandler(R.id.buttonProducts, ::Products)
        replaceOrAddHandler(R.id.buttonComma, ::CommaGroupExpr)

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

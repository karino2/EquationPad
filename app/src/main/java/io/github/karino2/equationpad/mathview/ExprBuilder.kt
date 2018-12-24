package io.github.karino2.equationpad.mathview

object ExprBuilder {
    fun v(name: String) = Variable(name)
    fun pro(a: Expr, b: Expr) =
        Products(a, b)
    fun sub(a: Expr, b: Expr) =
        Subscript(a, b)
    fun sup(a: Expr, b: Expr) =
        Superscript(a, b)
    fun sum(body: Expr, sup: Expr? = null, sub: Expr? = null) : SumExpr {
        val sumExp = SumExpr(body)
        assignSubSup(sumExp, sup, sub)
        return sumExp
    }
    fun prod(body: Expr, sup: Expr? = null, sub: Expr? = null) : ProdExpr {
        val prodExp = ProdExpr(body)
        assignSubSup(prodExp, sup, sub)
        return prodExp
    }

    private fun assignSubSup(mathExp: MathOpExpr, sup: Expr?, sub: Expr?) {
        sup?.let {
            mathExp.superScript = it
        }
        sub?.let {
            mathExp.subScript = it
        }
    }
}

fun build(body: ExprBuilder.()-> Expr) = ExprBuilder.body()
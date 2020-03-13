from flask import Flask
from flask import request
import symengine as sm
from sympy import *
from sympy.parsing.sympy_parser import parse_expr
from flashtext.keyword import KeywordProcessor

app = Flask(__name__)


@app.route('/')
def index():
    return "Hello, World!"


@app.route('/d', methods=['GET', 'POST'])
def d():

    v = Symbol(request.form.get("variable"))
    f = Function("f")(v)
    s = sympify(str(request.form.get("input")).lower())
    print(s)
    dstr = diff(f, v).subs(f, s).doit()
    print(dstr)
    return dstr


@app.route('/expand', methods=['GET', 'POST'])
def expand():
    # kw = KeywordProcessor()
    # kw.add_keyword('**', '^')
    # kw.add_keyword(' ', '')
    istr = request.form.get("input")
    estr = str(sm.expand(istr))
    # rstr = kw.replace_keywords(estr)
    return estr


if __name__ == '__main__':
    app.run(debug=True)

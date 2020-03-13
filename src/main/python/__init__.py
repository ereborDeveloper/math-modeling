from flask import Flask
from flask import request
import symengine as sm
from sympy import *

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
    istr = request.form.get("input")
    estr = str(sm.expand(istr))
    rstr = estr.replace("**", "^").replace(" ", "")
    return rstr


if __name__ == '__main__':
    app.run(debug=True)

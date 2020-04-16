from flask import Flask
from flask import request
import symengine as sm
from sympy import *
import time

from src.main.python.parse.Parse import Parse

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
    # time.sleep(10)
    return rstr


@app.route('/expand-list', methods=['GET', 'POST'])
def expandList():
    istr = request.form.get("input")
    estr = str(sm.expand(istr))
    rstr = estr.replace("**", "^").replace(" ", "")
    parse = Parse()
    return parse.getTermsFromString(rstr)

if __name__ == '__main__':
    app.run(debug=True)

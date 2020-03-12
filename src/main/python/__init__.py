from flask import Flask
from flask import request
import symengine as sm
from flashtext.keyword import KeywordProcessor

kw = KeywordProcessor()
kw.add_keyword('**', '^')
kw.add_keyword(' ', '')
app = Flask(__name__)


@app.route('/')
def index():
    return "Hello, World!"


@app.route('/expand', methods=['GET', 'POST'])
def expand():
    istr = request.form.get("input")
    estr = str(sm.expand(istr))
    return estr


if __name__ == '__main__':
    app.run(debug=True)

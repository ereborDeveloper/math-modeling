import unittest

from src.main.python.parse.Parse import Parse


class MyTestCase(unittest.TestCase):

    def setUp(self):
        self.parse = Parse()

    def test_terms(self):
        input = "1*a + b*2 - 0.1*a + a*0.3 + 0.4*b"
        expected = dict()
        expected["a"] = 1.2
        expected["b"] = 2.4
        # self.assertEqual(expected, self.parse.getTermsFromString(input))

        input = "Sin(x*a) - 2*Sin(x*a) - Cos(x*a) + Sin(x*a) + Cos(x*a)"
        self.assertEqual(dict(), self.parse.getTermsFromString(input))

    def test_sign(self):
            self.assertEqual(True, self.parse.isSign('+'))

    def test_split(self):
        istr = "x*x"
        expected = list()
        expected.append("x")
        expected.append("x")
        self.assertEqual(expected, self.parse.splitAndSkipInsideBrackets(istr, "*"))

    def test_index(self):
        self.assertEqual(-1, self.parse.index('avc', '+'))


if __name__ == '__main__':
    unittest.main()

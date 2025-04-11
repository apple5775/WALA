// Code generated with the assistance of ChatGPT (OpenAI)
// Date: March 10, 2025
function testExponentationConstant() {
  return 2 ** -6;
}

function testExponentationVariables(x, y) {
  return x ** y;
}

function testExponentationWithinFunction(two) {
  return  function w(i) { return i ** 1; } ( two );
}

function runExponentationTests() {
    var x = 2, y = 3;
    testExponentationConstant();
    testExponentationVariables(x, y);
    testExponentationWithinFunction(x);
}

runExponentationTests();
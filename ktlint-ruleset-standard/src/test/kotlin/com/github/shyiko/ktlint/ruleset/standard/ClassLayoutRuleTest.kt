package com.github.shyiko.ktlint.ruleset.standard

import org.testng.annotations.Test

class ClassLayoutRuleTest {

  @Test
  fun testLint() =
    testLintUsingResource(ClassLayoutRule())

}
